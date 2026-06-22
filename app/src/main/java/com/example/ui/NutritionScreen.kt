package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
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
import kotlin.math.sin

@Composable
fun NutritionScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.allLogs.collectAsState()
    val calorieGoalValue by viewModel.caloriesGoal.collectAsState()

    // Query daily food and drink totals
    val waterLogs = remember(logs) { logs.filter { it.type == "WATER" } }
    val foodLogs = remember(logs) { logs.filter { it.type == "CALORIES_IN" } }

    val waterTotalToday = waterLogs.filter { isToday(it.timestamp) }.sumOf { it.value1 }
    val caloriesTotalToday = foodLogs.filter { isToday(it.timestamp) }.sumOf { it.value1 }

    // Input state
    var mealName by remember { mutableStateOf("") }
    var mealCalories by remember { mutableStateOf("") }
    var loggingStatusMsg by remember { mutableStateOf("") }

    // Wave animation for the water container
    val infiniteTransition = rememberInfiniteTransition(label = "water_waves")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "wave_offset"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Section Header
        item {
            Text(
                text = "HYDRATION & NUTRITION TRACKER",
                color = SoftGrayText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // 1. Interactive 3D Water Bottle Canvas Block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HYDRATION CYLINDER FLUID VOLUME",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Custom Canvas-driven water tube
                        val maxCapacity = 3000.0 // 3L
                        val waterFillPct = (waterTotalToday / maxCapacity).coerceIn(0.0, 1.0).toFloat()

                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(160.dp)
                                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp, topStart = 10.dp, topEnd = 10.dp))
                                .background(DarkBg)
                                .border(2.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp, topStart = 10.dp, topEnd = 10.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw vertical water height
                                val waterHeight = h * waterFillPct
                                val waterTopY = h - waterHeight

                                if (waterHeight > 0) {
                                    val path = androidx.compose.ui.graphics.Path()
                                    path.moveTo(0f, waterTopY)

                                    // Draw animating sine wave on liquid surface
                                    for (x in 0..w.toInt() step 5) {
                                        val waveY = waterTopY + 8f * sin(x.toFloat() * 0.08f + waveOffset)
                                        path.lineTo(x.toFloat(), waveY)
                                    }
                                    path.lineTo(w, h)
                                    path.lineTo(0f, h)
                                    path.close()

                                    drawPath(
                                        path = path,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(NeonCyan, Color(0xFF00557F))
                                        )
                                    )
                                }
                            }

                            // Cylinder overlay text
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${waterTotalToday.toInt()}",
                                    color = IceWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "mL / 3L",
                                    color = SoftGrayText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Water action loggers
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = { viewModel.logWater(250.0) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.width(110.dp).testTag("add_water_250ml")
                            ) {
                                Text("+ 250 mL", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.logWater(500.0) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.width(110.dp).testTag("add_water_500ml")
                            ) {
                                Text("+ 500 mL", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "Ideal water target is: 2.5L",
                                color = SoftGrayText,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Meal journal adder form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("nutrition_logger_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LOG MEAL DIARY",
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = mealName,
                        onValueChange = { mealName = it },
                        label = { Text("Food Description (e.g. Avocado Toast)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = SoftGrayText,
                            focusedLabelColor = NeonPink,
                            unfocusedLabelColor = SoftGrayText,
                            focusedTextColor = IceWhite,
                            unfocusedTextColor = IceWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("meal_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = mealCalories,
                        onValueChange = { mealCalories = it },
                        label = { Text("Energy Content (Calories kCal)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = SoftGrayText,
                            focusedLabelColor = NeonPink,
                            unfocusedLabelColor = SoftGrayText,
                            focusedTextColor = IceWhite,
                            unfocusedTextColor = IceWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("meal_calories_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress indicators
                    val caloriesProgress = (caloriesTotalToday.toFloat() / calorieGoalValue.coerceAtLeast(1)).coerceIn(0f, 1.2f)
                    LinearProgressIndicator(
                        progress = { caloriesProgress },
                        color = if (caloriesTotalToday > calorieGoalValue) NeonRed else NeonPink,
                        trackColor = DarkBg,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Today: ${caloriesTotalToday.toInt()} kCal",
                            color = if (caloriesTotalToday > calorieGoalValue) NeonRed else NeonPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target: ${calorieGoalValue} kCal",
                            color = SoftGrayText,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val cals = mealCalories.toDoubleOrNull()
                            if (mealName.isNotBlank() && cals != null && cals in 1.0..6000.0) {
                                viewModel.logMeal(cals, mealName)
                                loggingStatusMsg = "Logged food successfully!"
                                mealName = ""
                                mealCalories = ""
                            } else {
                                loggingStatusMsg = "Error: Input valid meal title and calories (1 to 6000 kCal)!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("log_meal_button")
                    ) {
                        Text("SAVE DIARY LOG", color = IceWhite, fontWeight = FontWeight.Bold)
                    }

                    if (loggingStatusMsg.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = loggingStatusMsg,
                            color = if (loggingStatusMsg.startsWith("Error")) NeonRed else NeonGreen,
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

        // 3. Dinner/Breakfast logs history lists
        if (foodLogs.isNotEmpty()) {
            item {
                Text(
                    text = "HISTORICAL NUTRITIONAL JOURNAL (" + foodLogs.size + ")",
                    color = SoftGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(foodLogs) { log ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, NeonPink.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Restaurant, "meal", tint = NeonPink, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.notes,
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

                        Text(
                            text = "+${log.value1.toInt()} kCal",
                            color = NeonPink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
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
