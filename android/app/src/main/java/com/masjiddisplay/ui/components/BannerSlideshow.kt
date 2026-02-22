package com.masjiddisplay.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.masjiddisplay.data.BannerRemote
import kotlinx.coroutines.delay

/**
 * Banner Slideshow Composable
 *
 * Displays banners from Supabase as a full-width slideshow with:
 * - Auto-advancement every [intervalMs] milliseconds
 * - Crossfade transitions between banners
 * - Navigation dots indicator
 * - Graceful empty/single-banner handling
 */
@Composable
fun BannerSlideshow(
    banners: List<BannerRemote>,
    intervalMs: Long = 8000L,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    // Auto-advance
    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(intervalMs)
                currentIndex = (currentIndex + 1) % banners.size
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Banner image with crossfade
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                fadeIn(animationSpec = androidx.compose.animation.core.tween(800)) togetherWith
                fadeOut(animationSpec = androidx.compose.animation.core.tween(800))
            },
            label = "banner_crossfade"
        ) { index ->
            val banner = banners.getOrNull(index) ?: banners[0]
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(banner.image_url)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = banner.title ?: "Banner ${index + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
        }

        // Bottom gradient for dots visibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        // Navigation dots
        if (banners.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                banners.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentIndex) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex)
                                    Color(0xFFFFA500) // Orange for active
                                else
                                    Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }


    }
}
