package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val wearDevice by viewModel.connectedDevice.collectAsState()
    
    // Goals configuration hooks
    val stepsGoalVal by viewModel.stepsGoal.collectAsState()
    val waterGoalVal by viewModel.waterGoal.collectAsState()
    val sleepGoalVal by viewModel.sleepGoal.collectAsState()
    val caloriesGoalVal by viewModel.caloriesGoal.collectAsState()

    var inputStepsGoal by remember { mutableStateOf(stepsGoalVal.toString()) }
    var inputWaterGoal by remember { mutableStateOf(waterGoalVal.toString()) }
    var inputSleepGoal by remember { mutableStateOf(sleepGoalVal.toString()) }
    var inputCalorieGoal by remember { mutableStateOf(caloriesGoalVal.toString()) }

    var statusMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Core header
        item {
            Text(
                text = "SYSTEM CONTROLS & DEVICES",
                color = SoftGrayText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // 1. Interactive Simulated Premium BLE Smartband Linker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp).testTag("wearable_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BLUETOOTH HEALTHBAND LINKAGE",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (wearDevice == null) {
                        Text(
                            text = "No biometric wearable linked. Activating BLE smartwatch pairs enables live cardiac loops and oxygen status feeds.",
                            color = IceWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { viewModel.connectWearable("FitPulse Band v2") },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("connect_fitpulse_button")
                            ) {
                                Text("FITPULSE v2", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = { viewModel.connectWearable("Cardio3D Watch") },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("connect_cardio3d_button")
                            ) {
                                Text("CARDIO3D WATCH", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = wearDevice!!.uppercase(),
                                    color = NeonGreen,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Connected via simulated secure BLE broadcast",
                                    color = SoftGrayText,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = { viewModel.disconnectWearable() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("disconnect_wearable_button")
                            ) {
                                Text("UNLINK", color = IceWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Custom Target Calibrations
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp).testTag("goals_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CALIBRATE VITAL TARGET GOALS",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = inputStepsGoal,
                            onValueChange = { inputStepsGoal = it },
                            label = { Text("Steps Target") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = SoftGrayText,
                                focusedTextColor = IceWhite,
                                unfocusedTextColor = IceWhite
                            ),
                            modifier = Modifier.weight(1f).testTag("steps_goal_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = inputWaterGoal,
                            onValueChange = { inputWaterGoal = it },
                            label = { Text("Water Target (ml)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = SoftGrayText,
                                focusedTextColor = IceWhite,
                                unfocusedTextColor = IceWhite
                            ),
                            modifier = Modifier.weight(1f).testTag("water_goal_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = inputSleepGoal,
                            onValueChange = { inputSleepGoal = it },
                            label = { Text("Sleep Target (hrs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = SoftGrayText,
                                focusedTextColor = IceWhite,
                                unfocusedTextColor = IceWhite
                            ),
                            modifier = Modifier.weight(1f).testTag("sleep_goal_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = inputCalorieGoal,
                            onValueChange = { inputCalorieGoal = it },
                            label = { Text("Calorie Target") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPink,
                                unfocusedBorderColor = SoftGrayText,
                                focusedTextColor = IceWhite,
                                unfocusedTextColor = IceWhite
                            ),
                            modifier = Modifier.weight(1f).testTag("calories_goal_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val steps = inputStepsGoal.toIntOrNull()
                            val water = inputWaterGoal.toIntOrNull()
                            val sleep = inputSleepGoal.toDoubleOrNull()
                            val cals = inputCalorieGoal.toIntOrNull()

                            if (steps != null && steps in 100..100000 &&
                                water != null && water in 100..10000 &&
                                sleep != null && sleep in 1.0..24.0 &&
                                cals != null && cals in 500..10000
                            ) {
                                viewModel.stepsGoal.value = steps
                                viewModel.waterGoal.value = water
                                viewModel.sleepGoal.value = sleep
                                viewModel.caloriesGoal.value = cals
                                statusMessage = "Goals updated successfully!"
                            } else {
                                statusMessage = "Error: Input realistic numeric ranges for all parameters!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_goals_button")
                    ) {
                        Text("SAVE TARGET GUIDELINES", color = DarkBg, fontWeight = FontWeight.Bold)
                    }

                    if (statusMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = statusMessage,
                            color = if (statusMessage.startsWith("Error")) NeonRed else NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. Permission Indicators (Interactive layout)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VITAL PERMISSIONS SYNCHRONY",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val permissions = listOf(
                        Triple("Cardiovascular Sensors", "BODY_SENSORS", true),
                        Triple("Pedometer Counter", "ACTIVITY_RECOGNITION", true),
                        Triple("Cardio Navigation GPS", "ACCESS_FINE_LOCATION", true),
                        Triple("Smart Alarm Banners", "POST_NOTIFICATIONS", true)
                    )

                    permissions.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = item.first, color = IceWhite, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("GRANTED", color = NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Room Database Eraser
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SAFETY SYSTEMS MAINTENANCE",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Wipes out all cached biometric history logs securely from localized Room SQLite DB.",
                        color = IceWhite,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.clearHistory()
                            statusMessage = "Biometric database successfully cleared!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp).border(1.dp, NeonRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).testTag("clear_history_button")
                    ) {
                        Text("REMOVE ROOM CACHES", color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
