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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ActivityScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val stepCount by viewModel.stepCount.collectAsState()
    val ambientLight by viewModel.ambientLight.collectAsState()
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsState()
    val caloriesBurned by viewModel.liveCaloriesBurned.collectAsState()
    
    // Calculate distance derived from steps (approx 0.75m per step)
    val distanceKm = (stepCount * 0.00075)
    
    // Workout simulator logs
    var workoutSeconds by remember { mutableStateOf(0) }
    val workoutPathPoints = remember { mutableStateListOf<Pair<Double, Double>>() }

    // Simulated workout timer & path incrementor
    LaunchedEffect(isWorkoutActive) {
        if (isWorkoutActive) {
            // Start at a standard San Francisco coordinate
            var lat = 37.7749
            var lon = -122.4194
            while (isWorkoutActive) {
                delay(1000)
                workoutSeconds++
                // Random walk simulated GPS track path
                lat += ((-15..15).random() * 0.00005)
                lon += ((-15..15).random() * 0.00005)
                workoutPathPoints.add(Pair(lat, lon))
            }
        } else {
            workoutSeconds = 0
            workoutPathPoints.clear()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Core metrics header
        item {
            Text(
                text = "EXERCISE & TELEMETRY",
                color = SoftGrayText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Parallax metrics grid
        item {
            Column {
                ParallaxMetricCard(
                    title = "Today's Steps",
                    value = String.format("%,d", stepCount),
                    unit = "steps",
                    icon = { Icon(Icons.Default.DirectionsRun, "steps", tint = NeonGreen) },
                    glowColor = NeonGreen,
                    testTag = "steps_status_card"
                )
                
                ParallaxMetricCard(
                    title = "Estimated Distance",
                    value = String.format("%.2f", distanceKm),
                    unit = "km",
                    icon = { Icon(Icons.Default.Map, "distance", tint = NeonCyan) },
                    glowColor = NeonCyan,
                    testTag = "distance_status_card"
                )

                ParallaxMetricCard(
                    title = "Energy Expenditure",
                    value = String.format("%.1f", caloriesBurned),
                    unit = "kcal",
                    icon = { Icon(Icons.Default.LocalFireDepartment, "calories", tint = NeonPink) },
                    glowColor = NeonPink,
                    testTag = "calories_status_card"
                )

                ParallaxMetricCard(
                    title = "Ambient Lux Sensor",
                    value = String.format("%.0f", ambientLight),
                    unit = "lux",
                    icon = { Icon(Icons.Default.LightMode, "light", tint = NeonAmber) },
                    glowColor = NeonAmber,
                    testTag = "light_status_card"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Circadian screen-time tip based on ambient light
        item {
            val circadianAdvice = when {
                ambientLight < 15f -> "Dim ambient light filters active. Optimal for natural melatonin synthesis!"
                ambientLight > 600f -> "Great screen visibility. High lux environment suppresses digital fatigue."
                else -> "Moderate office environment light level logs."
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonAmber.copy(alpha = 0.05f))
                    .border(1.dp, NeonAmber.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Circadian Warning",
                        tint = NeonAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = circadianAdvice,
                        color = IceWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Workout active tracker module
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("workout_control_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACTIVE ROUTE TRACKING (GPS)",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isWorkoutActive) {
                        // Inactive workout banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "GPS Satellites",
                                    tint = SoftGrayText,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ready to Sync Active GPS Trails",
                                    color = SoftGrayText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.toggleWorkout() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, "start workout", tint = DarkBg)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "START ACTIVE CARDIO",
                                    color = DarkBg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Active Workout Panel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val hrVal by viewModel.liveHeartRate.collectAsState()
                                Text("WORKOUT TIME", color = SoftGrayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format("%02d:%02d", workoutSeconds / 60, workoutSeconds % 60),
                                    color = NeonGreen,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("Live telemetry active", color = SoftGrayText, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val hrVal by viewModel.liveHeartRate.collectAsState()
                                Text("CARDIO LOAD", color = SoftGrayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "$hrVal BPM",
                                    color = NeonRed,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("ACCELERATED HR", color = NeonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Render active real-time GPS pathway coordinates log
                        Text(
                            text = "LIVE SATELLITE GPS LOGS",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkBg)
                                .border(1.dp, NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            if (workoutPathPoints.isEmpty()) {
                                Text(
                                    text = "Acquiring locking signals...",
                                    color = SoftGrayText,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column {
                                        Text(
                                            text = "🛰️ San Francisco active grid coords:",
                                            color = SoftGrayText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            items(workoutPathPoints.asReversed().take(6)) { pt ->
                                                Text(
                                                    text = "📍 Lat: ${String.format("%.6f", pt.first)}, Lon: ${String.format("%.6f", pt.second)}",
                                                    color = IceWhite,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeonCyan.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("+ GPS TRACING", color = NeonCyan, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.toggleWorkout() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stop, "stop workout", tint = IceWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FINISH EXERCISE SESSION",
                                    color = IceWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
