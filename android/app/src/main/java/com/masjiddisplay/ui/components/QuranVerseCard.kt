package com.masjiddisplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.data.QuranVerse
import com.masjiddisplay.data.getRandomQuranVerse
import com.masjiddisplay.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Card displaying a rotating Quran verse
 */
@Composable
fun QuranVerseCard(
    autoRotate: Boolean = true,
    rotationInterval: Long = 40000L,
    modifier: Modifier = Modifier
) {
    var verse by remember { mutableStateOf(getRandomQuranVerse()) }
    var alpha by remember { mutableFloatStateOf(1f) }
    
    // Auto-rotation effect
    if (autoRotate) {
        LaunchedEffect(rotationInterval) {
            while (true) {
                delay(rotationInterval)
                // Fade out
                alpha = 0f
                delay(Durations.medium.toLong())
                // Change verse
                verse = getRandomQuranVerse()
                // Fade in
                alpha = 1f
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(Radii.medium))
            .background(AppColors.surfaceGlass)
            .border(1.dp, AppColors.accentSecondarySoft, RoundedCornerShape(Radii.medium))
            .padding(vertical = Spacing.xl, horizontal = Spacing.xxl)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier.padding(bottom = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QS. ${verse.surah} (${verse.surahNumber}): ${verse.ayah}",
                    style = AppTypography.bodyM.copy(fontSize = 14.sp),
                    color = AppColors.accentSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = Spacing.xs)
            ) {
                // Arabic text
                Text(
                    text = verse.arabic,
                    style = AppTypography.bodyL.copy(
                        fontSize = 18.sp,
                        lineHeight = 32.sp
                    ),
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.End,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Transliteration
                verse.transliteration?.let { transliteration ->
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = transliteration,
                        style = AppTypography.caption.copy(
                            fontSize = 9.sp,
                            lineHeight = 14.sp
                        ),
                        color = AppColors.accentSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Footer decorative line
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(Radii.pill))
                        .background(AppColors.accentSecondary)
                )
            }
        }
    }
}
