package com.masjiddisplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.data.Prayer
import com.masjiddisplay.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*

enum class OverlayType {
    ADHAN,
    IQAMAH,
    FRIDAY_REMINDER
}

@Composable
fun PrayerAlertOverlay(
    visible: Boolean,
    overlayType: OverlayType,
    prayer: Prayer?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var canDismiss by remember(visible) { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            canDismiss = false
            delay(350)
            canDismiss = true
        } else {
            canDismiss = false
        }
    }

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
                    .fillMaxWidth(0.55f)
                    .align(Alignment.Center) // Explicitly center the card
                    .graphicsLayer { translationY = 40f } // Nudge down to correct visual center
                    .shadow(
                        elevation = 48.dp,
                        shape = RoundedCornerShape(40.dp),
                        spotColor = Color.Black.copy(alpha = 0.8f),
                        ambientColor = Color.Black.copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(40.dp))
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
                        shape = RoundedCornerShape(40.dp)
                    )
                    .padding(horizontal = 48.dp, vertical = 56.dp)
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
                        .fillMaxHeight()
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
                    }

                    Text(
                        text = title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentColor.copy(alpha = 0.9f),
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = subtitle,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                        lineHeight = 48.sp
                    )

                    if (prayer != null && overlayType != OverlayType.FRIDAY_REMINDER) {
                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(40.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ADZAN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textSecondary.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prayer.adhanTime,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Light,
                                    color = AppColors.textPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "IQAMAH",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textSecondary.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prayer.iqamahTime,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Light,
                                    color = AppColors.textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Text(
                        text = "Ketuk untuk menutup",
                        fontSize = 13.sp,
                        color = AppColors.textMuted.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
