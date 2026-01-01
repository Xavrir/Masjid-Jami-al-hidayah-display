package com.masjiddisplay.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.ui.theme.*

/**
 * Horizontally scrolling announcement ticker
 */
@Composable
fun AnnouncementTicker(
    announcements: List<String>,
    speed: String = "slow",
    modifier: Modifier = Modifier
) {
    val combinedText = if (announcements.isNotEmpty()) {
        announcements.joinToString(" • ")
    } else {
        ""
    }
    
    val density = LocalDensity.current
    var textWidth by remember { mutableIntStateOf(0) }
    var containerWidth by remember { mutableIntStateOf(0) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    
    // Calculate animation duration based on text width and speed
    val pixelsPerSecond = if (speed == "slow") 50f else 100f
    val duration = if (textWidth > 0) {
        ((textWidth + containerWidth) / pixelsPerSecond * 1000).toInt()
    } else {
        10000
    }
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (textWidth > 0) -(textWidth.toFloat() + 100f) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerOffset"
    )
    
    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.small))
            .background(AppColors.surfaceGlass.copy(alpha = 0.85f))
            .border(1.dp, AppColors.accentPrimarySoft.copy(alpha = 0.25f), RoundedCornerShape(Radii.small))
            .padding(horizontal = Spacing.lg)
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.graphicsLayer {
                translationX = offset
            }
        ) {
            // First copy of text
            Text(
                text = combinedText,
                style = AppTypography.bodyM.copy(
                    fontSize = 24.sp,
                    letterSpacing = 0.3.sp
                ),
                color = AppColors.textPrimary,
                maxLines = 1,
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    textWidth = coordinates.size.width
                }
            )
            
            Spacer(modifier = Modifier.width(100.dp))
            
            // Second copy for seamless loop
            Text(
                text = combinedText,
                style = AppTypography.bodyM.copy(
                    fontSize = 24.sp,
                    letterSpacing = 0.3.sp
                ),
                color = AppColors.textPrimary,
                maxLines = 1
            )
        }
    }
}
