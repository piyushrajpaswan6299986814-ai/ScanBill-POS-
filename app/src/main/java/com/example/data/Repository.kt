package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    val shopDao = db.shopDao()
    val productDao = db.productDao()
    val customerDao = db.customerDao()
    val orderDao = db.orderDao()
    val chatDao = db.chatMessageDao()
    private val geminiManager = GeminiManager()

    // --- Flows for UI streams ---
    val shop: Flow<Shop?> = shopDao.getShop()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val chatHistory: Flow<List<ChatMessage>> = chatDao.getConversations()

    // --- Initial Prepopulate Method ---
    suspend fun prepopulateIfNeeded() = withContext(Dispatchers.IO) {
        // Build a default shop if none exists
        if (shopDao.getShopDirect() == null) {
            val defaultShop = Shop(
                shopId = "SHOP_101",
                name = "ScanBill Supermarket",
                ownerName = "Rajesh Patel",
                phone = "+91 98765 43210",
                email = "contact@scanbillstore.com",
                role = "Admin",
                currency = "₹",
                language = "en"
            )
            shopDao.insertShop(defaultShop)
        }

        // Fill pre-populated inventory
        if (productDao.getCount() == 0) {
            val sampleProducts = listOf(
                Product(
                    barcode = "89012300001",
                    name = "Maggie Masala Noodles Pack",
                    category = "Groceries",
                    subCategory = "Snacks",
                    sellingPrice = 15.00,
                    costPrice = 11.50,
                    stockQty = 85,
                    lowStockThreshold = 20,
                    supplierName = "Dev Snacks Distributors",
                    supplierPhone = "9898012345",
                    expiryDate = "2027-02-15",
                    imageUrl = ""
                ),
                Product(
                    barcode = "89012300002",
                    name = "Basmati Premium Rice 1kg",
                    category = "Groceries",
                    subCategory = "Grains",
                    sellingPrice = 125.00,
                    costPrice = 90.00,
                    stockQty = 8, // Triggers low stock alert
                    lowStockThreshold = 15,
                    supplierName = "Punjab Rice Mills Ltd",
                    supplierPhone = "9400234567",
                    expiryDate = "2028-05-30",
                    imageUrl = ""
                ),
                Product(
                    barcode = "89012300003",
                    name = "Amul Butter 100g",
                    category = "Groceries",
                    subCategory = "Dairy",
                    sellingPrice = 58.00,
                    costPrice = 48.00,
                    stockQty = 5, // Triggers low stock alert
                    lowStockThreshold = 10,
                    supplierName = "Amul Anand Cooperatives",
                    supplierPhone = "9900887766",
                    expiryDate = "2026-11-20",
                    imageUrl = ""
                ),
                Product(
                    barcode = "89012300004",
                    name = "Paracetamol 650mg Strips",
                    category = "Pharmacy",
                    subCategory = "Medicines",
                    sellingPrice = 42.00,
                    costPrice = 25.00,
                    stockQty = 145,
                    lowStockThreshold = 30,
                    supplierName = "HealthCare Biotech",
                    supplierPhone = "8899001122",
                    expiryDate = "2027-08-10",
                    imageUrl = ""
                ),
                Product(
                    barcode = "89012300005",
                    name = "Dettol Liquid Antiseptic 250ml",
                    category = "Pharmacy",
                    subCategory = "Hygiene",
                    sellingPrice = 168.00,
                    costPrice = 135.00,
                    stockQty = 4, // Triggers low stock alert
                    lowStockThreshold = 8,
                    supplierName = "HealthCare Biotech",
                    supplierPhone = "8899001122",
                    expiryDate = "2028-12-12",
                    imageUrl = ""
                ),
                Product(
                    barcode = "89012300006",
                    name = "Classic Cotton Men's Blue T-Shirt",
                    category = "Apparel",
                    subCategory = "Clothing",
                    sellingPrice = 599.00,
                    costPrice = 350.00,
                    stockQty = 18,
                    lowStockThreshold = 5,
                    supplierName = "Metropolis Garments",
                    supplierPhone = "7011223344",
                    expiryDate = "",
                    imageUrl = ""
                ),
                Product(
                    barcode = "89012300007",
                    name = "Fast USB-C Charger Power Adapter 30W",
                    category = "Electronics",
                    subCategory = "Accessories",
                    sellingPrice = 850.00,
                    costPrice = 520.00,
                    stockQty = 12,
                    lowStockThreshold = 5,
                    supplierName = "Alpha Electrics Ltd",
                    supplierPhone = "8055443322",
                    expiryDate = "",
                    imageUrl = ""
                ),
                Product(
                    barcode = "89012300008",
                    name = "Coca-Cola Refreshing Can 330ml",
                    category = "Groceries",
                    subCategory = "Beverages",
                    sellingPrice = 40.00,
                    costPrice = 30.00,
                    stockQty = 90,
                    lowStockThreshold = 20,
                    supplierName = "Snacks and Soda Co",
                    supplierPhone = "7700234561",
                    expiryDate = "2026-10-25",
                    imageUrl = ""
                )
            )
            productDao.insertProducts(sampleProducts)
        }

        // Fill pre-populated customers
        val initialCustomers = listOf(
            Customer("9876543210", "Amit Sharma", "amit@gmail.com", 320, 1850.00), // Credit (Udhaar)
            Customer("9812345678", "Anjali Verma", "anjali@verma.com", 85, 0.00),
            Customer("9900112233", "Vikram Singh", "vikram.singh@yahoo.com", 140, 420.00) // Credit (Udhaar)
        )
        for (c in initialCustomers) {
            if (customerDao.getCustomerByPhone(c.phone) == null) {
                customerDao.insertCustomer(c)
            }
        }

        // Prepopulate some demo order history to show charts immediately
        if (orderDao.getAllOrdersDirect().isEmpty()) {
            val order1 = Order(
                orderId = "TXN_4231",
                customerPhone = "9876543210",
                subtotal = 340.0,
                taxAmount = 18.0,
                discountAmount = 20.0,
                finalAmount = 338.0,
                paymentMethod = "UPI",
                paymentStatus = "PAID",
                timestamp = System.currentTimeMillis() - 86400000 * 3 // 3 Days ago
            )
            val items1 = listOf(
                OrderItem(orderId = "TXN_4231", productBarcode = "89012300001", productName = "Maggie Masala Noodles Pack", quantity = 10, unitPrice = 15.00, itemTotal = 150.00),
                OrderItem(orderId = "TXN_4231", productBarcode = "89012300002", productName = "Basmati Premium Rice 1kg", quantity = 1, unitPrice = 125.00, itemTotal = 125.00),
                OrderItem(orderId = "TXN_4231", productBarcode = "89012300003", productName = "Amul Butter 100g", quantity = 1, unitPrice = 58.00, itemTotal = 58.00)
            )

            val order2 = Order(
                orderId = "TXN_4232",
                customerPhone = "9900112233",
                subtotal = 1449.0,
                taxAmount = 75.0,
                discountAmount = 100.0,
                finalAmount = 1424.0,
                paymentMethod = "CARD",
                paymentStatus = "PAID",
                timestamp = System.currentTimeMillis() - 86400000 * 2 // 2 Days ago
            )
            val items2 = listOf(
                OrderItem(orderId = "TXN_4232", productBarcode = "89012300006", productName = "Classic Cotton Men's Blue T-Shirt", quantity = 2, unitPrice = 599.00, itemTotal = 1198.00),
                OrderItem(orderId = "TXN_4232", productBarcode = "89012300005", productName = "Dettol Liquid Antiseptic 250ml", quantity = 1, unitPrice = 168.00, itemTotal = 168.00)
            )

            val order3 = Order(
                orderId = "TXN_4233",
                customerPhone = "9876543210",
                subtotal = 650.0,
                taxAmount = 0.0,
                discountAmount = 50.0,
                finalAmount = 600.0,
                paymentMethod = "CASH",
                paymentStatus = "DEBT", // Credit
                timestamp = System.currentTimeMillis() - 86400000 * 1 // Yesterday
            )
            val items3 = listOf(
                OrderItem(orderId = "TXN_4233", productBarcode = "89012300007", productName = "Fast USB-C Charger Power Adapter 30W", quantity = 1, unitPrice = 850.00, itemTotal = 850.00)
            )

            val order4 = Order(
                orderId = "TXN_4234",
                customerPhone = "9812345678",
                subtotal = 90.0,
                taxAmount = 5.0,
                discountAmount = 0.0,
                finalAmount = 95.0,
                paymentMethod = "CASH",
                paymentStatus = "PAID",
                timestamp = System.currentTimeMillis() // Today
            )
            val items4 = listOf(
                OrderItem(orderId = "TXN_4234", productBarcode = "89012300001", productName = "Maggie Masala Noodles Pack", quantity = 6, unitPrice = 15.00, itemTotal = 90.00)
            )

            orderDao.insertOrder(order1)
            orderDao.insertOrderItems(items1)

            orderDao.insertOrder(order2)
            orderDao.insertOrderItems(items2)

            orderDao.insertOrder(order3)
            orderDao.insertOrderItems(items3)

            orderDao.insertOrder(order4)
            orderDao.insertOrderItems(items4)
        }
    }

    // --- Product Actions ---
    suspend fun addProduct(product: Product) = productDao.insertProduct(product)
    suspend fun getProduct(barcode: String): Product? = productDao.getProductByBarcode(barcode)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(barcode: String) = productDao.deleteByBarcode(barcode)

    // --- Customer Actions ---
    suspend fun addCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    suspend fun getCustomer(phone: String): Customer? = customerDao.getCustomerByPhone(phone)
    suspend fun addCredit(phone: String, creditDelta: Double) = customerDao.updateCreditBalance(phone, creditDelta)

    // --- Billing Checkout Flow ---
    suspend fun checkoutOrder(
        orderId: String,
        customerPhone: String,
        cartItems: List<Pair<Product, Int>>,
        subtotal: Double,
        taxAmount: Double,
        discountAmount: Double,
        finalAmount: Double,
        paymentMethod: String,
        paymentStatus: String,
        cashReceived: Double,
        changeGiven: Double
    ) = withContext(Dispatchers.IO) {
        // Create order
        val order = Order(
            orderId = orderId,
            customerPhone = customerPhone,
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            finalAmount = finalAmount,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            cashReceived = cashReceived,
            changeGiven = changeGiven,
            timestamp = System.currentTimeMillis()
        )
        // Store order
        orderDao.insertOrder(order)

        // Store order items & update product stock quantities
        val itemsList = cartItems.map { (product, quantity) ->
            // Update stock remaining
            val updatedProduct = product.copy(stockQty = (product.stockQty - quantity).coerceAtLeast(0))
            productDao.updateProduct(updatedProduct)

            OrderItem(
                orderId = orderId,
                productBarcode = product.barcode,
                productName = product.name,
                quantity = quantity,
                unitPrice = product.sellingPrice,
                itemTotal = product.sellingPrice * quantity
            )
        }
        orderDao.insertOrderItems(itemsList)

        // Update customer details if customer exists
        if (customerPhone.isNotEmpty()) {
            val loyaltyEarned = (finalAmount / 50.0).toInt() // 1 point per ₹50 spent
            customerDao.addLoyaltyPoints(customerPhone, loyaltyEarned)

            // If checked out as credit/debt, update customer's credit balances
            if (paymentStatus == "DEBT") {
                customerDao.updateCreditBalance(customerPhone, finalAmount)
            }
        }
    }

    // --- AI REST API Core Operations ---
    suspend fun getAiInsights(products: List<Product>, orders: List<Order>): String {
        val totalRevenue = orders.sumOf { it.finalAmount }
        val lowStockNames = products.filter { it.stockQty <= it.lowStockThreshold }.map { "${it.name} (Stock: ${it.stockQty})" }
        
        val prompt = """
            We are looking for Sales insights and procurement restock forecasts.
            Here is our shop database status:
            - Total Registered Products: ${products.size}
            - Low Stock alert items: ${lowStockNames.joinToString(", ")}
            - Total Historical Sales Count: ${orders.size}
            - Total Cumulative Revenue: Check-out summary adds to ₹$totalRevenue
            
            Synthesize a brief, smart, scanbill retail sales recommendation report with bullet points about:
            1. **Smart Restocking Predictions**: Which inventory requires immediate order to avoid outages.
            2. **Sales Forecasting**: Highlight seasonal or weekly demand patterns.
            3. **Profit & margin advice**: Quick tip to increase small shop profits.
        """.trimIndent()

        val systemPrompt = "You are scanbill, a highly professional AI and predictive retail consultant for grocery, pharma, electronics, and apparel shopkeepers. Keep answers under 250 words, with bold section headings."
        return geminiManager.getAIResponse(prompt, systemPrompt)
    }

    suspend fun processVoiceBillingInput(voiceString: String): String {
        val prompt = """
            Parse the following raw retail voice request: "$voiceString".
            Provide a valid JSON response matching this schema:
            {
              "action": "ADD_TO_CART" | "SEARCH_PRODUCT" | "GENERATE_BARCODE" | "UNKNOWN",
              "productName": String (item name mentioned, optional),
              "quantity": Integer (default 1),
              "feedback": String (A user greeting saying what scanbill did).
            }
            Return only the direct JSON contents under curly braces.
        """.trimIndent()
        
        val systemPrompt = "You are an intelligent retail speech parsing algorithm. You convert auditory statements into structural POS actions. Always response with clean JSON."
        return geminiManager.getAIResponse(prompt, systemPrompt)
    }

    suspend fun getChatResponse(chatMessage: String): String {
        val systemPrompt = "You are the ScanBill AI Virtual Shop Assistant. You assist shopkeepers in learning how to scan barcodes with their cameras, connect thermal bluetooth receipts printers, manage inventory, track payment debts (Udhaar tracking) or increase shop revenue."
        return geminiManager.getAIResponse(chatMessage, systemPrompt)
    }

    suspend fun clearChat() {
        chatDao.clearHistory()
    }

    suspend fun saveChatMessage(sender: String, message: String) {
        chatDao.insertMessage(ChatMessage(sender = sender, message = message))
    }
}
