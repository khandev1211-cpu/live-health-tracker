package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val liveHr by viewModel.liveHeartRate.collectAsState()
    val liveSpo2 by viewModel.liveSpO2.collectAsState()
    val stepCount by viewModel.stepCount.collectAsState()
    val wearDevice by viewModel.connectedDevice.collectAsState()
    val caloriesBurned by viewModel.liveCaloriesBurned.collectAsState()
    val currentSleepStage by viewModel.liveSleepStage.collectAsState()

    val logs by viewModel.allLogs.collectAsState()
    val smartAlerts by viewModel.smartAlerts.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()
    val isLoadingInsights by viewModel.isLoadingInsights.collectAsState()

    // Goals progress
    val stepsGoalValue by viewModel.stepsGoal.collectAsState()
    val stepsProgress = (stepCount.toFloat() / stepsGoalValue.coerceAtLeast(1)).coerceIn(0f, 1f)

    val waterGoalValue by viewModel.waterGoal.collectAsState()
    val waterLoggedToday = logs.filter { it.type == "WATER" && isToday(it.timestamp) }.sumOf { it.value1 }
    val waterProgress = (waterLoggedToday.toFloat() / waterGoalValue.coerceAtLeast(1)).coerceIn(0f, 1f)

    // Find the latest recorded heart rate in database
    val latestDbHr = remember(logs) {
        logs.firstOrNull { it.type == "HEART_RATE" }
    }

    val displayHr = if (wearDevice != null) {
        liveHr
    } else {
        latestDbHr?.value1?.toInt() ?: 0
    }

    val activeSource = if (wearDevice != null) {
        "WEARABLE"
    } else {
        latestDbHr?.notes ?: "NONE"
    }

    // Historical heart rates for live graph
    val hrHistory = remember(logs) {
        val lastHrs = logs.filter { it.type == "HEART_RATE" }.take(15).reversed().map { it.value1.toInt() }
        if (lastHrs.isEmpty()) listOf(72, 74, 71, 75, 78, 73, 72, 74) else lastHrs
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Wearable Connection Banner status
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (wearDevice != null) NeonGreen.copy(alpha = 0.08f) 
                        else DarkSurfaceCard
                    )
                    .border(
                        width = 1.dp,
                        color = if (wearDevice != null) NeonGreen.copy(alpha = 0.25f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (wearDevice != null) Icons.Default.Watch else Icons.Default.BluetoothDisabled,
                            contentDescription = "Wearable info",
                            tint = if (wearDevice != null) NeonGreen else SoftGrayText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (wearDevice != null) "WEARABLE CONNECTED" else "OFFLINE MODE (FALLBACK)",
                                color = if (wearDevice != null) NeonGreen else SoftGrayText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = wearDevice ?: "Connect a smart bracelet in settings for accurate biometrics.",
                                color = IceWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (wearDevice == null) {
                        Button(
                            onClick = { viewModel.navigateTo("settings") },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("LINK", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Immersive Central 3D Pulse Area
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Set orb color based on source confidence: Wearable (NeonGreen), PPG (NeonAmber), Tap (NeonRed), None (SoftGrayText)
                val statusColor = when (activeSource) {
                    "WEARABLE" -> NeonGreen
                    "CAMERA_PPG" -> NeonAmber
                    "FINGERPRINT_TAP" -> NeonRed
                    else -> SoftGrayText
                }

                Text(
                    text = "BIOMETRIC PULSE ENGINE",
                    color = SoftGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                LiveGlowingHeartOrb(
                    bpm = displayHr,
                    statusColor = statusColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Source trust badge below the central pulsating orb
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (activeSource) {
                                    "WEARABLE" -> NeonGreen.copy(alpha = 0.12f)
                                    "CAMERA_PPG" -> NeonAmber.copy(alpha = 0.12f)
                                    "FINGERPRINT_TAP" -> NeonRed.copy(alpha = 0.12f)
                                    else -> SoftGrayText.copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (activeSource) {
                                "WEARABLE" -> "🟢 HIGH CONFIDENCE: WEARABLE LIVE"
                                "CAMERA_PPG" -> "🟡 MEDIUM CONFIDENCE: CAMERA PPG SCAN"
                                "FINGERPRINT_TAP" -> "🔴 EXP CONFIDENCE: TAP-ASSIST WORKAROUND"
                                else -> "⚪ NO ACTIVE HEART PULSE FEED"
                            },
                            color = when (activeSource) {
                                "WEARABLE" -> NeonGreen
                                "CAMERA_PPG" -> NeonAmber
                                "FINGERPRINT_TAP" -> NeonRed
                                else -> SoftGrayText
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 3. Real-time Live Electrocardiogram Graph Block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "LIVE PPG CARDIAC WAVE",
                            color = SoftGrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "REAL-TIME",
                                color = NeonRed,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    RealtimePulseGraph(
                        dataPoints = hrHistory,
                        lineColor = if (smartAlerts.any { it.severity == "high" }) NeonRed else NeonCyan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }
        }

        // 4. Circular 3D Progress Rings for Goals
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LiveGoalRing3D(progress = stepsProgress, ringColor = NeonGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Daily Steps", color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("$stepCount / $stepsGoalValue", color = SoftGrayText, fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LiveGoalRing3D(progress = waterProgress, ringColor = NeonCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Hydration", color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${waterLoggedToday.toInt()} / ${waterGoalValue}mL", color = SoftGrayText, fontSize = 10.sp)
                }
            }
        }

        // 5. Smart Alerts Panel
        if (smartAlerts.isNotEmpty()) {
            item {
                Text(
                    text = "CRITICAL METRIC OBSERVATIONS (${smartAlerts.size})",
                    color = SoftGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(smartAlerts) { alert ->
                val cardGlow = when (alert.severity) {
                    "high" -> NeonRed
                    "medium" -> NeonAmber
                    else -> NeonCyan
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardGlow.copy(alpha = 0.05f))
                        .border(1.dp, cardGlow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = when (alert.severity) {
                                "high" -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            },
                            contentDescription = "Alert Indicator",
                            tint = cardGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = alert.title.uppercase(),
                                color = cardGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = alert.message,
                                color = IceWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 6. Gemini Smart AI Health Insights Board
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(NeonCyan.copy(alpha = 0.04f), Color.Transparent)
                            )
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI symbol",
                                tint = NeonCyan,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GEMINI PATTERN INSIGHTS",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Clinical Neural Advisor",
                                    color = SoftGrayText,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.fetchAiInsights() },
                            enabled = !isLoadingInsights,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            if (isLoadingInsights) {
                                CircularProgressIndicator(
                                    color = DarkBg,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("ANALYZE", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBg)
                            .border(1.dp, NeonCyan.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = aiInsights,
                            color = IceWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

private fun isToday(timeMs: Long): Boolean {
    val sdf = SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timeMs)) == sdf.format(java.util.Date())
}
