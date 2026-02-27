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
    LaunchedEffect(banners.size, intervalMs, currentIndex) {
        if (banners.size > 1) {
            val currentBanner = banners.getOrNull(currentIndex)
            // If it's a video, we might want to wait for it to finish or just use a longer interval
            // For now, let's stick to intervalMs or video duration if we could easily get it
            val delayValue = if (currentBanner?.type == "video") 15000L else intervalMs
            
            delay(delayValue)
            currentIndex = (currentIndex + 1) % banners.size
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

            if (banner.type == "video") {
                VideoPlayer(
                    url = banner.image_url,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
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

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            val mediaItem = androidx.media3.common.MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
            volume = 0f // Mute for banners
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(url) {
        onDispose {
            exoPlayer.release()
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            androidx.media3.ui.PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFramesLayout.RESIZE_MODE_ZOOM
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier
    )
}
