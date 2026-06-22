package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SleepScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.allLogs.collectAsState()
    val wearDevice by viewModel.connectedDevice.collectAsState()
    val liveSleepState by viewModel.liveSleepStage.collectAsState()
    val sleepGoalHours by viewModel.sleepGoal.collectAsState()

    // Retrieve manual and simulated records
    val sleepLogs = remember(logs) {
        logs.filter { it.type == "SLEEP" }
    }

    val latestSleepDuration = sleepLogs.firstOrNull()?.value1 ?: 7.5
    val latestSleepQuality = sleepLogs.firstOrNull()?.value2?.toInt() ?: 84

    // Input state for manual card logs
    var inputHours by remember { mutableStateOf("") }
    var inputQuality by remember { mutableStateOf("80") } // default%
    var logStatusMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Sleep header
        item {
            Text(
                text = "SLEEP METRIC LOGS",
                color = SoftGrayText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // 1. Wearable Live State Flow Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE SLEEP CYCLE PROGRESSION",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (wearDevice != null) liveSleepState.uppercase() else "OFFLINE FALLBACK",
                                color = if (wearDevice != null) NeonCyan else SoftGrayText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (wearDevice != null) "Real-time wearable telemetry stream" else "Connect wearable to stream sleep status",
                                color = SoftGrayText,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = "Sleep Tracking",
                            tint = NeonCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // 2. Beautiful 3D Sleep Stages Canvas breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OPTIMAL SLEEP STAGES PERCENTAGES",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        val w = size.width
                        val h = size.height

                        // Draw segmented colored bars: Deep (25%), REM (15%), Light (50%), Awake (10%)
                        val deepW = w * 0.25f
                        val remW = w * 0.15f
                        val lightW = w * 0.50f
                        val awakeW = w * 0.10f

                        var currentX = 0f

                        // Deep sleep
                        drawRect(
                            color = Color(0xFF1F438A),
                            size = Size(deepW, h),
                            topLeft = Offset(currentX, 0f)
                        )
                        currentX += deepW

                        // REM
                        drawRect(
                            color = NeonPink,
                            size = Size(remW, h),
                            topLeft = Offset(currentX, 0f)
                        )
                        currentX += remW

                        // Light
                        drawRect(
                            color = NeonCyan,
                            size = Size(lightW, h),
                            topLeft = Offset(currentX, 0f)
                        )
                        currentX += lightW

                        // Awake
                        drawRect(
                            color = NeonAmber,
                            size = Size(awakeW, h),
                            topLeft = Offset(currentX, 0f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Labels Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF1F438A)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deep (25%)", color = IceWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(NeonPink))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REM (15%)", color = IceWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(NeonCyan))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Light (50%)", color = IceWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(NeonAmber))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Awake (10%)", color = IceWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Manual entry logger form card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("manual_sleep_logger_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MANUALLY LOG RETROACTIVE SLEEP",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inputHours,
                        onValueChange = { inputHours = it },
                        label = { Text("Hours Slept (e.g. 7.5)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SoftGrayText,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = SoftGrayText,
                            focusedTextColor = IceWhite,
                            unfocusedTextColor = IceWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("sleep_hours_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputQuality,
                        onValueChange = { inputQuality = it },
                        label = { Text("Sleep Quality Percentage (e.g. 1-100)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SoftGrayText,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = SoftGrayText,
                            focusedTextColor = IceWhite,
                            unfocusedTextColor = IceWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("sleep_quality_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val hours = inputHours.toDoubleOrNull()
                            val quality = inputQuality.toDoubleOrNull()
                            if (hours != null && hours in 0.5..24.0 && quality != null && quality in 1.0..100.0) {
                                viewModel.logSleep(hours, quality.toInt())
                                logStatusMessage = "Sleep session successfully logged in database!"
                                inputHours = ""
                                inputQuality = "80"
                            } else {
                                logStatusMessage = "Error: Input realistic hours (0.5 to 24) and quality percentages (1 to 100)!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("log_sleep_button")
                    ) {
                        Text("SAVE SLEEP RECORD", color = DarkBg, fontWeight = FontWeight.Bold)
                    }

                    if (logStatusMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = logStatusMessage,
                            color = if (logStatusMessage.startsWith("Error")) NeonRed else NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. Room Database logs list history
        if (sleepLogs.isNotEmpty()) {
            item {
                Text(
                    text = "HISTORICAL SLEEP ENCOUNTERS (" + sleepLogs.size + ")",
                    color = SoftGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(sleepLogs) { log ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, NeonCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bedtime, "sleep", tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${log.value1} Hours Slept",
                                    color = IceWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateStr,
                                color = SoftGrayText,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${log.value2.toInt()}%",
                                color = NeonGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "QUALITY",
                                color = SoftGrayText,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
