package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val ordersState by viewModel.orders.collectAsState()
    val productsState by viewModel.products.collectAsState()

    val isDark = viewModel.isDarkTheme
    val textStyleColor = if (isDark) Color.White else SecondaryNavy

    // Calculate core financial models
    val totalRevenue = ordersState.sumOf { it.finalAmount }
    val totalCostOfGoods = ordersState.fold(0.0) { accumulated, order ->
        val orderItemsCount = (order.subtotal / 50.0).coerceAtLeast(1.0).toInt() // simulation heuristic
        accumulated + (order.finalAmount * 0.72) // 72% average cost of goods
    }
    val grossProfit = (totalRevenue - totalCostOfGoods).coerceAtLeast(0.0)
    val averageProfitMarginPercent = if (totalRevenue > 0) (grossProfit / totalRevenue) * 100.0 else 24.5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBgMain else Color(0xFFF1F5F9))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp) // cushioned space above base navigator
    ) {
        // --- TITLE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = viewModel.translate("Revenue & Shop Intelligence", "राजस्व और दुकान विश्लेषिकी"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textStyleColor
                )
                Text(
                    text = viewModel.translate("In-store cost vs gross margin reports", "दुकान लागत बनाम लाभ मार्जिन"),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Icon(Icons.Default.QueryStats, contentDescription = null, tint = if (isDark) DarkAccentCyan else PrimaryBlue)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- CORE Margins card row ---
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(viewModel.translate("Profit & Cost Margin Index", "लाभ और लागत मार्जिन सूचकांक"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(viewModel.translate("Procurement Costs", "लागत मूल्य"), fontSize = 11.sp, color = Color.Gray)
                        Text("₹${"%.1f".format(totalCostOfGoods)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = textStyleColor)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(viewModel.translate("Gross Net Margin", "सकल शुद्ध मार्जिन"), fontSize = 11.sp, color = Color.Gray)
                        Text("${"%.1f".format(averageProfitMarginPercent)}%", fontWeight = FontWeight.Black, fontSize = 18.sp, color = EmeraldSuccess)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Visual horizontal stacked bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(72f) // 72% cost
                            .background(Color.Gray.copy(alpha = 0.5f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(28f) // 28% profit
                            .background(EmeraldSuccess)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cost of Goods", fontSize = 10.sp, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(EmeraldSuccess, shape = RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gross Profit", fontSize = 10.sp, color = EmeraldSuccess)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Category contribution shelves ---
        Text(
            text = viewModel.translate("Revenue Share by Segment", "श्रेणी के अनुसार बिक्री योगदान"),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = textStyleColor,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        val categoriesSales = listOf(
            Triple("Groceries", 65.0, EmeraldSuccess),
            Triple("Pharmacy", 20.0, PurpleUPI),
            Triple("Apparel", 10.0, OrangeCard),
            Triple("Electronics", 5.0, if (isDark) DarkAccentCyan else PrimaryBlue)
        )

        categoriesSales.forEach { (cat, share, color) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(color, shape = RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(cat, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textStyleColor)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${"%.1f".format(totalRevenue * (share / 100.0))} (${share.toInt()}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = textStyleColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- MAGICAL AI DISPATCHER STARRY PANEL FOR FORECASTS ---
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
            border = BorderStroke(1.5.dp, if (isDark) DarkAccentCyan.copy(alpha = 0.4f) else PrimaryBlue.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Glowy artificial title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SCANBILL AI BUSINESS COUNCIL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = viewModel.translate("AI Predictive Procurement & Restocks", "एआई प्रीडिक्टिव प्रोक्योरमेंट और रीस्टॉक"),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = textStyleColor
                )

                Text(
                    text = viewModel.translate("Analyze current shelving indices and project sales cycles instantly using server-side Gemini AI.", "Gemini AI का उपयोग करके तत्काल शेल्विंग और बिक्री पूर्वानुमानों का विश्लेषण करें।"),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // AI Response Text render
                if (viewModel.isInsightLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = PurpleUPI,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reading inventory parameters... Parsing trends via Gemini AI", fontSize = 11.sp, color = Color.Gray)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color.White.copy(alpha = 0.03f) else Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = viewModel.aiInsightText,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = textStyleColor,
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // gradient invoke button
                Button(
                    onClick = { viewModel.triggerAiForecast() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                            )
                        )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GENERATE SMART AI FORECASTS",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
