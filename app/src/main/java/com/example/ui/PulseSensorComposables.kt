package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import kotlin.math.abs

/**
 * Dialog overlay to perform Camera-based PPG Heart Rate Analysis
 */
@Composable
fun CameraPpgMeasurementDialog(
    onDismiss: () -> Unit,
    onLogReading: (bpm: Int) -> Unit
) {
    val context = LocalContext.current
    val analyzer = remember { CameraPpgAnalyzer(context) }
    
    val isMeasuring by analyzer.isMeasuring.collectAsState()
    val progress by analyzer.progress.collectAsState()
    val liveWaveform by analyzer.liveWaveform.collectAsState()
    val measuredBpm by analyzer.measuredBpm.collectAsState()
    val signalQuality by analyzer.signalQuality.collectAsState()
    val isSimulationMode by analyzer.isSimulationMode.collectAsState()
    val errorMessage by analyzer.errorMessage.collectAsState()

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
        if (granted) {
            analyzer.startMeasurement()
        } else {
            Toast.makeText(context, "Camera permission required for PPG heart-rate sensing", Toast.LENGTH_LONG).show()
        }
    }

    // Safety release: Always stop camera stream if user dismisses or exits composable
    DisposableEffect(analyzer) {
        onDispose {
            analyzer.stopMeasurement()
        }
    }

    Dialog(
        onDismissRequest = {
            analyzer.stopMeasurement()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                .background(DarkBg)
                .padding(20.dp),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Camera sense",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CAMERA PPG LABORATORY",
                            color = IceWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(
                        onClick = {
                            analyzer.stopMeasurement()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("dismiss_camera_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SoftGrayText)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High-fidelity Accuracy Ranking Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonAmber.copy(alpha = 0.08f))
                        .border(1.dp, NeonAmber.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonAmber)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("MEDIUM ACCURACY", color = DarkBg, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🟡 Subject to ambient lights and finger tremors.",
                            color = IceWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = NeonRed,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (isSimulationMode) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔬 CHROMATIC SIMULATOR ACTIVE (EMULATOR FALLBACK)",
                            color = NeonCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Waveform rendering space (The Holographic Oscilloscope)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .border(1.dp, NeonCyan.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isMeasuring && liveWaveform.isNotEmpty()) {
                            // Render raw PPG waveform line
                            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                val width = size.width
                                val height = size.height
                                val points = liveWaveform.takeLast(60)

                                if (points.size > 1) {
                                    val stepX = width / (points.size - 1)
                                    val path = Path()

                                    for (i in points.indices) {
                                        val x = i * stepX
                                        // Center line is mid height
                                        // points vary from roughly -1.0 to 1.5, map them carefully
                                        val rawVal = points[i]
                                        val mappedY = height / 2f - (rawVal * (height * 0.35f))
                                        val finalY = mappedY.coerceIn(5f, height - 5f)

                                        if (i == 0) {
                                            path.moveTo(x, finalY)
                                        } else {
                                            path.lineTo(x, finalY)
                                        }
                                    }

                                    drawPath(
                                        path = path,
                                        color = if (isSimulationMode) NeonCyan else NeonRed,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }
                        } else if (measuredBpm != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Perfect Heart",
                                    tint = NeonRed,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${measuredBpm} BPM",
                                    color = IceWhite,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Signal Integrity: $signalQuality",
                                    color = if (signalQuality.contains("Poor")) NeonRed else NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "SENSOR READY",
                                    color = SoftGrayText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap START and place index finger fully covering the rear camera lens.",
                                    color = SoftGrayText,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                if (isMeasuring) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Capturing blood pulses...",
                                color = SoftGrayText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            color = NeonCyan,
                            trackColor = DarkSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Control Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isMeasuring && measuredBpm == null) {
                        Button(
                            onClick = {
                                if (cameraPermissionGranted) {
                                    analyzer.startMeasurement()
                                } else {
                                    launcher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("start_camera_measure")
                        ) {
                            Text("START ANALYSIS", color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                    } else if (isMeasuring) {
                        Button(
                            onClick = { analyzer.stopMeasurement() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp).border(1.dp, NeonRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp)).testTag("stop_camera_measure")
                        ) {
                            Text("ABORT SENSING", color = NeonRed, fontWeight = FontWeight.Bold)
                        }
                    } else if (measuredBpm != null) {
                        Button(
                            onClick = {
                                analyzer.startMeasurement()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("RE-MEASURE", color = IceWhite, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onLogReading(measuredBpm!!)
                                Toast.makeText(context, "Sensed rate of $measuredBpm BPM logged successfully!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f).height(46.dp).testTag("save_camera_measure")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = DarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LOG VITALS", color = DarkBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * Experimental Fingerprint Touched Pulsing calibrator dialog
 */
@Composable
fun FingerprintPulseTapDialog(
    onDismiss: () -> Unit,
    onLogReading: (bpm: Int) -> Unit
) {
    val context = LocalContext.current
    val tapTimestamps = remember { mutableStateListOf<Long>() }
    
    // Calculate live estimation
    val instantBpm = remember(tapTimestamps.size) {
        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                val diff = tapTimestamps[i] - tapTimestamps[i - 1]
                if (diff in 350..1500) { // filter outliers representing double taps or 2s pauses
                    intervals.add(diff)
                }
            }
            if (intervals.isNotEmpty()) {
                val avgInterval = intervals.average()
                (60000.0 / avgInterval).toInt().coerceIn(50, 150)
            } else null
        } else null
    }

    // Measure timing consistency (variance / jitter)
    val tapStability = remember(tapTimestamps.size) {
        if (tapTimestamps.size >= 4) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                intervals.add(tapTimestamps[i] - tapTimestamps[i - 1])
            }
            val avg = intervals.average()
            val variance = intervals.map { abs(it - avg) }.average()
            
            // Map variance to stability percentage (where 0ms jitter is 100%, 150ms variance is 0%)
            val stability = (100.0 - (variance / 1.5)).coerceIn(0.0, 100.0).toInt()
            stability
        } else null
    }

    // Simulated scanner feedback
    var scannerPulsesScale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scannerPulsesScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        finishedListener = { scannerPulsesScale = 1f },
        label = "scanner_press"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, NeonPink.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                .background(DarkBg)
                .padding(20.dp),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Tap sense",
                            tint = NeonPink,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "FINGERPRINT PULSE alignment",
                            color = IceWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_fingerprint_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SoftGrayText)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // HIGHLY PROMINENT CHROMATIC ACCURACY WARNING (RED RANK)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonRed.copy(alpha = 0.08f))
                        .border(1.dp, NeonRed.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LOW ACCURACY / EXPERIMENTAL", color = IceWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔴 Manual-Assist Bypass",
                                color = NeonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "Android does not expose raw biometric capacitance or pressure indexes through public BiometricPrompt modules. This feature operates as a manual touch synchronization calibration module.",
                            color = SoftGrayText,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Diagnostic feedback details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (instantBpm != null) {
                        Text(
                            text = "$instantBpm BPM",
                            color = NeonPink,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("fingerprint_live_bpm")
                        )
                        
                        if (tapStability != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Timing Stability: $tapStability% " + 
                                       (if (tapStability > 80) " (Good/Regular)" else " (Irregular/Varying)"),
                                color = if (tapStability > 80) NeonGreen else NeonAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Calibrating pulses: ${tapTimestamps.size} taps recorded",
                            color = SoftGrayText,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "AWAITING SYNCHRONY",
                            color = SoftGrayText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Begin tapping the sensor below to calibrate intervals...",
                            color = SoftGrayText,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // The Big central glowing biometric touching pod (Haptic Fingerprint card)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(NeonPink.copy(alpha = 0.15f * animatedScale), Color.Transparent)
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(NeonPink, Color.Transparent, NeonPink.copy(alpha = 0.5f), NeonPink)
                            ),
                            shape = CircleShape
                        )
                        .clickable {
                            scannerPulsesScale = 1.25f
                            tapTimestamps.add(System.currentTimeMillis())
                            // limit to last 20 timestamps
                            if (tapTimestamps.size > 20) {
                                tapTimestamps.removeAt(0)
                            }
                        }
                        .testTag("fingerprint_sensor_pad")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Sensor scanner",
                        tint = if (instantBpm != null) NeonPink else SoftGrayText,
                        modifier = Modifier.size(72.dp * animatedScale)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Guide texts on inconsistent tap rhythm
                if (tapStability != null && tapStability < 70) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonAmber.copy(alpha = 0.12f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ TAP RHYTHM GUIDE: To maintain accurate BPM reading loops, tap steadily matching the exact pulse of your wrist artery.",
                            color = NeonAmber,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Control Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            tapTimestamps.clear()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .border(1.dp, SoftGrayText.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    ) {
                        Text("RESET TAPS", color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (instantBpm != null && instantBpm in 40..160) {
                                onLogReading(instantBpm)
                                Toast.makeText(context, "Calibrated tap pulse of $instantBpm BPM logged successfully!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Please perform at least 5 steady taps first!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = instantBpm != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            disabledContainerColor = DarkSurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("save_fingerprint_measure")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Log", tint = if (instantBpm != null) DarkBg else SoftGrayText, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LOG READING", color = if (instantBpm != null) DarkBg else SoftGrayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
