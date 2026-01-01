package com.masjiddisplay.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.data.Prayer
import com.masjiddisplay.ui.theme.*

/**
 * Alert banner shown during adhan or iqamah time
 */
@Composable
fun PrayerAlertBanner(
    type: String,
    prayer: Prayer,
    modifier: Modifier = Modifier
) {
    val isAdhan = type == "adhan"
    
    // Animation for slide in
    var animatedOffset by remember { mutableFloatStateOf(-100f) }
    var animatedAlpha by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animatedOffset = 0f
        animatedAlpha = 1f
    }
    
    // Pulsing animation for the indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
            .graphicsLayer {
                translationY = animatedOffset
                alpha = animatedAlpha
            }
            .clip(RoundedCornerShape(Radii.medium))
            .background(
                if (isAdhan) AppColors.accentSecondarySoft.copy(alpha = 0.25f)
                else AppColors.accentPrimarySoft.copy(alpha = 0.3f)
            )
            .border(
                width = if (isAdhan) 2.dp else 3.dp,
                color = if (isAdhan) AppColors.accentSecondary else AppColors.accentPrimary,
                shape = RoundedCornerShape(Radii.medium)
            )
            .padding(vertical = Spacing.md, horizontal = Spacing.xl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = if (isAdhan) "🕌" else "🚶",
                style = AppTypography.headlineL.copy(fontSize = 40.sp),
                modifier = Modifier.padding(end = Spacing.md)
            )
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isAdhan) "WAKTU ADZAN" else "IQAMAH",
                    style = AppTypography.headlineS.copy(
                        letterSpacing = 2.sp,
                        fontSize = if (isAdhan) 22.sp else 28.sp
                    ),
                    color = if (isAdhan) AppColors.accentSecondary else AppColors.accentPrimary
                )
                Text(
                    text = if (isAdhan) prayer.name.uppercase() else "MOHON BERDIRI UNTUK SHALAT",
                    style = AppTypography.bodyM.copy(
                        fontSize = if (isAdhan) 16.sp else 18.sp,
                        letterSpacing = if (isAdhan) 0.sp else 1.sp
                    ),
                    color = AppColors.textPrimary
                )
            }
            
            // Pulsing indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .graphicsLayer { alpha = pulseAlpha }
                    .clip(CircleShape)
                    .background(
                        if (isAdhan) AppColors.accentSecondary else AppColors.accentPrimary
                    )
            )
        }
    }
}
