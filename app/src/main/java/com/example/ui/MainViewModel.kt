package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)

    // --- Core Reactive Database Streams ---
    val shop: StateFlow<Shop?> = repository.shop.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val products: StateFlow<List<Product>> = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lowStock: StateFlow<List<Product>> = repository.lowStockProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customers: StateFlow<List<Customer>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val orders: StateFlow<List<Order>> = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val chatHistory: StateFlow<List<ChatMessage>> = repository.chatHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Navigation ---
    var currentScreen by mutableStateOf("SPLASH")

    // --- Language and Theme Prefs ---
    var isDarkTheme by mutableStateOf(false)
    var isHindiLanguage by mutableStateOf(false)

    // --- Active Billing Checkout Cart State ---
    val activeCart = mutableStateMapOf<Product, Int>()
    var selectedCustomerPhone by mutableStateOf("")
    var discountPercent by mutableStateOf(0.0)
    var taxRatePercent by mutableStateOf(18.0) // 18% GST by default in India
    var activePaymentMethod by mutableStateOf("CASH") // CASH, UPI, CARD
    var paymentStatus by mutableStateOf("PAID") // PAID, DEBT
    var cashReceivedAmount by mutableStateOf("")

    // --- Detailed Invoice Dialog Overlay state ---
    var checkoutDoneInvoice by mutableStateOf<Order?>(null)
    var checkoutDoneItems = mutableListOf<OrderItem>()

    // --- Search & Filter constraints ---
    var productSearchQuery by mutableStateOf("")
    var productCategoryFilter by mutableStateOf("All")

    // --- AI insights status ---
    var aiInsightText by mutableStateOf("Click 'Generate AI Insights' below to analyze daily performance, stock trends, and procure forecasting...")
    var isInsightLoading by mutableStateOf(false)

    // --- Chat interface status ---
    var chatInput by mutableStateOf("")
    var isChatLoading by mutableStateOf(false)

    // --- Scanner simulation feedback ---
    var scannerFeedbackMessage by mutableStateOf<String?>(null)

    // --- Initialization ---
    init {
        viewModelScope.launch {
            // Seed base items, customers, and history
            repository.prepopulateIfNeeded()
        }
    }

    // --- Billing Mechanics ---
    val cartSubtotal: Double
        get() = activeCart.entries.sumOf { it.key.sellingPrice * it.value }

    val cartTaxAmount: Double
        get() = (cartSubtotal - cartDiscountAmount) * (taxRatePercent / 100.0)

    val cartDiscountAmount: Double
        get() = cartSubtotal * (discountPercent / 100.0)

    val cartFinalTotal: Double
        get() = (cartSubtotal - cartDiscountAmount) + cartTaxAmount

    fun addToCart(product: Product, qty: Int = 1) {
        val existingQty = activeCart[product] ?: 0
        activeCart[product] = existingQty + qty
    }

    fun decreaseQuantity(product: Product) {
        val existingQty = activeCart[product] ?: 0
        if (existingQty > 1) {
            activeCart[product] = existingQty - 1
        } else {
            activeCart.remove(product)
        }
    }

    fun removeFromCart(product: Product) {
        activeCart.remove(product)
    }

    fun clearCart() {
        activeCart.clear()
        selectedCustomerPhone = ""
        discountPercent = 0.0
        activePaymentMethod = "CASH"
        paymentStatus = "PAID"
        cashReceivedAmount = ""
    }

    // --- Simulate Barcode Reading in Emulator ---
    fun handleBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            val matchedProduct = repository.getProduct(barcode)
            if (matchedProduct != null) {
                addToCart(matchedProduct, 1)
                scannerFeedbackMessage = "Scanned: ${matchedProduct.name} - ₹${matchedProduct.sellingPrice} successfully!"
            } else {
                scannerFeedbackMessage = "Barcode Error: '$barcode' not found in store database. You can add it in Inventory!"
            }
        }
    }

    // --- Perform Order Checkout ---
    fun checkoutCurrentCart() {
        if (activeCart.isEmpty()) return
        viewModelScope.launch {
            val orderId = "TXN_${UUID.randomUUID().toString().take(6).uppercase()}"
            val finalAmt = cartFinalTotal
            val change = if (activePaymentMethod == "CASH" && cashReceivedAmount.toDoubleOrNull() != null) {
                (cashReceivedAmount.toDouble() - finalAmt).coerceAtLeast(0.0)
            } else {
                0.0
            }

            // Perform Repository checkout (modifies stock & customer credit)
            val cartList = activeCart.entries.map { Pair(it.key, it.value) }
            repository.checkoutOrder(
                orderId = orderId,
                customerPhone = selectedCustomerPhone,
                cartItems = cartList,
                subtotal = cartSubtotal,
                taxAmount = cartTaxAmount,
                discountAmount = cartDiscountAmount,
                finalAmount = finalAmt,
                paymentMethod = activePaymentMethod,
                paymentStatus = paymentStatus,
                cashReceived = cashReceivedAmount.toDoubleOrNull() ?: finalAmt,
                changeGiven = change
            )

            // Feed Invoice Detail display
            val shopDetails = repository.shopDao.getShopDirect()
            checkoutDoneItems.clear()
            checkoutDoneItems.addAll(cartList.map { (prod, qty) ->
                OrderItem(
                    orderId = orderId,
                    productBarcode = prod.barcode,
                    productName = prod.name,
                    quantity = qty,
                    unitPrice = prod.sellingPrice,
                    itemTotal = prod.sellingPrice * qty
                )
            })

            checkoutDoneInvoice = Order(
                orderId = orderId,
                customerPhone = selectedCustomerPhone,
                subtotal = cartSubtotal,
                taxAmount = cartTaxAmount,
                discountAmount = cartDiscountAmount,
                finalAmount = finalAmt,
                paymentMethod = activePaymentMethod,
                paymentStatus = paymentStatus,
                cashReceived = cashReceivedAmount.toDoubleOrNull() ?: finalAmt,
                changeGiven = change,
                timestamp = System.currentTimeMillis(),
                billShared = false,
                staffName = shopDetails?.ownerName ?: "Admin"
            )

            // Clean Cart
            clearCart()
        }
    }

    // --- Add New Product ---
    fun createOrEditProduct(
        barcode: String,
        name: String,
        category: String,
        sellingPrice: Double,
        costPrice: Double,
        stockQty: Int,
        lowStockThreshold: Int,
        supplierName: String,
        supplierPhone: String,
        expiryDate: String
    ) {
        viewModelScope.launch {
            val product = Product(
                barcode = barcode,
                name = name,
                category = category,
                sellingPrice = sellingPrice,
                costPrice = costPrice,
                stockQty = stockQty,
                lowStockThreshold = lowStockThreshold,
                supplierName = supplierName,
                supplierPhone = supplierPhone,
                expiryDate = expiryDate
            )
            repository.addProduct(product)
        }
    }

    fun deleteStoreProduct(barcode: String) {
        viewModelScope.launch {
            repository.deleteProduct(barcode)
        }
    }

    // --- Customer Operations ---
    fun createCustomer(name: String, phone: String, email: String) {
        viewModelScope.launch {
            val cust = Customer(
                phone = phone,
                name = name,
                email = email,
                loyaltyPoints = 0,
                creditBalance = 0.0
            )
            repository.addCustomer(cust)
            selectedCustomerPhone = phone
        }
    }

    fun collectCreditPayment(customerPhone: String, amount: Double) {
        viewModelScope.launch {
            repository.addCredit(customerPhone, -amount)
        }
    }

    // --- AI Operations ---
    fun triggerAiForecast() {
        if (isInsightLoading) return
        isInsightLoading = true
        aiInsightText = "Connecting to ScanBill AI predictive engine... (Analyzing sales margins and low stock parameters)"
        viewModelScope.launch {
            val prodList = products.value
            val ordList = orders.value
            val response = repository.getAiInsights(prodList, ordList)
            aiInsightText = response
            isInsightLoading = false
        }
    }

    fun sendChatMessage() {
        val userQuery = chatInput
        if (userQuery.trim().isEmpty()) return
        chatInput = ""
        isChatLoading = true

        viewModelScope.launch {
            repository.saveChatMessage("USER", userQuery)
            
            // Check if it's a voice/billing request format (e.g. "Add milk to cart")
            val lower = userQuery.lowercase()
            if (lower.contains("add ") || lower.contains("put ") || lower.contains("maggie") || lower.contains("rice")) {
                val jsonResponseStr = repository.processVoiceBillingInput(userQuery)
                try {
                    val jsonObj = JSONObject(jsonResponseStr)
                    val action = jsonObj.optString("action", "UNKNOWN")
                    val feedback = jsonObj.optString("feedback", "")
                    
                    if (action == "ADD_TO_CART") {
                        // Attempt to match named pre-loaded item
                        val nameMatch = jsonObj.optString("productName", "").lowercase()
                        val matched = products.value.firstOrNull { 
                            it.name.lowercase().contains(nameMatch) || nameMatch.contains(it.name.lowercase()) 
                        } ?: products.value.firstOrNull { it.barcode == "89012300001" } // Fallback to Maggie Noodles
                        
                        if (matched != null) {
                            val qty = jsonObj.optInt("quantity", 1)
                            addToCart(matched, qty)
                            repository.saveChatMessage("AI", "${feedback}\n\n[Action Executed: Added ${matched.name} ${qty}x to cart]")
                        } else {
                            repository.saveChatMessage("AI", "Voice parser parsed successfully, but could not match product index locally.")
                        }
                    } else {
                        repository.saveChatMessage("AI", "Voice match command unknown: $feedback")
                    }
                } catch (e: Exception) {
                    val fallbackResponse = repository.getChatResponse(userQuery)
                    repository.saveChatMessage("AI", fallbackResponse)
                }
            } else {
                // Ordinary Chat help query
                val aiResponse = repository.getChatResponse(userQuery)
                repository.saveChatMessage("AI", aiResponse)
            }
            isChatLoading = false
        }
    }

    fun clearActiveChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // --- Multi-language Translations Helper ---
    fun translate(enText: String, hiText: String): String {
        return if (isHindiLanguage) hiText else enText
    }
}
