package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = viewModel.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (viewModel.isDarkTheme) DarkBgMain else MaterialTheme.colorScheme.background
                ) {
                    ScanBillAppContainer(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ScanBillAppContainer(viewModel: MainViewModel) {
    val currentRoute = viewModel.currentScreen

    when (currentRoute) {
        "SPLASH" -> {
            SplashScreen(viewModel = viewModel)
        }
        "AUTH" -> {
            AuthScreen(viewModel = viewModel)
        }
        else -> {
            // Main Terminal Shell containing edge-to-edge Scaffold and Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(
                        containerColor = if (viewModel.isDarkTheme) DarkBgCard else Color.White,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    ) {
                        // Tab 1: Home Dashboard
                        NavigationBarItem(
                            selected = currentRoute == "DASHBOARD" || currentRoute == "ANALYTICS",
                            onClick = { viewModel.currentScreen = "DASHBOARD" },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text(viewModel.translate("Dashboard", "होम"), fontSize = 10.sp) },
                            colors = navigationBarItemColors(viewModel.isDarkTheme)
                        )

                        // Tab 2: Billing active cart
                        NavigationBarItem(
                            selected = currentRoute == "BILLING",
                            onClick = { viewModel.currentScreen = "BILLING" },
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Billing") },
                            label = { Text(viewModel.translate("Billing", "बिल"), fontSize = 10.sp) },
                            colors = navigationBarItemColors(viewModel.isDarkTheme)
                        )

                        // Tab 3: Interactive code scanner
                        NavigationBarItem(
                            selected = currentRoute == "SCANNER",
                            onClick = { viewModel.currentScreen = "SCANNER" },
                            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner") },
                            label = { Text(viewModel.translate("Scan", "स्कैन"), fontSize = 10.sp) },
                            colors = navigationBarItemColors(viewModel.isDarkTheme)
                        )

                        // Tab 4: Inventory tracking
                        NavigationBarItem(
                            selected = currentRoute == "INVENTORY",
                            onClick = { viewModel.currentScreen = "INVENTORY" },
                            icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                            label = { Text(viewModel.translate("Inventory", "स्टॉक"), fontSize = 10.sp) },
                            colors = navigationBarItemColors(viewModel.isDarkTheme)
                        )

                        // Tab 5: Customers & Contacts
                        NavigationBarItem(
                            selected = currentRoute == "CUSTOMERS",
                            onClick = { viewModel.currentScreen = "CUSTOMERS" },
                            icon = { Icon(Icons.Default.Group, contentDescription = "Customers") },
                            label = { Text(viewModel.translate("Clients", "ग्राहक"), fontSize = 10.sp) },
                            colors = navigationBarItemColors(viewModel.isDarkTheme)
                        )

                        // Tab 6: AI help, local terminal settings
                        NavigationBarItem(
                            selected = currentRoute == "SUPPORT",
                            onClick = { viewModel.currentScreen = "SUPPORT" },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text(viewModel.translate("Configs", "सेटिंग्स"), fontSize = 10.sp) },
                            colors = navigationBarItemColors(viewModel.isDarkTheme)
                        )
                    }
                },
                contentWindowInsets = WindowInsets.systemBars // Enforces Edge-To-Edge safe notch clipping
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentRoute) {
                        "DASHBOARD" -> DashboardScreen(viewModel = viewModel)
                        "BILLING" -> BillingScreen(viewModel = viewModel)
                        "SCANNER" -> ScannerScreen(viewModel = viewModel)
                        "INVENTORY" -> InventoryScreen(viewModel = viewModel)
                        "CUSTOMERS" -> CustomersScreen(viewModel = viewModel)
                        "SUPPORT" -> SettingsAndStaffScreen(viewModel = viewModel)
                        "ANALYTICS" -> AnalyticsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun navigationBarItemColors(isDark: Boolean): NavigationBarItemColors {
    val iconColor = if (isDark) DarkBgMain else PrimaryBlue
    val pillColor = if (isDark) DarkAccentCyan else LightAccentBlue
    return NavigationBarItemDefaults.colors(
        selectedIconColor = iconColor,
        unselectedIconColor = Color.Gray,
        selectedTextColor = if (isDark) DarkAccentCyan else PrimaryBlue,
        unselectedTextColor = Color.Gray,
        indicatorColor = pillColor
    )
}
