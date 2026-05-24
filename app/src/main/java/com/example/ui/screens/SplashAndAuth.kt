package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: MainViewModel) {
    var startPulse by remember { mutableStateOf(false) }
    val scaleFactor by animateFloatAsState(
        targetValue = if (startPulse) 1.15f else 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startPulse = true
        delay(1800) // 1.8s delays for brand showcase
        viewModel.currentScreen = "AUTH"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkBgMain, Color(0xFF1E3A8A), Color(0xFF020617))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scaleFactor)
                    .background(Color.White, shape = RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "ScanBill Logo",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "ScanBill",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            
            Text(
                text = "Next-Gen AI POS & Smart Retail",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = DarkAccentCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    var ownerName by remember { mutableStateOf("Rajesh Patel") }
    var shopName by remember { mutableStateOf("ScanBill Supermarket") }
    var phoneNo by remember { mutableStateOf("+91 98765 43210") }
    var emailId by remember { mutableStateOf("contact@scanbillstore.com") }
    var userRole by remember { mutableStateOf("Admin") } // Admin or Staff

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (viewModel.isDarkTheme) DarkBgMain else Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Retail icon",
                    tint = if (viewModel.isDarkTheme) DarkAccentCyan else PrimaryBlue,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ScanBill POS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (viewModel.isDarkTheme) Color.White else SecondaryNavy
                )
            }
            
            Text(
                text = viewModel.translate("Register / Access Store Terminal", "रजिस्टर / स्टोर टर्मिनल एक्सेस"),
                fontSize = 14.sp,
                color = if (viewModel.isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auth Input Panel Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isDarkTheme) DarkBgCard else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = viewModel.translate("Shop Parameters", "दुकान के विवरण"),
                        fontWeight = FontWeight.SemiBold,
                        color = if (viewModel.isDarkTheme) Color.White else SecondaryNavy,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text(viewModel.translate("Shop Name", "दुकान का नाम")) },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_shop_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(viewModel.translate("Owner / Manager Name", "मालिक / प्रबंधक का नाम")) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneNo,
                        onValueChange = { phoneNo = it },
                        label = { Text(viewModel.translate("Phone Number (SMS)", "फ़ोन नंबर")) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = emailId,
                        onValueChange = { emailId = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role Toggles
                    Text(
                        text = viewModel.translate("Terminal Access Level", "टर्मिनल पहुंच स्तर"),
                        color = if (viewModel.isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { userRole = "Admin" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userRole == "Admin") (if (viewModel.isDarkTheme) DarkAccentCyan else PrimaryBlue) else Color.Gray.copy(alpha = 0.2f),
                                contentColor = if (userRole == "Admin") Color.White else (if (viewModel.isDarkTheme) Color.White else SecondaryNavy)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Admin")
                        }

                        Button(
                            onClick = { userRole = "Staff" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userRole == "Staff") (if (viewModel.isDarkTheme) DarkAccentCyan else PrimaryBlue) else Color.Gray.copy(alpha = 0.2f),
                                contentColor = if (userRole == "Staff") Color.White else (if (viewModel.isDarkTheme) Color.White else SecondaryNavy)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Staff")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Big Launch button
            Button(
                onClick = {
                    viewModel.currentScreen = "DASHBOARD"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isDarkTheme) EmeraldSuccess else PrimaryBlue
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = viewModel.translate("Enter Shop Terminal", "दुकान टर्मिनल में प्रवेश करें"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go")
            }
        }
    }
}
