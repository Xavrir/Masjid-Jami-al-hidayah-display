package com.masjiddisplay.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun EnhancedRunningText(
    content: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.accentPrimary,
    textColor: Color = AppColors.textInverse,
    onAnimationCycleComplete: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    // Key by content to reset animation when text changes
    var containerSize by remember(content) { mutableStateOf(IntSize.Zero) }
    var textWidth by remember(content) { mutableStateOf(0) }
    
    val totalScrollDistance = remember(textWidth, containerSize, content) {
        if (textWidth > 0 && containerSize.width > 0) {
            (textWidth + containerSize.width).toFloat()
        } else {
            1f
        }
    }
    
    val durationMs = remember(content) {
        (content.length * 300).coerceIn(20000, 60000)
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "marquee_$content")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_offset_$content"
    )
    
    // Detect animation cycle completion
    var lastOffsetX by remember(content) { mutableStateOf(0f) }
    LaunchedEffect(offsetX, content) {
        // Detect when animation resets (offsetX goes from high to low)
        if (lastOffsetX > 0.95f && offsetX < 0.05f) {
            onAnimationCycleComplete?.invoke()
        }
        lastOffsetX = offsetX
    }
    
    val translationX = remember(offsetX, totalScrollDistance, containerSize) {
        if (containerSize.width > 0) {
            containerSize.width - (offsetX * totalScrollDistance)
        } else {
            0f
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0D2137),
                        Color(0xFF164A5F),
                        Color(0xFF16A085).copy(alpha = 0.8f),
                        Color(0xFF164A5F),
                        Color(0xFF0D2137)
                    )
                )
            )
            .onSizeChanged { containerSize = it }
            .clipToBounds(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = content,
            style = AppTypography.bodyM.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            color = Color.White,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            onTextLayout = { textLayoutResult ->
                textWidth = textLayoutResult.size.width
            },
            modifier = Modifier
                .offset(x = with(density) { translationX.toDp() })
                .padding(vertical = Spacing.md)
        )
    }
}

@Composable
fun MultiSourceRunningText(
    announcements: List<String>,
    kasItems: List<String>,
    quranVerses: List<String>,
    hadiths: List<String>,
    pengajian: List<String>,
    modifier: Modifier = Modifier
) {
    val allContent = remember(announcements, kasItems, quranVerses, hadiths, pengajian) {
        mutableListOf<String>().apply {
            addAll(announcements.map { "📢 Pengumuman: $it" })
            addAll(kasItems.map { "💰 Kas Masjid: $it" })
            addAll(quranVerses.map { "📖 Ayat Quran: $it" })
            addAll(hadiths.map { "💭 Hadits: $it" })
            addAll(pengajian.map { "🎓 Pengajian: $it" })
        }
    }
    
    var currentIndex by remember { mutableStateOf(0) }
    var displayText by remember(allContent) { 
        mutableStateOf(if (allContent.isNotEmpty()) allContent[0] else "Selamat datang di Masjid Jami' Al-Hidayah") 
    }
    
    var pendingNextText by remember { mutableStateOf(false) }
    
    LaunchedEffect(allContent) {
        if (allContent.isEmpty()) return@LaunchedEffect
        currentIndex = 0
        displayText = allContent[0]
    }
    
    val advanceToNextText: () -> Unit = {
        if (allContent.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % allContent.size
            displayText = allContent[currentIndex]
            pendingNextText = false
        }
    }
    
    LaunchedEffect(displayText) {
        pendingNextText = true
    }
    
    EnhancedRunningText(
        content = displayText,
        modifier = modifier,
        onAnimationCycleComplete = {
            if (pendingNextText && allContent.size > 1) {
                advanceToNextText()
            }
        }
    )
}

/**
 * Simple running announcement ticker
 */
@Composable
fun RunningAnnouncementTicker(
    announcements: List<String>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }
    var displayAnnouncement by remember { mutableStateOf(
        if (announcements.isNotEmpty()) announcements[0] else ""
    ) }
    
    LaunchedEffect(announcements) {
        if (announcements.isNotEmpty()) {
            currentIndex = 0
            displayAnnouncement = announcements[0]
        }
    }
    
    val advanceToNext: () -> Unit = {
        if (announcements.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % announcements.size
            displayAnnouncement = announcements[currentIndex]
        }
    }
    
    EnhancedRunningText(
        content = displayAnnouncement,
        modifier = modifier,
        onAnimationCycleComplete = {
            if (announcements.size > 1) {
                advanceToNext()
            }
        }
    )
}

/**
 * Quran verse rotating display
 */
@Composable
fun QuranVerseRunningText(
    verses: List<String>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }
    var displayVerse by remember { mutableStateOf(
        if (verses.isNotEmpty()) verses[0] else ""
    ) }
    
    LaunchedEffect(verses) {
        if (verses.isNotEmpty()) {
            currentIndex = 0
            displayVerse = verses[0]
        }
    }
    
    val advanceToNext: () -> Unit = {
        if (verses.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % verses.size
            displayVerse = verses[currentIndex]
        }
    }
    
    EnhancedRunningText(
        content = displayVerse,
        modifier = modifier,
        backgroundColor = AppColors.successColor,
        textColor = Color.White,
        onAnimationCycleComplete = {
            if (verses.size > 1) {
                advanceToNext()
            }
        }
    )
}

/**
 * Hadith rotating display
 */
@Composable
fun HadithRunningText(
    hadiths: List<String>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }
    var displayHadith by remember { mutableStateOf(
        if (hadiths.isNotEmpty()) hadiths[0] else ""
    ) }
    
    LaunchedEffect(hadiths) {
        if (hadiths.isNotEmpty()) {
            currentIndex = 0
            displayHadith = hadiths[0]
        }
    }
    
    val advanceToNext: () -> Unit = {
        if (hadiths.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % hadiths.size
            displayHadith = hadiths[currentIndex]
        }
    }
    
    EnhancedRunningText(
        content = displayHadith,
        modifier = modifier,
        backgroundColor = AppColors.warningColor,
        textColor = Color.White,
        onAnimationCycleComplete = {
            if (hadiths.size > 1) {
                advanceToNext()
            }
        }
    )
}

/**
 * Pengajian (teaching schedule) rotating display
 */
@Composable
fun PengajianRunningText(
    pengajianList: List<String>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }
    var displayPengajian by remember { mutableStateOf(
        if (pengajianList.isNotEmpty()) pengajianList[0] else ""
    ) }
    
    LaunchedEffect(pengajianList) {
        if (pengajianList.isNotEmpty()) {
            currentIndex = 0
            displayPengajian = pengajianList[0]
        }
    }
    
    val advanceToNext: () -> Unit = {
        if (pengajianList.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % pengajianList.size
            displayPengajian = pengajianList[currentIndex]
        }
    }
    
    EnhancedRunningText(
        content = displayPengajian,
        modifier = modifier,
        backgroundColor = AppColors.infoColor,
        textColor = Color.White,
        onAnimationCycleComplete = {
            if (pengajianList.size > 1) {
                advanceToNext()
            }
        }
    )
}
