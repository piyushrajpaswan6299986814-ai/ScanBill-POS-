package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Customer
import com.example.data.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(viewModel: MainViewModel) {
    val productsState by viewModel.products.collectAsState()
    val customersState by viewModel.customers.collectAsState()

    val isDark = viewModel.isDarkTheme
    val textStyleColor = if (isDark) Color.White else SecondaryNavy

    var searchQuery by remember { mutableStateOf("") }
    var showCustomerDialog by remember { mutableStateOf(false) }

    // Filter products for quick shelf add
    val filteredCatalog = productsState.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBgMain else Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp) // Leave safety padding above base navigator
        ) {
            // --- TOP POS BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) DarkBgCard else Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = if (isDark) DarkAccentCyan else PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.translate("Billing Terminal", "बिल्लिंग टर्मिनल"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textStyleColor
                    )
                }

                // Clear Cart badge
                if (viewModel.activeCart.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearCart() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = ErrorCrimson)
                    }
                }
            }

            // Outer scroll for layout sections
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // --- IN-TERMINAL QUICK ADD PANEL ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(viewModel.translate("Search Product / Scan Barcode...", "सामग्री खोजें या बारकोड डालें...")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { 
                            if (searchQuery.isNotEmpty()) {
                                viewModel.handleBarcodeScanned(searchQuery)
                                searchQuery = ""
                            }
                        }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = if (isDark) DarkAccentCyan else PrimaryBlue)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("username_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick item shelf list
                if (searchQuery.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .heightIn(max = 200.dp)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            filteredCatalog.forEach { product ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.addToCart(product)
                                            searchQuery = ""
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(product.name, fontWeight = FontWeight.Bold, color = textStyleColor)
                                        Text("₹${product.sellingPrice} • Barcode: ${product.barcode}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = EmeraldSuccess)
                                }
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- POS ACTIVE CART ITEMS SLOTS ---
                Text(
                    text = viewModel.translate("Active Sales Cart", "सक्रिय बिक्री कार्ट"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textStyleColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (viewModel.activeCart.isEmpty()) {
                    // Friendly Empty State View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.translate("Your sales cart is empty", "आपका बिक्री कार्ट खाली है"),
                                fontWeight = FontWeight.SemiBold,
                                color = textStyleColor
                            )
                            Text(
                                text = viewModel.translate("Search above, scan code, or use voice command.", "ऊपर खोजें, बारकोड स्कैन करें, या आवाज़ कमांड दें।"),
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    viewModel.activeCart.forEach { (product, quantity) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, color = textStyleColor, fontSize = 14.sp)
                                    Text("₹${product.sellingPrice} / unit", color = Color.Gray, fontSize = 11.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { viewModel.decreaseQuantity(product) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Sub", tint = if (isDark) DarkAccentCyan else PrimaryBlue)
                                    }

                                    Text(
                                        text = "$quantity",
                                        fontWeight = FontWeight.Black,
                                        color = textStyleColor,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.addToCart(product, 1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = EmeraldSuccess)
                                    }
                                }

                                Text(
                                    text = "₹${"%.1f".format(product.sellingPrice * quantity)}",
                                    fontWeight = FontWeight.Bold,
                                    color = textStyleColor,
                                    modifier = Modifier.weight(0.6f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- CUSTOMER RELATIONSHIP MANAGEMENT SECTION ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.translate("Linked Customer", "संबद्ध ग्राहक"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textStyleColor
                    )

                    Button(
                        onClick = { showCustomerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White),
                        border = BorderStroke(1.dp, if (isDark) Color.Gray else PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (isDark) DarkAccentCyan else PrimaryBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (viewModel.selectedCustomerPhone.isEmpty()) viewModel.translate("Link", "लिंक करें") else viewModel.translate("Change", "बदलें"),
                            fontSize = 11.sp,
                            color = if (isDark) Color.White else PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (viewModel.selectedCustomerPhone.isNotEmpty()) {
                    val matchingCust = customersState.firstOrNull { it.phone == viewModel.selectedCustomerPhone }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(matchingCust?.name ?: "Selected Buyer", fontWeight = FontWeight.Bold, color = textStyleColor)
                                Text("Ph: ${viewModel.selectedCustomerPhone} • Loyalty: ${matchingCust?.loyaltyPoints ?: 0} pts", fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.selectedCustomerPhone = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Unlink", tint = Color.Gray)
                            }
                        }
                    }
                } else {
                    Text(
                        text = viewModel.translate("No loyalty customer linked.", "कोई वफादारी ग्राहक जुड़ा नहीं है।"),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- TAXES, DISCOUNTS AND PRICUING CALCULATORS ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = viewModel.translate("Taxes & Discounts", "कर और छूट"),
                            fontWeight = FontWeight.Bold,
                            color = textStyleColor,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Discount Percentage selectors row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0.0, 5.0, 10.0, 15.0).forEach { disc ->
                                val active = viewModel.discountPercent == disc
                                Button(
                                    onClick = { viewModel.discountPercent = disc },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (active) (if (isDark) DarkAccentCyan else PrimaryBlue) else Color.Gray.copy(alpha = 0.1f),
                                        contentColor = if (active) Color.White else textStyleColor
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("$disc%", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Invoice Math Summaries
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(viewModel.translate("Cart Subtotal", "कार्ट सबटोटल"), color = Color.Gray, fontSize = 13.sp)
                            Text("₹${"%.2f".format(viewModel.cartSubtotal)}", color = textStyleColor, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(viewModel.translate("Flat Discount", "फ्लैट छूट"), color = Color.Gray, fontSize = 13.sp)
                            Text("- ₹${"%.2f".format(viewModel.cartDiscountAmount)}", color = ErrorCrimson, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST / CGST / SGST (${viewModel.taxRatePercent}%)", color = Color.Gray, fontSize = 13.sp)
                            Text("+ ₹${"%.2f".format(viewModel.cartTaxAmount)}", color = textStyleColor, fontSize = 13.sp)
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(viewModel.translate("TOTAL DUE", "कुल देय"), fontWeight = FontWeight.Black, fontSize = 18.sp, color = textStyleColor)
                            Text("₹${"%.2f".format(viewModel.cartFinalTotal)}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = if (isDark) EmeraldSuccess else PrimaryBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- PAYMENT GATEWAYS & CREDIT SETUP ---
                Text(
                    text = viewModel.translate("Select Payment Gateway", "भुगतान का साधन चुनें"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textStyleColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // CASH
                    Button(
                        onClick = {
                            viewModel.activePaymentMethod = "CASH"
                            viewModel.paymentStatus = "PAID"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.activePaymentMethod == "CASH") EmeraldSuccess else Color.Gray.copy(alpha = 0.1f),
                            contentColor = if (viewModel.activePaymentMethod == "CASH") Color.White else textStyleColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Payments, contentDescription = null)
                            Text("Cash", fontSize = 12.sp)
                        }
                    }

                    // UPI
                    Button(
                        onClick = {
                            viewModel.activePaymentMethod = "UPI"
                            viewModel.paymentStatus = "PAID"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.activePaymentMethod == "UPI") PurpleUPI else Color.Gray.copy(alpha = 0.1f),
                            contentColor = if (viewModel.activePaymentMethod == "UPI") Color.White else textStyleColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Text("UPI Scan", fontSize = 12.sp)
                        }
                    }

                    // CARD
                    Button(
                        onClick = {
                            viewModel.activePaymentMethod = "CARD"
                            viewModel.paymentStatus = "PAID"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.activePaymentMethod == "CARD") OrangeCard else Color.Gray.copy(alpha = 0.1f),
                            contentColor = if (viewModel.activePaymentMethod == "CARD") Color.White else textStyleColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CreditCard, contentDescription = null)
                            Text("Card Pay", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Conditionally render options based on selected gateway
                if (viewModel.activePaymentMethod == "CASH") {
                    OutlinedTextField(
                        value = viewModel.cashReceivedAmount,
                        onValueChange = { viewModel.cashReceivedAmount = it },
                        label = { Text(viewModel.translate("Cash Tendered Received (₹)", "प्राप्त नकद (₹)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            val due = viewModel.cartFinalTotal
                            val received = viewModel.cashReceivedAmount.toDoubleOrNull() ?: 0.0
                            if (received > due) {
                                Text(
                                    "Change: ₹${"%.1f".format(received - due)}",
                                    color = EmeraldSuccess,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    )
                }

                // Udhaar Toggle switch if loyalty customer is linked
                if (viewModel.selectedCustomerPhone.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (viewModel.paymentStatus == "DEBT") ErrorCrimson.copy(alpha = 0.5f) else Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = viewModel.translate("Add to Udhaar Debt Book (Credit)", "उधार खाता बुक में जोड़ें"),
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewModel.paymentStatus == "DEBT") ErrorCrimson else textStyleColor,
                                    fontSize = 13.sp
                                )
                                Text(viewModel.translate("Register receipt as unpaid store debt.", "अनपेड दुकान ऋण के रूप में दर्ज करें।"), fontSize = 11.sp, color = Color.Gray)
                            }
                            Switch(
                                checked = viewModel.paymentStatus == "DEBT",
                                onCheckedChange = { isDebt ->
                                    viewModel.paymentStatus = if (isDebt) "DEBT" else "PAID"
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ErrorCrimson,
                                    checkedTrackColor = ErrorCrimson.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // --- OUT-CHECKOUT TRIGGER BUTTON ---
                Button(
                    onClick = { viewModel.checkoutCurrentCart() },
                    enabled = viewModel.activeCart.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.paymentStatus == "DEBT") ErrorCrimson else EmeraldSuccess,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (viewModel.paymentStatus == "DEBT") viewModel.translate("SAVE UNPAID DEBT BILL ", "उधार बिल सहेजें") else viewModel.translate("INSTANT BILLING CHECKOUT", "त्वरित बिल भुगतान"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // --- SECTION OVERLAYS: SELECT LOYALTY CUSTOMER DIALOG ---
        if (showCustomerDialog) {
            Dialog(onDismissRequest = { showCustomerDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(0.95f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(viewModel.translate("Select Shop Customer", "ग्राहक चुनें"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textStyleColor)
                            IconButton(onClick = { showCustomerDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier.heightIn(max = 250.dp).verticalScroll(rememberScrollState())
                        ) {
                            customersState.forEach { customer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectedCustomerPhone = customer.phone
                                            showCustomerDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Bold, color = textStyleColor)
                                        Text("Ph: ${customer.phone}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    if (customer.creditBalance > 0) {
                                        Text("Debt: ₹${"%.1f".format(customer.creditBalance)}", color = ErrorCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("${customer.loyaltyPoints} pts", color = EmeraldSuccess, fontSize = 11.sp)
                                    }
                                }
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION OVERLAYS: MASTER RECEIPT DIALOG PRINT SIMULATOR ---
        val invoice = viewModel.checkoutDoneInvoice
        if (invoice != null) {
            Dialog(onDismissRequest = { viewModel.checkoutDoneInvoice = null }) {
                var printerConnected by remember { mutableStateOf(false) }
                var statusMssg by remember { mutableStateOf("Thermal Printer Offline") }

                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .verticalScroll(rememberScrollState()),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(viewModel.translate("Invoice Generated", "बिल रसीद"), fontWeight = FontWeight.Black, fontSize = 18.sp, color = textStyleColor)
                            IconButton(onClick = { viewModel.checkoutDoneInvoice = null }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // --- THERMAL PAPER EMULATOR FRAME ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
                            border = BorderStroke(1.dp, Color.LightGray),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    "--- SCANBILL TAX INVOICE ---",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "ScanBill Retail Store Terminal 01\nOwner: Rajesh Patel\nPh: +91 98765 43210",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.DarkGray
                                )

                                Text(
                                    "--------------------------------",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )

                                Text(
                                    "Invoice: ${invoice.orderId}\nDate: 2026-05-24 08:00 UTC\nPayment: ${invoice.paymentMethod} (${invoice.paymentStatus})",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.Black
                                )

                                Text(
                                    "--------------------------------",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )

                                // Print Items rows
                                viewModel.checkoutDoneItems.forEach { orderItem ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "${orderItem.productName.take(16)} x${orderItem.quantity}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = Color.Black
                                        )
                                        Text(
                                            "₹${"%.1f".format(orderItem.itemTotal)}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = Color.Black
                                        )
                                    }
                                }

                                Text(
                                    "--------------------------------",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                                    Text("₹${"%.2f".format(invoice.subtotal)}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Disc Recd:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.DarkGray)
                                    Text("-₹${"%.2f".format(invoice.discountAmount)}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("GST/Tax Paid:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.DarkGray)
                                    Text("+₹${"%.2f".format(invoice.taxAmount)}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("TOTAL NET DUE:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text("₹${"%.2f".format(invoice.finalAmount)}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }

                                Text(
                                    "--------------------------------",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )

                                Text(
                                    "Points Earned: ${(invoice.finalAmount / 50.0).toInt()}\nThank You! Visit ScanBill Again.",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- UTILITIES CONTROLLERS: PRINTER & SHARE ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Bluetooth 58mm Printer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textStyleColor)
                                    }
                                    Switch(
                                        checked = printerConnected,
                                        onCheckedChange = { conn ->
                                            printerConnected = conn
                                            statusMssg = if (conn) "Thermal 58mm Connected (Battery: 85%)" else "Thermal Printer Offline"
                                        }
                                    )
                                }
                                Text(statusMssg, fontSize = 11.sp, color = if (printerConnected) EmeraldSuccess else Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Print Receipt
                            Button(
                                onClick = {
                                    statusMssg = if (printerConnected) "SUCCESS: Printing Receipt..." else "ERROR: Toggle Bluetooth switch first!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print", fontSize = 12.sp)
                            }

                            // Share WhatsApp / SMS
                            Button(
                                onClick = {
                                    statusMssg = "WhatsApp Share link compiled successfully!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp PDF", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
