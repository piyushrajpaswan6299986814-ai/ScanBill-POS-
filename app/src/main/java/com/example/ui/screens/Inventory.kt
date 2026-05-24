package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun InventoryScreen(viewModel: MainViewModel) {
    val productsState by viewModel.products.collectAsState()
    val isDark = viewModel.isDarkTheme
    val textStyleColor = if (isDark) Color.White else SecondaryNavy

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf<Product?>(null) }

    // Form Field States for Add Product Drawer
    var formBarcode by remember { mutableStateOf("") }
    var formName by remember { mutableStateOf("") }
    var formCategory by remember { mutableStateOf("Groceries") }
    var formSellingPrice by remember { mutableStateOf("") }
    var formCostPrice by remember { mutableStateOf("") }
    var formStockQty by remember { mutableStateOf("") }
    var formLowStockThreshold by remember { mutableStateOf("10") }
    var formSupplierName by remember { mutableStateOf("") }
    var formSupplierPhone by remember { mutableStateOf("") }
    var formExpiryDate by remember { mutableStateOf("") }

    // Sorting & Filtering products
    val processedInventory = productsState.filter {
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery)
        val matchesCategory = selectedCategory == "All" || it.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Reset fields for new entry
                    formBarcode = ""
                    formName = ""
                    formCategory = "Groceries"
                    formSellingPrice = ""
                    formCostPrice = ""
                    formStockQty = ""
                    formLowStockThreshold = "10"
                    formSupplierName = ""
                    formSupplierPhone = ""
                    formExpiryDate = ""
                    showAddDialog = true
                },
                containerColor = if (isDark) DarkAccentCyan else PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        containerColor = if (isDark) DarkBgMain else Color(0xFFF1F5F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .padding(bottom = 60.dp) // Leave cushion above bottom navigator bar
        ) {
            // --- TOP LOGISTIC BAR ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = viewModel.translate("Shop Inventory", "दुकान की सूची"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textStyleColor
                    )
                    Text(
                        text = "${productsState.size} " + viewModel.translate("Registered Stock SKUs", "पंजीकृत स्टॉक SKUs"),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Quick Import helper
                TextButton(onClick = { viewModel.triggerAiForecast() }) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isDark) DarkAccentCyan else PrimaryBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto Match", fontSize = 11.sp, color = if (isDark) DarkAccentCyan else PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Filter Row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(viewModel.translate("Search Catalog by SKU, Name...", "सामग्री खोजें...")) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Categories Quick Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Groceries", "Pharmacy", "Apparel", "Electronics").forEach { cat ->
                    val active = selectedCategory == cat
                    FilterChip(
                        selected = active,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) DarkAccentCyan else PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- INVENTORY BULK LIST VIEW ---
            if (processedInventory.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.5f))
                        Text(viewModel.translate("No inventory SKU matches filter", "कोई भी स्टॉक फ़िल्टर से मेल नहीं खाता"), color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(processedInventory) { product ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDetailsDialog = product },
                            border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic Stock Bar visual
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                product.stockQty <= 0 -> ErrorCrimson
                                                product.stockQty <= product.lowStockThreshold -> AlertAmber
                                                else -> EmeraldSuccess
                                            }
                                        )
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, color = textStyleColor, fontSize = 14.sp)
                                    Text("SKU Barcode: ${product.barcode}", fontSize = 11.sp, color = Color.Gray)
                                    
                                    if (product.expiryDate.isNotEmpty()) {
                                        Text("Exp: ${product.expiryDate}", fontSize = 11.sp, color = ErrorCrimson, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹${product.sellingPrice}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = textStyleColor
                                    )
                                    
                                    // Margin math
                                    val profit = product.sellingPrice - product.costPrice
                                    val marginPercent = if (product.sellingPrice > 0) (profit / product.sellingPrice) * 100.0 else 0.0
                                    Text(
                                        text = "Margin: ${"%.1f".format(marginPercent)}%",
                                        fontSize = 10.sp,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .background(
                                                color = (if (product.stockQty <= product.lowStockThreshold) AlertAmber else EmeraldSuccess).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${product.stockQty} In Stock",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (product.stockQty <= product.lowStockThreshold) AlertAmber else EmeraldSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- ADD / EDIT PRODUCT SLIDE DRAWER DIALOG ---
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .verticalScroll(rememberScrollState()),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(viewModel.translate("Register New Product", "नई सामग्री पंजीकृत करें"), fontWeight = FontWeight.Black, fontSize = 18.sp, color = textStyleColor)
                            IconButton(onClick = { showAddDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category Dropdown simulated
                        Text("Section Category", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Groceries", "Pharmacy", "Apparel", "Electronics").forEach { cat ->
                                val active = formCategory == cat
                                Button(
                                    onClick = { formCategory = cat },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (active) (if (isDark) DarkAccentCyan else PrimaryBlue) else Color.Gray.copy(alpha = 0.15f),
                                        contentColor = if (active) Color.White else textStyleColor
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Text(cat, fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Barcode scan input and auto generator SKU
                        OutlinedTextField(
                            value = formBarcode,
                            onValueChange = { formBarcode = it },
                            label = { Text("EAN / UPC Barcode Value") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    // Generate standard sample UPC barcode
                                    val randomCode = "890123" + (10000 + Random.nextInt(90000)).toString()
                                    formBarcode = randomCode
                                }) {
                                    Icon(Icons.Default.QrCode, contentDescription = "Gen Barcode", tint = if (isDark) DarkAccentCyan else PrimaryBlue)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Product Name Label") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("username_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formSellingPrice,
                                onValueChange = { formSellingPrice = it },
                                label = { Text("Selling Price (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formCostPrice,
                                onValueChange = { formCostPrice = it },
                                label = { Text("Cost Price (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formStockQty,
                                onValueChange = { formStockQty = it },
                                label = { Text("Stock Quantity (Qty)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formLowStockThreshold,
                                onValueChange = { formLowStockThreshold = it },
                                label = { Text("Low Limit Alert") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = formExpiryDate,
                            onValueChange = { formExpiryDate = it },
                            placeholder = { Text("YYYY-MM-DD (Optional for Medicine/Dairy)") },
                            label = { Text("Shelf Expiry Date") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = formSupplierName,
                            onValueChange = { formSupplierName = it },
                            label = { Text("Supplier Business Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (formBarcode.trim().isNotEmpty() && formName.trim().isNotEmpty()) {
                                    viewModel.createOrEditProduct(
                                        barcode = formBarcode.trim(),
                                        name = formName.trim(),
                                        category = formCategory,
                                        sellingPrice = formSellingPrice.toDoubleOrNull() ?: 0.0,
                                        costPrice = formCostPrice.toDoubleOrNull() ?: 0.0,
                                        stockQty = formStockQty.toIntOrNull() ?: 0,
                                        lowStockThreshold = formLowStockThreshold.toIntOrNull() ?: 10,
                                        supplierName = formSupplierName.trim(),
                                        supplierPhone = formSupplierPhone.trim(),
                                        expiryDate = formExpiryDate.trim()
                                    )
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) EmeraldSuccess else PrimaryBlue),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("SAVE STOCK DATA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SKU / DETAILS DIALOG PRODUCING INDIVIDUAL BARCODE CARD ---
        val detailedProduct = showDetailsDialog
        if (detailedProduct != null) {
            Dialog(onDismissRequest = { showDetailsDialog = null }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Product Metadata Card", fontWeight = FontWeight.Black, fontSize = 16.sp, color = textStyleColor)
                            IconButton(onClick = { showDetailsDialog = null }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Visual Barcode SVG Canvas drawing
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, Color.LightGray)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Generate highly professional varied physical barcodes lines
                                    listOf(3, 1, 4, 1, 2, 5, 1, 3, 2, 1, 4, 1, 3, 2, 1, 4, 3, 1).forEach { w ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(w.dp)
                                                .background(Color.Black)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = detailedProduct.barcode,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Details table rows
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow("SKU Name", detailedProduct.name, isDark)
                            DetailRow("Category", "${detailedProduct.category} • ${detailedProduct.subCategory.ifEmpty { "General" }}", isDark)
                            DetailRow("Barcode ID", detailedProduct.barcode, isDark)
                            DetailRow("Selling Price / MRP", "₹${detailedProduct.sellingPrice}", isDark)
                            DetailRow("Cost of Goods Unit", "₹${detailedProduct.costPrice}", isDark)
                            DetailRow("Active stock level", "${detailedProduct.stockQty} items", isDark)
                            DetailRow("Supplier Procured", detailedProduct.supplierName.ifEmpty { "General Sourcing" }, isDark)
                            
                            if (detailedProduct.expiryDate.isNotEmpty()) {
                                DetailRow("Expiry Date Tracker", detailedProduct.expiryDate, isDark, textC = ErrorCrimson)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.deleteStoreProduct(detailedProduct.barcode)
                                    showDetailsDialog = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorCrimson),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete SKU")
                            }

                            Button(
                                onClick = { showDetailsDialog = null },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) DarkAccentCyan else PrimaryBlue),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, valStr: String, isDark: Boolean, textC: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(
            text = valStr,
            color = textC ?: (if (isDark) Color.White else SecondaryNavy),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
