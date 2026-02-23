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
 * Displays banners from Supabase as a slideshow that fills available space.
 * Total display time is [totalDurationMs] (default 10 minutes), divided equally
 * among all banners. Uses crossfade transitions and navigation dots.
 *
 * @param banners List of banners to display
 * @param totalDurationMs Total cycle duration in ms (default 600,000 = 10 min)
 * @param onCycleComplete Called when one full cycle through all banners is done
 */
@Composable
fun BannerSlideshow(
    banners: List<BannerRemote>,
    totalDurationMs: Long = 600_000L,
    onCycleComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    val perBannerMs = remember(banners.size, totalDurationMs) {
        if (banners.size > 0) totalDurationMs / banners.size else totalDurationMs
    }

    // Auto-advance timer
    LaunchedEffect(banners.size, perBannerMs) {
        if (banners.size > 1) {
            while (true) {
                delay(perBannerMs)
                val nextIndex = (currentIndex + 1) % banners.size
                currentIndex = nextIndex
                if (nextIndex == 0) {
                    onCycleComplete()
                }
            }
        } else {
            // Single banner: wait for full duration then signal complete
            delay(totalDurationMs)
            onCycleComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Banner image with crossfade
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                fadeIn(animationSpec = androidx.compose.animation.core.tween(1000)) togetherWith
                fadeOut(animationSpec = androidx.compose.animation.core.tween(1000))
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
                modifier = Modifier.fillMaxSize(),
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
                                    Color(0xFFFFA500)
                                else
                                    Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }

        // Banner title overlay
        val currentBanner = banners.getOrNull(currentIndex)
        if (currentBanner?.title != null && currentBanner.title.isNotBlank()) {
            Text(
                text = currentBanner.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 28.dp)
            )
        }
    }
}
