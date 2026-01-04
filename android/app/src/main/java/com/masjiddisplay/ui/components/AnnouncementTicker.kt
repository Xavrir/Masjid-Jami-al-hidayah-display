package com.masjiddisplay.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val SLOW_PIXELS_PER_SECOND = 50f
private const val FAST_PIXELS_PER_SECOND = 100f
private const val DEFAULT_DURATION_MS = 10000
private const val GAP_WIDTH_DP = 100

@Composable
fun AnnouncementTicker(
    announcements: List<String>,
    speed: String = "slow",
    modifier: Modifier = Modifier
) {
    val combinedText = if (announcements.isNotEmpty()) {
        announcements.joinToString("  •  ")
    } else {
        ""
    }
    
    var textWidth by remember { mutableIntStateOf(0) }
    var containerWidth by remember { mutableIntStateOf(0) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    
    val pixelsPerSecond = if (speed == "slow") SLOW_PIXELS_PER_SECOND else FAST_PIXELS_PER_SECOND
    val duration = if (textWidth > 0) {
        ((textWidth + containerWidth) / pixelsPerSecond * 1000).toInt()
    } else {
        DEFAULT_DURATION_MS
    }
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (textWidth > 0) -(textWidth.toFloat() + GAP_WIDTH_DP.toFloat()) else 0f,
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
            .height(40.dp)
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.graphicsLayer {
                translationX = offset
            }
        ) {
            Text(
                text = combinedText,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                ),
                maxLines = 1,
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    textWidth = coordinates.size.width
                }
            )
            
            Spacer(modifier = Modifier.width(GAP_WIDTH_DP.dp))
            
            Text(
                text = combinedText,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                ),
                maxLines = 1
            )
        }
    }
}
