package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Live 3D Glowing pulse core representing health state.
 * Pulses in frequency matching the user's live Heart-Rate BPM.
 */
@Composable
fun LiveGlowingHeartOrb(
    bpm: Int,
    modifier: Modifier = Modifier,
    statusColor: Color = NeonGreen
) {
    // Dynamically calculate pulse duration based on current BPM
    val pulseDuration = if (bpm > 0) (60000 / bpm).coerceIn(350, 1500) else 1000

    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse_scale"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_rotation"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .testTag("glowing_heart_orb"),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Background Shadow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2.0f) * 0.7f

            // Pulsing cyber-sphere gradients
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        statusColor.copy(alpha = 0.45f * pulseScale),
                        statusColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.5f
                ),
                radius = radius * 1.5f
            )

            // Inner volumetric metal/glass rim
            drawCircle(
                color = DarkBg,
                radius = radius,
                center = center
            )

            // Dynamic sine grid (representing holographic 3D lines)
            val path = Path()
            val pointsCount = 60
            val width = radius * 2
            val startX = center.x - radius
            val startY = center.y

            for (i in 0..pointsCount) {
                val fraction = i.toFloat() / pointsCount
                val px = startX + fraction * width
                // check boundaries to keep lines within circle limits
                val dx = px - center.x
                val limitY = sqrt((radius * radius - dx * dx).coerceAtLeast(0f))
                
                val waveHeight = 12f * pulseScale * sin(fraction * 4 * PI.toFloat() + waveOffset)
                val py = (startY + waveHeight).coerceIn(center.y - limitY, center.y + limitY)

                if (i == 0) {
                    path.moveTo(px, py)
                } else {
                    path.lineTo(px, py)
                }
            }

            drawPath(
                path = path,
                color = statusColor.copy(alpha = 0.75f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Glowing Outer Hologram Orbit
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        statusColor,
                        Color.Transparent,
                        statusColor.copy(alpha = 0.5f),
                        statusColor
                    )
                ),
                radius = radius * pulseScale,
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), waveOffset * 10f)),
                center = center
            )
        }

        // Central Heart Rate Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (bpm > 0) bpm.toString() else "--",
                color = IceWhite,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "BPM",
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Volumetric Circular 3D progress ring representing daily goals.
 */
@Composable
fun LiveGoalRing3D(
    progress: Float, // 0.0f to 1.0f+
    modifier: Modifier = Modifier,
    ringColor: Color = NeonGreen,
    strokeWidth: Dp = 12.dp
) {
    val animatedProgress = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 2f),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "goal_ring_fill"
    )

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - strokeWidth.toPx()

            // 1. Shadow Underlay
            drawCircle(
                color = ringColor.copy(alpha = 0.08f),
                radius = radius,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // 2. Neon Glow Arc
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(ringColor.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = radius * 1.3f
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress.value * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx() * 1.5f, cap = StrokeCap.Round)
            )

            // 3. Main Colorful Filled Arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedProgress.value * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Inner Percentage Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress * 100).toInt()}%",
                color = IceWhite,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "GOAL",
                color = SoftGrayText,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Metric card with physical 3D elevation and neon border hover/glowing effect.
 */
@Composable
fun ParallaxMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: @Composable () -> Unit,
    glowColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Smooth physical hover parallax lifting translation
    val offsetTranslation by animateDpAsState(
        targetValue = if (isHovered) (-4).dp else 0.dp,
        animationSpec = tween(200, easing = LinearOutSlowInEasing),
        label = "card_displacement"
    )

    val borderGlowGleam by animateFloatAsState(
        targetValue = if (isHovered) 1f else 0.2f,
        animationSpec = tween(500),
        label = "border_gleam"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .offset(y = offsetTranslation)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceCard
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            hoveredElevation = 10.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Render subtle 3D left neon-line glowing edge
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(glowColor, Color.Transparent)
                        ),
                        size = Size(width = 4.dp.toPx(), height = size.height),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    // Volumetric corner ambient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = 0.08f * borderGlowGleam), Color.Transparent),
                            center = Offset(size.width, 0f),
                            radius = size.width * 0.4f
                        ),
                        radius = size.width * 0.4f,
                        center = Offset(size.width, 0f)
                    )
                }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.uppercase(),
                        color = SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            color = IceWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            color = glowColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(glowColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }
        }
    }
}

/**
 * Animated heart rate or step-count live electrocardiogram graph.
 */
@Composable
fun RealtimePulseGraph(
    dataPoints: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = NeonRed
) {
    val infiniteTransition = rememberInfiniteTransition(label = "graph_scroll")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "graph_phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Base cardiac grids
        val spacingY = height / 5f
        for (i in 0..5) {
            drawLine(
                color = lineColor.copy(alpha = 0.05f),
                start = Offset(0f, i * spacingY),
                end = Offset(width, i * spacingY),
                strokeWidth = 1.dp.toPx()
            )
        }

        val paddingX = 10.dp.toPx()
        val renderedPoints = dataPoints.takeLast(15)
        if (renderedPoints.size < 2) {
            // Draw baseline if insufficient data
            val midY = height / 2f
            drawLine(
                color = lineColor.copy(alpha = 0.4f),
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        val maxVal = (renderedPoints.maxOrNull() ?: 100).coerceAtLeast(100).toFloat()
        val minVal = (renderedPoints.minOrNull() ?: 50).coerceAtMost(50).toFloat()
        val deltaVal = (maxVal - minVal).coerceAtLeast(1f)

        val spacingX = (width - paddingX * 2) / (renderedPoints.size - 1)
        val path = Path()

        for (i in renderedPoints.indices) {
            val rawVal = renderedPoints[i]
            val pct = (rawVal - minVal) / deltaVal
            // Invert Y so higher is up
            val px = paddingX + i * spacingX
            val py = height - paddingX - pct * (height - paddingX * 2)

            if (i == 0) {
                path.moveTo(px, py)
            } else {
                // Draw bezier sweep curves
                val prevPx = paddingX + (i - 1) * spacingX
                val prevPy = height - paddingX - ((renderedPoints[i - 1] - minVal) / deltaVal) * (height - paddingX * 2)
                path.cubicTo(
                    (prevPx + px) / 2f, prevPy,
                    (prevPx + px) / 2f, py,
                    px, py
                )
            }
        }

        // 1. Draw glowing background shadow
        val fillPath = Path().apply {
            addPath(path)
            lineTo(paddingX + (renderedPoints.size - 1) * spacingX, height)
            lineTo(paddingX, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.15f), Color.Transparent)
            )
        )

        // 2. Draw actual main electrocardiogram curve
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Highlight newest active leading pulse node
        val lastIdx = renderedPoints.size - 1
        val lastPx = paddingX + lastIdx * spacingX
        val lastPy = height - paddingX - ((renderedPoints[lastIdx] - minVal) / deltaVal) * (height - paddingX * 2)

        drawCircle(
            color = IceWhite,
            radius = 5.dp.toPx(),
            center = Offset(lastPx, lastPy)
        )
        drawCircle(
            color = lineColor.copy(alpha = 0.5f),
            radius = 12.dp.toPx(),
            center = Offset(lastPx, lastPy)
        )
    }
}
