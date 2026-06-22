package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout()
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val viewModel: HealthViewModel = viewModel()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val wearDevice by viewModel.connectedDevice.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PulseBottomNavigation(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
        ) {
            // High-Tech Top Vitals Header
            PulseTopHeader(wearDevice = wearDevice)

            // Dynamic layout selection
            when (currentScreen) {
                "dashboard" -> DashboardScreen(viewModel = viewModel)
                "activity" -> ActivityScreen(viewModel = viewModel)
                "sleep" -> SleepScreen(viewModel = viewModel)
                "nutrition" -> NutritionScreen(viewModel = viewModel)
                "vitals" -> VitalsScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PulseTopHeader(wearDevice: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "PULSE 3D",
                color = IceWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )
            Text(
                text = "SECURE CARDIO LABS",
                color = NeonCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Live pulsing device badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (wearDevice != null) NeonGreen.copy(alpha = 0.15f)
                        else NeonRed.copy(alpha = 0.12f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (wearDevice != null) NeonGreen else NeonRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (wearDevice != null) "WEARABLE LINKED" else "NO DEVICE",
                        color = if (wearDevice != null) NeonGreen else NeonRed,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PulseBottomNavigation(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavBarItem("dashboard", Icons.Default.Dashboard, "Home"),
        NavBarItem("activity", Icons.Default.DirectionsRun, "Activity"),
        NavBarItem("sleep", Icons.Default.NightsStay, "Sleep"),
        NavBarItem("nutrition", Icons.Default.Restaurant, "Ingests"),
        NavBarItem("vitals", Icons.Default.Favorite, "Vitals"),
        NavBarItem("settings", Icons.Default.Settings, "Config")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            val glowColor = when (item.screen) {
                "dashboard" -> NeonCyan
                "activity" -> NeonGreen
                "sleep" -> NeonCyan
                "nutrition" -> NeonPink
                "vitals" -> NeonRed
                else -> NeonGreen
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigate(item.screen) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("nav_tab_${item.screen}"),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) glowColor else SoftGrayText,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.label,
                    color = if (isSelected) IceWhite else SoftGrayText,
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

data class NavBarItem(
    val screen: String,
    val icon: ImageVector,
    val label: String
)
