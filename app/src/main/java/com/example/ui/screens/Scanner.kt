package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ScannerScreen(viewModel: MainViewModel) {
    val productsState by viewModel.products.collectAsState()
    val isDark = viewModel.isDarkTheme
    val textStyleColor = if (isDark) Color.White else SecondaryNavy

    var isFlashlightOn by remember { mutableStateOf(false) }
    var manualBarcodeEntry by remember { mutableStateOf("") }
    var scannerActiveResult by remember { mutableStateOf<String?>(null) }

    // Floating Scanner laser line animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    // Clear alert logic
    LaunchedEffect(viewModel.scannerFeedbackMessage) {
        val msg = viewModel.scannerFeedbackMessage
        if (msg != null) {
            scannerActiveResult = msg
            delay(3500)
            viewModel.scannerFeedbackMessage = null
            scannerActiveResult = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBgMain else Color(0xFFF1F5F9))
            .padding(16.dp)
            .padding(bottom = 60.dp), // cushion above toolbar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = viewModel.translate("Fast Camera Barcode Scan", "फास्ट कैमरा बारकोड स्कैन"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textStyleColor
                )
                Text(
                    text = viewModel.translate("Instant recognition POS decoder", "त्वरित पहचान पीओएस डिकोडर"),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { isFlashlightOn = !isFlashlightOn },
                modifier = Modifier.background(if (isFlashlightOn) EmeraldSuccess else Color.Gray.copy(alpha = 0.1f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = "Flashlight",
                    tint = if (isFlashlightOn) Color.White else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SCANNING VIEWFIDER EMULATOR CANVAS ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color.Black, shape = RoundedCornerShape(24.dp))
                .border(2.dp, if (isFlashlightOn) Color.Yellow else Color.DarkGray, shape = RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Simulated camera grainy grid patterns
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                        )
                    )
            )

            // Dynamic Scanning Box Overlay Frame
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 150.dp)
                    .border(
                        border = BorderStroke(2.dp, if (scannerActiveResult != null) EmeraldSuccess else (if (isDark) DarkAccentCyan else PrimaryBlue)),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                // Interactive Animated Laser Beam Scanning Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.015f)
                        .offset(y = 150.dp * laserOffsetY)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (scannerActiveResult != null) listOf(Color.Transparent, EmeraldSuccess, Color.Transparent) else listOf(Color.Transparent, Color.Red, Color.Transparent)
                            )
                        )
                )

                // Corner framing brackets
                Text(
                    "┲", color = if (isDark) Color.White else Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.TopStart).offset(x = 6.dp, y = 4.dp)
                )
                Text(
                    "┱", color = if (isDark) Color.White else Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-6).dp, y = 4.dp)
                )
                Text(
                    "┺", color = if (isDark) Color.White else Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.BottomStart).offset(x = 6.dp, y = (-4).dp)
                )
                Text(
                    "┹", color = if (isDark) Color.White else Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-6).dp, y = (-4).dp)
                )
            }

            // Text inside decoder layer
            Text(
                text = if (isFlashlightOn) "FLASHLIGHT: INJECTED MODE" else "CAMERA SCANNER READY",
                color = if (isFlashlightOn) Color.Yellow.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }

        // --- DECODED STATUS MESSAGE OVERLAY ---
        AnimatedVisibility(
            visible = scannerActiveResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.padding(top = 10.dp)
        ) {
            val resultColor = if (scannerActiveResult?.startsWith("Barcode Error") == true) ErrorCrimson else EmeraldSuccess
            Card(
                colors = CardDefaults.cardColors(containerColor = resultColor.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, resultColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (resultColor == ErrorCrimson) Icons.Default.Cancel else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = resultColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = scannerActiveResult ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else SecondaryNavy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- MANUAL BARCODE INPUT COMPOSABLE ---
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBgCard else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = viewModel.translate("Manual SKU / Barcode Entry", "मैनुअल बारकोड प्रविष्टि"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textStyleColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualBarcodeEntry,
                        onValueChange = { manualBarcodeEntry = it },
                        placeholder = { Text("e.g. 89012300001") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) DarkAccentCyan else PrimaryBlue
                        )
                    )

                    Button(
                        onClick = {
                            if (manualBarcodeEntry.trim().isNotEmpty()) {
                                viewModel.handleBarcodeScanned(manualBarcodeEntry.trim())
                                manualBarcodeEntry = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) DarkAccentCyan else PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 80.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- IN-EMULATOR TEST SHELF: CLICK TO SIMULATE BARCODE HIT ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emulator Tap-to-Scan Tray",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = textStyleColor
            )
            Text(
                text = "Emulator Helper (Click to Scan)",
                fontSize = 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(productsState) { product ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(150.dp)
                        .clickable { viewModel.handleBarcodeScanned(product.barcode) }
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Graphic representing physical product label barcode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(Color.White, shape = RoundedCornerShape(4.dp))
                                .border(1.dp, Color.LightGray)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Draw stylized lines simulating UPC barcode stripes
                                Row(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    listOf(2, 4, 1, 3, 1, 4, 2, 3, 1, 2).forEach { weight ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(weight.dp)
                                                .background(Color.Black)
                                        )
                                    }
                                }
                                Text(
                                    text = product.barcode.takeLast(7),
                                    fontSize = 8.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            color = textStyleColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "₹${product.sellingPrice}",
                            color = if (isDark) DarkAccentCyan else PrimaryBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
