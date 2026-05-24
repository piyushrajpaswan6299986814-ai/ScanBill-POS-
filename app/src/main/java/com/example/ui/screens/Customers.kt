package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Customer
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun CustomersScreen(viewModel: MainViewModel) {
    val customersState by viewModel.customers.collectAsState()
    val isDark = viewModel.isDarkTheme
    val textStyleColor = if (isDark) Color.White else SecondaryNavy

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCollectDebtDialog by remember { mutableStateOf<Customer?>(null) }

    // Roster Fields for Add Customer drawer
    var rName by remember { mutableStateOf("") }
    var rPhone by remember { mutableStateOf("") }
    var rEmail by remember { mutableStateOf("") }

    // Debt Ledger fields
    var collectAmountStr by remember { mutableStateOf("") }
    var ledgerSuccessAlert by remember { mutableStateOf<String?>(null) }

    val filteredCustomers = customersState.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    rName = ""
                    rPhone = ""
                    rEmail = ""
                    showAddDialog = true
                },
                containerColor = if (isDark) DarkAccentCyan else PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        },
        containerColor = if (isDark) DarkBgMain else Color(0xFFF1F5F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .padding(bottom = 60.dp) // Leave a margin above layout nav tools
        ) {
            // --- TOP TITLE BAR ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = viewModel.translate("Customer Roster (Udhaar Book)", "ग्राहक सूची (उधार बुक)"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textStyleColor
                    )
                    Text(
                        text = "${customersState.size} " + viewModel.translate("Registered Store Members", "पंजीकृत वफादार ग्राहक"),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                AssistChip(
                    onClick = { /* Simulated backup status */ },
                    label = { Text("Encrypted Ledger", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(12.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(viewModel.translate("Search customers by phone, name...", "ग्राहक खोजें...")) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Debt Statistics Card overview
            val overallOutstandingDebt = customersState.sumOf { it.creditBalance }
            Card(
                colors = CardDefaults.cardColors(containerColor = ErrorCrimson.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = viewModel.translate("Outstanding Debt (Active Udhaar)", "बकाया कुल उधार"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorCrimson
                        )
                        Text(
                            text = "₹${"%.2f".format(overallOutstandingDebt)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else SecondaryNavy
                        )
                    }

                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorCrimson, modifier = Modifier.size(36.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- CUSTOMERS ROSTER LIST ---
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCustomers) { cust ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                        border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(cust.name, fontWeight = FontWeight.Black, color = textStyleColor, fontSize = 16.sp)
                                    Text("Phone: ${cust.phone} • Email: ${cust.email.ifEmpty { "None" }}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${cust.loyaltyPoints} Coins",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = EmeraldSuccess
                                    )
                                }
                            }

                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(viewModel.translate("Account Credit (Udhaar)", "खाता बकाया (उधार)"), fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = "₹${"%.2f".format(cust.creditBalance)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = if (cust.creditBalance > 0) ErrorCrimson else EmeraldSuccess
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Simulated debt cleared ping
                                    IconButton(
                                        onClick = {
                                            ledgerSuccessAlert = "WhatsApp debt reminder link generated for ${cust.name}! Ph: ${cust.phone}"
                                        },
                                        modifier = Modifier.background(Color.Gray.copy(alpha = 0.1f), shape = CircleShape)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Ping", tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                    }

                                    if (cust.creditBalance > 0) {
                                        Button(
                                            onClick = {
                                                collectAmountStr = ""
                                                showCollectDebtDialog = cust
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(viewModel.translate("Collect Cash", "नकद प्राप्त करें"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pop feedback banner if ping shared
            if (ledgerSuccessAlert != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { ledgerSuccessAlert = null }) {
                            Text("OK", color = Color.White)
                        }
                    },
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text(ledgerSuccessAlert ?: "")
                }
            }
        }

        // --- EXCELLENT DIAOLGS: ADD CUSTOMER WIDGET ---
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(viewModel.translate("Add Loyal Customer", "नया ग्राहक जोड़ें"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textStyleColor)
                            IconButton(onClick = { showAddDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = rName,
                            onValueChange = { rName = it },
                            label = { Text("Customer Name Label") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = rPhone,
                            onValueChange = { rPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = rEmail,
                            onValueChange = { rEmail = it },
                            label = { Text("Email Address (Optional)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (rName.trim().isNotEmpty() && rPhone.trim().isNotEmpty()) {
                                    viewModel.createCustomer(rName.trim(), rPhone.trim(), rEmail.trim())
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) DarkAccentCyan else PrimaryBlue),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CREATE PROFILE ACCOUNT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- EXCELLENT DIAOLGS: DEBT COLLECTION SETTLEMENT PANEL ---
        val selectedDebtCust = showCollectDebtDialog
        if (selectedDebtCust != null) {
            Dialog(onDismissRequest = { showCollectDebtDialog = null }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Collect Udhaar payment", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textStyleColor)
                            IconButton(onClick = { showCollectDebtDialog = null }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Customer: ${selectedDebtCust.name}\nOutstanding Balanced Debt: ₹${"%.2f".format(selectedDebtCust.creditBalance)}",
                            fontSize = 13.sp,
                            color = textStyleColor
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = collectAmountStr,
                            onValueChange = { collectAmountStr = it },
                            label = { Text("Amount Collected Received (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val parseAmt = collectAmountStr.toDoubleOrNull()
                                if (parseAmt != null && parseAmt > 0) {
                                    viewModel.collectCreditPayment(selectedDebtCust.phone, parseAmt)
                                    ledgerSuccessAlert = "SUCCESS: Settled ₹$parseAmt debt for ${selectedDebtCust.name}!"
                                    showCollectDebtDialog = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("SUBMIT CASH COLLECTION LINK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
