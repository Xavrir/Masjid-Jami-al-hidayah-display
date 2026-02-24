package com.masjiddisplay.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.masjiddisplay.data.BannerRemote
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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

    LaunchedEffect(banners.size) {
        if (currentIndex >= banners.size) {
            currentIndex = 0
        }
    }

    // Auto-advance
    LaunchedEffect(banners.size, intervalMs) {
        if (banners.size > 1) {
            while (currentCoroutineContext().isActive) {
                delay(intervalMs)
                val safeSize = banners.size
                if (safeSize > 1) {
                    currentIndex = (currentIndex + 1) % safeSize
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        AnimatedContent(
            targetState = currentIndex,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            label = "banner_slide"
        ) { index ->
            val banner = banners.getOrNull(index) ?: banners.firstOrNull()
            if (banner == null) {
                Box(modifier = Modifier.fillMaxSize())
                return@AnimatedContent
            }
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
