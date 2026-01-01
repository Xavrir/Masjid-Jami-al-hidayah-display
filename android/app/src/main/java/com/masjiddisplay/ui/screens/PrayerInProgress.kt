package com.masjiddisplay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.data.Prayer
import com.masjiddisplay.ui.theme.*
import com.masjiddisplay.utils.*
import kotlinx.coroutines.delay
import java.util.*

private const val AUTO_RETURN_MS = 60_000L

/**
 * Screen shown when a prayer is currently in progress
 */
@Composable
fun PrayerInProgress(
    prayer: Prayer,
    onComplete: () -> Unit,
    masjidName: String = "Masjid",
    masjidLocation: String? = null,
    forceDebug: Boolean = false,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }
    var autoReturnDeadline by remember { mutableStateOf(Date(System.currentTimeMillis() + AUTO_RETURN_MS)) }
    
    // Get prayer window bounds
    val windowBounds = remember(prayer, currentTime) {
        PrayerTimeCalculator.getPrayerWindowBounds(prayer, currentTime)
    }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }
    
    // Auto-return timer
    LaunchedEffect(prayer) {
        delay(AUTO_RETURN_MS)
        onComplete()
    }
    
    // Calculate remaining times
    val windowRemainingMs = (windowBounds.end.time - currentTime.time).coerceAtLeast(0)
    val iqamahRemainingMs = (windowBounds.iqamahDate.time - currentTime.time).coerceAtLeast(0)
    val autoReturnRemainingMs = (autoReturnDeadline.time - currentTime.time).coerceAtLeast(0)
    
    val phase = PrayerTimeCalculator.getPrayerPhase(prayer, currentTime)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.background.copy(alpha = 0.84f),
                        AppColors.backgroundGradientBottom.copy(alpha = 0.94f)
                    )
                )
            )
            .padding(
                top = SafeAreaMargins.top + Spacing.lg,
                bottom = SafeAreaMargins.bottom + Spacing.lg,
                start = Spacing.xxl,
                end = Spacing.xxl
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xxl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left - Masjid info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = Spacing.xxl)
                ) {
                    Text(
                        text = masjidName.uppercase(),
                        style = AppTypography.headlineS.copy(letterSpacing = 2.sp),
                        color = AppColors.textPrimary
                    )
                    masjidLocation?.let { location ->
                        Text(
                            text = location,
                            style = AppTypography.bodyS.copy(lineHeight = 18.sp),
                            color = AppColors.textSecondary,
                            maxLines = 2
                        )
                    }
                }
                
                // Center - Clock
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatTimeWithSeconds(currentTime),
                        style = AppTypography.displayM,
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = formatGregorianDate(currentTime),
                        style = AppTypography.bodyM,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = getHijriDate(currentTime),
                        style = AppTypography.bodyS,
                        color = AppColors.accentPrimary
                    )
                }
                
                // Right - Badges
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    // Phase badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radii.pill))
                            .background(AppColors.surfaceGlass)
                            .border(1.dp, AppColors.accentPrimarySoft, RoundedCornerShape(Radii.pill))
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    ) {
                        Text(
                            text = if (phase == "adzan") "Adzan" else "Iqamah",
                            style = AppTypography.caption.copy(letterSpacing = 0.5.sp),
                            color = AppColors.textPrimary
                        )
                    }
                    
                    // Mode badge (Live/Debug)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radii.pill))
                            .background(if (forceDebug) AppColors.badgeWarning else AppColors.surfaceGlass)
                            .border(1.dp, AppColors.accentPrimarySoft, RoundedCornerShape(Radii.pill))
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    ) {
                        Text(
                            text = if (forceDebug) "Debug" else "Live",
                            style = AppTypography.caption.copy(letterSpacing = 0.5.sp),
                            color = AppColors.textPrimary
                        )
                    }
                }
            }
            
            // Center content - Main card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(Radii.large))
                        .background(AppColors.surfaceDefault.copy(alpha = 0.75f))
                        .border(1.2.dp, AppColors.accentPrimarySoft, RoundedCornerShape(Radii.large))
                        .padding(Spacing.sectionGap)
                ) {
                    Column {
                        // Card header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Spacing.xl),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radii.pill))
                                    .background(AppColors.accentPrimarySoft)
                                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                            ) {
                                Text(
                                    text = "${if (phase == "adzan") "Adzan" else "Iqamah"} • ${prayer.name}",
                                    style = AppTypography.caption.copy(letterSpacing = 0.5.sp),
                                    color = AppColors.accentPrimary
                                )
                            }
                            
                            // Window duration
                            Text(
                                text = "Jendela ${windowBounds.durationMinutes} menit",
                                style = AppTypography.bodyS,
                                color = AppColors.textSecondary
                            )
                        }
                        
                        // Title
                        Text(
                            text = "Sedang berlangsung",
                            style = AppTypography.headlineL,
                            color = AppColors.textPrimary
                        )
                        
                        Text(
                            text = "Mohon menjaga ketenangan dan kekhusyukan jamaah.",
                            style = AppTypography.bodyM,
                            color = AppColors.textSecondary,
                            modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xl)
                        )
                        
                        // Countdown row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                        ) {
                            // Remaining time
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Radii.medium))
                                    .background(AppColors.surfaceElevated)
                                    .border(1.dp, AppColors.borderSubtle, RoundedCornerShape(Radii.medium))
                                    .padding(vertical = Spacing.lg, horizontal = Spacing.xl)
                            ) {
                                Column {
                                    Text(
                                        text = "SISA WAKTU",
                                        style = AppTypography.caption,
                                        color = AppColors.textMuted
                                    )
                                    Text(
                                        text = formatMsToClock(windowRemainingMs),
                                        style = AppTypography.displayS,
                                        color = AppColors.accentPrimary
                                    )
                                    Text(
                                        text = "Hingga akhir jendela adzan",
                                        style = AppTypography.bodyS,
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                            
                            // Iqamah countdown
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Radii.medium))
                                    .background(AppColors.surfaceElevated)
                                    .border(1.dp, AppColors.borderSubtle, RoundedCornerShape(Radii.medium))
                                    .padding(vertical = Spacing.lg, horizontal = Spacing.xl)
                            ) {
                                Column {
                                    Text(
                                        text = "MENUJU IQAMAH",
                                        style = AppTypography.caption,
                                        color = AppColors.textMuted
                                    )
                                    Text(
                                        text = if (phase == "iqamah") "00:00" else formatMsToClock(iqamahRemainingMs),
                                        style = AppTypography.displayS,
                                        color = AppColors.accentPrimary
                                    )
                                    Text(
                                        text = if (phase == "iqamah") "Iqamah berlangsung" else "Perkiraan ke iqamah",
                                        style = AppTypography.bodyS,
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                        }
                        
                        // Time chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            // Adhan chip
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Radii.medium))
                                    .background(
                                        if (phase == "adzan") AppColors.accentPrimarySoft
                                        else AppColors.surfaceGlass
                                    )
                                    .border(
                                        1.dp,
                                        if (phase == "adzan") AppColors.accentPrimary else AppColors.borderSubtle,
                                        RoundedCornerShape(Radii.medium)
                                    )
                                    .padding(vertical = Spacing.md, horizontal = Spacing.lg)
                            ) {
                                Column {
                                    Text(
                                        text = "ADZAN",
                                        style = AppTypography.caption,
                                        color = AppColors.textMuted
                                    )
                                    Text(
                                        text = prayer.adhanTime,
                                        style = AppTypography.numericMedium,
                                        color = AppColors.textPrimary
                                    )
                                }
                            }
                            
                            // Iqamah chip
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Radii.medium))
                                    .background(
                                        if (phase == "iqamah") AppColors.accentPrimarySoft
                                        else AppColors.surfaceGlass
                                    )
                                    .border(
                                        1.dp,
                                        if (phase == "iqamah") AppColors.accentPrimary else AppColors.borderSubtle,
                                        RoundedCornerShape(Radii.medium)
                                    )
                                    .padding(vertical = Spacing.md, horizontal = Spacing.lg)
                            ) {
                                Column {
                                    Text(
                                        text = "IQAMAH",
                                        style = AppTypography.caption,
                                        color = AppColors.textMuted
                                    )
                                    Text(
                                        text = prayer.iqamahTime,
                                        style = AppTypography.numericMedium,
                                        color = AppColors.textPrimary
                                    )
                                }
                            }
                        }
                        
                        // Footer
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radii.pill))
                                .background(AppColors.surfaceElevated)
                                .border(1.dp, AppColors.accentPrimarySoft, RoundedCornerShape(Radii.pill))
                                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        ) {
                            Text(
                                text = "Notifikasi suara hanya saat adzan & iqamah",
                                style = AppTypography.caption.copy(letterSpacing = 0.2.sp),
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }
            }
            
            // Bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Start time
                Column {
                    Text(
                        text = "MULAI ADZAN",
                        style = AppTypography.caption,
                        color = AppColors.textMuted
                    )
                    Text(
                        text = prayer.adhanTime,
                        style = AppTypography.numericSmall,
                        color = AppColors.textPrimary
                    )
                }
                
                // End time
                Column {
                    Text(
                        text = "AKHIR JENDELA",
                        style = AppTypography.caption,
                        color = AppColors.textMuted
                    )
                    Text(
                        text = formatTime(windowBounds.end),
                        style = AppTypography.numericSmall,
                        color = AppColors.textPrimary
                    )
                }
                
                // Auto-return info and skip button
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Kembali ke beranda dalam ${formatMsToClock(autoReturnRemainingMs)}",
                        style = AppTypography.bodyS,
                        color = AppColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radii.medium))
                            .background(AppColors.surfaceGlass)
                            .border(1.dp, AppColors.borderSubtle, RoundedCornerShape(Radii.medium))
                            .clickable { onComplete() }
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                    ) {
                        Text(
                            text = "Kembali",
                            style = AppTypography.bodyS.copy(letterSpacing = 0.5.sp),
                            color = AppColors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
