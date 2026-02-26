package com.masjiddisplay.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.data.Prayer
import com.masjiddisplay.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*

enum class OverlayType {
    ADHAN,
    IQAMAH,
    FRIDAY_REMINDER,
    PRE_ADHAN
}

@Composable
fun PrayerAlertOverlay(
    visible: Boolean,
    overlayType: OverlayType,
    prayer: Prayer?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCountdown = 10
    var canDismiss by remember(visible) { mutableStateOf(false) }
    var countdownValue by remember(visible) { mutableIntStateOf(totalCountdown) }
    var countdownProgress by remember(visible) { mutableFloatStateOf(1f) }

    LaunchedEffect(visible) {
        if (visible) {
            canDismiss = false
            delay(350)
            canDismiss = true
        } else {
            canDismiss = false
        }
    }

    LaunchedEffect(visible, overlayType) {
        if (visible && overlayType == OverlayType.IQAMAH) {
            countdownValue = totalCountdown
            countdownProgress = 1f
            for (i in totalCountdown downTo 1) {
                countdownValue = i

                val startProgress = i.toFloat() / totalCountdown
                val endProgress = (i - 1).toFloat() / totalCountdown
                val steps = 20
                val stepDelay = 1000L / steps
                for (s in 0 until steps) {
                    countdownProgress = startProgress + (endProgress - startProgress) * s / steps
                    delay(stepDelay)
                }
            }
            countdownValue = 0
            countdownProgress = 0f
            delay(500)
            onDismiss()
        }
    }

    val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    if (visible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
                .clickable(enabled = canDismiss) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .align(Alignment.Center)
                    .shadow(
                        elevation = 48.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color.Black.copy(alpha = 0.8f),
                        ambientColor = Color.Black.copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppColors.surfaceElevated.copy(alpha = 0.6f))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AppColors.surfaceGlass.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset.Unspecified,
                            radius = 500f
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.0f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 48.dp, vertical = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Transparent,
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val (title, subtitle, accentColor) = when (overlayType) {
                        OverlayType.ADHAN -> Triple(
                            "WAKTU ADZAN",
                            prayer?.name?.uppercase() ?: "",
                            AppColors.accentPrimary
                        )
                        OverlayType.IQAMAH -> Triple(
                            "IQAMAH",
                            "MOHON BERDIRI UNTUK SHALAT ${prayer?.name?.uppercase() ?: ""}",
                            AppColors.accentSecondary
                        )
                        OverlayType.FRIDAY_REMINDER -> Triple(
                            "PENGINGAT JUMAT",
                            "1 MENIT MENUJU SHALAT JUMAT",
                            Color(0xFF4CAF50)
                        )
                        OverlayType.PRE_ADHAN -> Triple(
                            "PERSIAPAN ADZAN",
                            "3 MENIT MENUJU ADZAN ${prayer?.name?.uppercase() ?: ""}",
                            Color(0xFF2196F3)
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentColor.copy(alpha = 0.9f),
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = subtitle,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                        lineHeight = 44.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (prayer != null && overlayType != OverlayType.FRIDAY_REMINDER && overlayType != OverlayType.PRE_ADHAN) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(40.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ADZAN",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textSecondary.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prayer.adhanTime,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Light,
                                    color = AppColors.textPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "IQAMAH",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textSecondary.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prayer.iqamahTime,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Light,
                                    color = AppColors.textPrimary
                                )
                            }
                        }
                    }

                    if (overlayType == OverlayType.IQAMAH) {
                        Spacer(modifier = Modifier.height(16.dp))

                        val ringColor = when {
                            countdownValue <= 3 -> Color(0xFFE74C3C)
                            countdownValue <= 5 -> Color(0xFFF39C12)
                            else -> AppColors.accentSecondary
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(90.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 8.dp.toPx()
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.1f),
                                    style = Stroke(width = strokeWidth)
                                )
                                drawArc(
                                    color = ringColor.copy(alpha = pulseAlpha),
                                    startAngle = -90f,
                                    sweepAngle = 360f * countdownProgress,
                                    useCenter = false,
                                    style = Stroke(
                                        width = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                )
                            }

                            Text(
                                text = "$countdownValue",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = ringColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "BERSIAP UNTUK SHALAT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ketuk untuk menutup",
                        fontSize = 20.sp,
                        color = AppColors.textMuted.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
