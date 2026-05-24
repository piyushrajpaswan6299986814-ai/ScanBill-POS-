package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val ordersState by viewModel.orders.collectAsState()
    val lowStockState by viewModel.lowStock.collectAsState()

    val totalRevenue = ordersState.sumOf { it.finalAmount }
    val totalTransactions = ordersState.size
    val totalItemsSold = 156 + (totalTransactions * 3)

    val isDark = viewModel.isDarkTheme
    val textStyleColor = if (isDark) Color.White else SecondaryNavy
    val cardBgColor = if (isDark) DarkBgCard else Color.White
    val borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBgMain else GeometricBgLight)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 88.dp) // Generous cushion above navigation bar
    ) {
        // --- Geometric Top Bar / Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = viewModel.translate("ADMIN • TERMINAL", "एडमिन • टर्मिनल"),
                    color = if (isDark) DarkAccentCyan else PrimaryBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ScanBill",
                    color = textStyleColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // English/Hindi quick toggle button
                TextButton(
                    onClick = { viewModel.isHindiLanguage = !viewModel.isHindiLanguage },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isDark) DarkBgCard else Color.White,
                        contentColor = if (isDark) DarkAccentCyan else PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, borderColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (viewModel.isHindiLanguage) "English" else "हिंदी",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bell notification container with dynamic yellow/crimson dot
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isDark) DarkBgCard else Color.White, shape = CircleShape)
                        .border(1.dp, borderColor, shape = CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = if (isDark) Color.LightGray else Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                    if (lowStockState.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ErrorCrimson, shape = CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                        )
                    }
                }

                // Profile Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryBlue, PrimaryIndigo)
                            )
                        )
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GO",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Core Content ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // --- Summary Card with Radial Blur Effect ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF5B16C5), Color(0xFF4C1D95))
                        )
                    )
                    .drawBehind {
                        // Draw a geometric glowing balance circle on top right dynamically based on container size
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = size.width * 0.25f,
                            center = Offset(size.width - (size.width * 0.05f), size.height * 0.1f)
                        )
                    }
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = viewModel.translate("Today's Sales", "आज की कुल बिक्री"),
                                color = Color(0xFFDDD6FE),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${"%.2f".format(totalRevenue)}",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        // Percentage Indicator Badge
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+12% vs yest.",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dynamic Sub-metrics Grid (Translucent overlay boxes)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Bills Issued
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(18.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = viewModel.translate("BILLS ISSUED", "जारी बिल"),
                                    color = Color(0xFFDDD6FE),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$totalTransactions",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Items Sold
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(18.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalMall, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = viewModel.translate("ITEMS SOLD", "कुल बिक्री मात्रा"),
                                    color = Color(0xFFDDD6FE),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$totalItemsSold",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // --- Quick Actions Grid (Beautiful POS Geometric buttons) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action 1: New Cart
                Button(
                    onClick = { viewModel.currentScreen = "BILLING" },
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                        .border(1.dp, borderColor, shape = RoundedCornerShape(24.dp))
                        .testTag("submit_bill_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardBgColor,
                        contentColor = textStyleColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF5F3FF), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "New Cart",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.translate("New Cart", "नया कार्ट"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textStyleColor
                        )
                    }
                }

                // Action 2: Scan Code
                Button(
                    onClick = { viewModel.currentScreen = "SCANNER" },
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                        .border(1.dp, borderColor, shape = RoundedCornerShape(24.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardBgColor,
                        contentColor = textStyleColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEFF6FF), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Code",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.translate("Scan Code", "कोड स्कैन"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textStyleColor
                        )
                    }
                }

                // Action 3: Stock Inventory
                Button(
                    onClick = { viewModel.currentScreen = "INVENTORY" },
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                        .border(1.dp, borderColor, shape = RoundedCornerShape(24.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardBgColor,
                        contentColor = textStyleColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFECFDF5), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = "Inventory",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.translate("Stock", "स्टॉक"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textStyleColor
                        )
                    }
                }
            }

            // Warning Banner for Low Stock (If active)
            if (lowStockState.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AlertAmber.copy(alpha = 0.08f), shape = RoundedCornerShape(20.dp))
                        .border(1.dp, AlertAmber.copy(alpha = 0.25f), shape = RoundedCornerShape(20.dp))
                        .clickable { viewModel.currentScreen = "INVENTORY" }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AlertAmber, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.translate("Low Stock Alert", "स्टॉक चेतावनी!"),
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${lowStockState.size} " + viewModel.translate("items are below minimum limits.", "सामग्रियां न्यूनतम सीमा से नीचे हैं।"),
                                color = if (isDark) Color.LightGray else Color.DarkGray,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Review",
                            tint = AlertAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // --- Weekly Sales Trend Line Graphic ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBgColor, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, borderColor, shape = RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = viewModel.translate("Weekly Sales Trend", "सापचारिक बिक्री का रुझान"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textStyleColor
                        )
                        Text(
                            text = "Mon - Sun Terminal Sync",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(EmeraldSuccess.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+12% VS LAST WK",
                            color = EmeraldSuccess,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val strokeColor = if (isDark) DarkAccentCyan else PrimaryBlue
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Baseline guides
                        drawLine(Color.Gray.copy(alpha = 0.1f), Offset(0f, height * 0.25f), Offset(width, height * 0.25f))
                        drawLine(Color.Gray.copy(alpha = 0.1f), Offset(0f, height * 0.5f), Offset(width, height * 0.5f))
                        drawLine(Color.Gray.copy(alpha = 0.1f), Offset(0f, height * 0.75f), Offset(width, height * 0.75f))

                        val points = listOf(0.15f, 0.45f, 0.38f, 0.72f, 0.52f, 0.88f, 0.95f)
                        val stepX = width / (points.size - 1)
                        val path = Path()

                        points.forEachIndexed { idx, value ->
                            val x = idx * stepX
                            val y = height - (value * height * 0.85f) - 6f
                            if (idx == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                            
                            drawCircle(
                                color = if (idx == points.size - 1) EmeraldSuccess else strokeColor,
                                radius = 12f,
                                center = Offset(x, y)
                            )
                        }

                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(width = 8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Text(day, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            // --- Recent Sales Activity / Fast Movers ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.translate("Recent Sales", "हाल ही की बिक्री"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textStyleColor
                )
                TextButton(
                    onClick = { viewModel.currentScreen = "BILLING" },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = viewModel.translate("View All", "सभी देखें"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) DarkAccentCyan else PrimaryBlue
                    )
                }
            }

            // Sales list
            if (ordersState.isNotEmpty()) {
                ordersState.take(3).forEach { order ->
                    val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(order.timestamp))
                    val customerDisplay = if (order.customerPhone.isNotEmpty()) "Cust: ${order.customerPhone}" else "Walk-in Customer"
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBgColor, shape = RoundedCornerShape(20.dp))
                            .border(1.dp, borderColor, shape = RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📄",
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customerDisplay,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textStyleColor
                                )
                                Text(
                                    text = "${order.orderId} • $formattedTime",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Light
                                )
                            }
                            Text(
                                text = "₹${"%.2f".format(order.finalAmount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            } else {
                // Fallbacks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBgColor, shape = RoundedCornerShape(20.dp))
                        .border(1.dp, borderColor, shape = RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFFF7ED), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍞", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Whole Wheat Bread", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textStyleColor)
                        Text("#INV-2041 • 2:14 PM", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text("₹45.00", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldSuccess)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBgColor, shape = RoundedCornerShape(20.dp))
                        .border(1.dp, borderColor, shape = RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFECFDF5), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧴", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hand Sanitizer Gel", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textStyleColor)
                        Text("#INV-2040 • 1:58 PM", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text("₹120.00", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldSuccess)
                }
            }
        }
    }
}
