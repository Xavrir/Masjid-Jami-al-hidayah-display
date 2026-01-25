package com.masjiddisplay.ui.components

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Enhanced running text component that displays data from Supabase
 * Rotates through announcements, Quran verses, Hadiths, and Pengajian
 */
@Composable
fun EnhancedRunningText(
    content: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.accentPrimary,
    textColor: Color = AppColors.textInverse
) {
    var displayText by remember { mutableStateOf(content) }
    
    // Create repeating animation
    val animationDuration = (displayText.length * 50).coerceIn(5000, 20000)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            style = AppTypography.bodyM.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        )
    }
}

/**
 * Multi-source running text that rotates between different types of content
 */
@Composable
fun MultiSourceRunningText(
    announcements: List<String>,
    quranVerses: List<String>,
    hadiths: List<String>,
    pengajian: List<String>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }
    var displayText by remember { mutableStateOf("") }
    
    // Combine all content
    val allContent = remember {
        mutableListOf<String>().apply {
            addAll(announcements.map { "📢 Pengumuman: $it" })
            addAll(quranVerses.map { "📖 Ayat Quran: $it" })
            addAll(hadiths.map { "💭 Hadits: $it" })
            addAll(pengajian.map { "🎓 Pengajian: $it" })
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            if (allContent.isNotEmpty()) {
                displayText = allContent[currentIndex % allContent.size]
                currentIndex++
                delay(8000) // Display each item for 8 seconds
            }
            delay(1000)
        }
    }
    
    EnhancedRunningText(
        content = displayText,
        modifier = modifier
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
        while (true) {
            if (announcements.isNotEmpty()) {
                displayAnnouncement = announcements[currentIndex % announcements.size]
                currentIndex++
                delay(6000) // 6 seconds per announcement
            }
            delay(500)
        }
    }
    
    EnhancedRunningText(
        content = displayAnnouncement,
        modifier = modifier
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
        while (true) {
            if (verses.isNotEmpty()) {
                displayVerse = verses[currentIndex % verses.size]
                currentIndex++
                delay(10000) // 10 seconds per verse
            }
            delay(500)
        }
    }
    
    EnhancedRunningText(
        content = displayVerse,
        modifier = modifier,
        backgroundColor = AppColors.successColor,
        textColor = Color.White
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
        while (true) {
            if (hadiths.isNotEmpty()) {
                displayHadith = hadiths[currentIndex % hadiths.size]
                currentIndex++
                delay(10000) // 10 seconds per hadith
            }
            delay(500)
        }
    }
    
    EnhancedRunningText(
        content = displayHadith,
        modifier = modifier,
        backgroundColor = AppColors.warningColor,
        textColor = Color.White
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
        while (true) {
            if (pengajianList.isNotEmpty()) {
                displayPengajian = pengajianList[currentIndex % pengajianList.size]
                currentIndex++
                delay(8000) // 8 seconds per item
            }
            delay(500)
        }
    }
    
    EnhancedRunningText(
        content = displayPengajian,
        modifier = modifier,
        backgroundColor = AppColors.infoColor,
        textColor = Color.White
    )
}
