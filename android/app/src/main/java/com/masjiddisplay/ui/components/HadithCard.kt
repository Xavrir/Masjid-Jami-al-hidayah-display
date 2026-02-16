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
import com.masjiddisplay.data.Hadith
import com.masjiddisplay.data.getRandomHadith
import com.masjiddisplay.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Card displaying a rotating Hadith
 */
@Composable
fun HadithCard(
    autoRotate: Boolean = true,
    rotationInterval: Long = 50000L,
    modifier: Modifier = Modifier
) {
    var hadith by remember { mutableStateOf(getRandomHadith()) }
    var alpha by remember { mutableFloatStateOf(1f) }
    
    // Auto-rotation effect
    if (autoRotate) {
        LaunchedEffect(rotationInterval) {
            while (true) {
                delay(rotationInterval)
                // Fade out
                alpha = 0f
                delay(Durations.medium.toLong())
                // Change hadith
                hadith = getRandomHadith()
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
            .border(1.dp, AppColors.accentPrimarySoft, RoundedCornerShape(Radii.medium))
            .padding(vertical = Spacing.xl, horizontal = Spacing.xxl)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Column(
                modifier = Modifier.padding(bottom = Spacing.md)
            ) {
                Text(
                    text = "Hadits Pilihan",
                    style = AppTypography.bodyM.copy(fontSize = 14.sp),
                    color = AppColors.accentPrimary
                )
                Text(
                    text = (hadith.category ?: "Umum").uppercase(),
                    style = AppTypography.caption.copy(fontSize = 10.sp),
                    color = AppColors.textMuted
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
                    text = hadith.arabic,
                    style = AppTypography.bodyL.copy(
                        fontSize = 17.sp,
                        lineHeight = 30.sp
                    ),
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.End,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Footer with source
            Column(
                modifier = Modifier.padding(top = Spacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppColors.divider)
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = hadith.source,
                    style = AppTypography.caption.copy(fontSize = 11.sp),
                    color = AppColors.accentPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
