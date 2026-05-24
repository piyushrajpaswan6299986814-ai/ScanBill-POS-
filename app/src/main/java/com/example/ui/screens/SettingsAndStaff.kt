package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun SettingsAndStaffScreen(viewModel: MainViewModel) {
    val chatHistoryState by viewModel.chatHistory.collectAsState()
    val isDark = viewModel.isDarkTheme
    val textStyleColor = if (isDark) Color.White else SecondaryNavy

    var selectedTab by remember { mutableStateOf("SETTINGS") } // SETTINGS, CHAT, STAFF
    var showVoicePresetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBgMain else Color(0xFFF1F5F9))
            .padding(16.dp)
            .padding(bottom = 72.dp) // cushioned headroom above navigation bar
    ) {
        // --- Tab Top Selector Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) DarkBgCard else Color.White, shape = RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("SETTINGS", "AI CHAT_BOT", "STAFF").forEach { tab ->
                val active = selectedTab == tab
                Button(
                    onClick = { selectedTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) (if (isDark) DarkAccentCyan else PrimaryBlue) else Color.Transparent,
                        contentColor = if (active) Color.White else textStyleColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        text = when (tab) {
                            "SETTINGS" -> viewModel.translate("Configs", "सेटिंग्स")
                            "AI CHAT_BOT" -> "AI Assistant"
                            else -> viewModel.translate("Staff", "स्टाफ")
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- RENDER CONDITIONALLY BASED ON ACTIVE SUB-TAB ---
        when (selectedTab) {
            "SETTINGS" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Item 1: Language Configs
                    Card(colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Language (हिंदी / English)", fontWeight = FontWeight.Bold, color = textStyleColor, fontSize = 14.sp)
                                Text("Switch terminal labels translated schema.", fontSize = 11.sp, color = Color.Gray)
                            }
                            Switch(
                                checked = viewModel.isHindiLanguage,
                                onCheckedChange = { viewModel.isHindiLanguage = it }
                            )
                        }
                    }

                    // Item 2: Theme Settings
                    Card(colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Dark Theme Mode", fontWeight = FontWeight.Bold, color = textStyleColor, fontSize = 14.sp)
                                Text("Reduce eye strain in low-lit environments.", fontSize = 11.sp, color = Color.Gray)
                            }
                            Switch(
                                checked = viewModel.isDarkTheme,
                                onCheckedChange = { viewModel.isDarkTheme = it }
                            )
                        }
                    }

                    // Item 3: Printer Device Emulator Selection
                    Card(colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Thermal Receipt Printer", fontWeight = FontWeight.Bold, color = textStyleColor, fontSize = 14.sp)
                            Text("Set target Bluetooth printer link configs.", fontSize = 11.sp, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Pair BT Printer (58mm)", fontSize = 11.sp, color = textStyleColor)
                                }
                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("USB Printer Link (80mm)", fontSize = 11.sp, color = textStyleColor)
                                }
                            }
                        }
                    }

                    // Item 4: Backup SQLite Ledger
                    Card(colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Backup Store Database", fontWeight = FontWeight.Bold, color = textStyleColor, fontSize = 14.sp)
                                Text("Local SQLite encrypted snapshot is synced.", fontSize = 11.sp, color = Color.Gray)
                            }
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Backup DB", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            "AI CHAT_BOT" -> {
                // --- FULL TERMINAL CHAT WINDOW AND VOICE CONTROLS ---
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat History lists
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(if (isDark) Color.White.copy(alpha = 0.02f) else Color.White, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        if (chatHistoryState.isEmpty() && !viewModel.isChatLoading) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                Text("ScanBill AI Assistant is ready!", fontWeight = FontWeight.Bold, color = textStyleColor)
                                Text("Try voice inputs or ask how to link bluetooth printer.", fontSize = 11.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chatHistoryState) { chat ->
                                    val isAi = chat.sender == "AI"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isAi) (if (isDark) DarkBgCard else Color(0xFFF1F5F9)) else PurpleUPI
                                            ),
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp, topEnd = 16.dp,
                                                bottomStart = if (isAi) 4.dp else 16.dp, bottomEnd = if (isAi) 16.dp else 4.dp
                                            ),
                                            modifier = Modifier.fillMaxWidth(0.82f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = if (isAi) "ScanBill AI" else "Me (Operator)",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAi) (if (isDark) DarkAccentCyan else PrimaryBlue) else Color.White.copy(alpha = 0.7f)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = chat.message,
                                                    fontSize = 12.sp,
                                                    color = if (isAi) textStyleColor else Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text input entry panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.chatInput,
                            onValueChange = { viewModel.chatInput = it },
                            placeholder = { Text("Ask ScanBill AI...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.5f),
                            leadingIcon = {
                                IconButton(onClick = { showVoicePresetDialog = true }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice assistant", tint = PurpleUPI)
                                }
                            },
                            trailingIcon = {
                                if (viewModel.isChatLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    IconButton(onClick = { viewModel.sendChatMessage() }) {
                                        Icon(Icons.Default.Send, contentDescription = "Send", tint = if (isDark) DarkAccentCyan else PrimaryBlue)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            "STAFF" -> {
                // --- STAFF MANAGEMENT LAYOUT ---
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(viewModel.translate("Registered Store Terminal Staff", "पंजीकृत दुकान स्टाफ"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textStyleColor)
                        
                        AssistChip(
                            onClick = { },
                            label = { Text("Add Employee") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        )
                    }

                    // Stack of default staff members
                    val staffList = listOf(
                        Pair("Rajesh Patel", "Admin - Complete system permissions"),
                        Pair("Amit Kumar", "Staff Cashier - Cart checkout checkout, stock check"),
                        Pair("Sonia Verma", "Staff Cashier - Cart billing checkout")
                    )

                    staffList.forEach { (name, desc) ->
                        Card(colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Badge, contentDescription = null, tint = OrangeCard)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(name, fontWeight = FontWeight.Bold, color = textStyleColor, fontSize = 14.sp)
                                        Text(desc, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                TextButton(onClick = {}) {
                                    Text("Edit", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SUB OVERLAYS: VOICE COMMANDS PRESETS EMULATOR DROPDOWN ---
        if (showVoicePresetDialog) {
            Dialog(onDismissRequest = { showVoicePresetDialog = false }) {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = PurpleUPI)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice Command Simulator", fontWeight = FontWeight.Black, fontSize = 16.sp, color = textStyleColor)
                            }
                            IconButton(onClick = { showVoicePresetDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Text(
                            "Select an auditory statement to simulate speaking into the terminal mic in emulator:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Triggerable phrases mapping
                        val phrases = listOf(
                            "Add 1 Maggie noodles pack to cart",
                            "Settle balance with customer Amit Sharma",
                            "Predict inventory low stock restocks",
                            "Explain how to configure thermal receipt printer"
                        )

                        phrases.forEach { phrase ->
                            Button(
                                onClick = {
                                    viewModel.chatInput = phrase
                                    showVoicePresetDialog = false
                                    viewModel.sendChatMessage()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PurpleUPI, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(phrase, fontSize = 11.sp, color = textStyleColor, textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
