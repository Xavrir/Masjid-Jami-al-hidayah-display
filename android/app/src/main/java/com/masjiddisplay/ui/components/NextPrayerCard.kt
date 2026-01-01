package com.masjiddisplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.data.Prayer
import com.masjiddisplay.ui.theme.*

/**
 * Card displaying the next prayer time with countdown
 */
@Composable
fun NextPrayerCard(
    prayer: Prayer?,
    isTomorrow: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(Radii.medium))
            .background(AppColors.surfaceElevated)
            .border(1.dp, AppColors.accentPrimarySoft, RoundedCornerShape(Radii.medium))
            .padding(Spacing.lg)
    ) {
        if (prayer == null) {
            Text(
                text = "Tidak ada jadwal salat berikutnya",
                style = AppTypography.bodyS,
                color = AppColors.textMuted,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Chip row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                ) {
                    // Prayer name chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radii.pill))
                            .background(AppColors.accentPrimarySoft)
                            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    ) {
                        Text(
                            text = prayer.name,
                            style = AppTypography.bodyS.copy(fontSize = 12.sp),
                            color = AppColors.accentPrimary
                        )
                    }
                    
                    // Tomorrow badge
                    if (isTomorrow) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radii.pill))
                                .background(AppColors.accentSecondarySoft)
                                .border(1.dp, AppColors.accentSecondary, RoundedCornerShape(Radii.pill))
                                .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                        ) {
                            Text(
                                text = "BESOK",
                                style = AppTypography.caption.copy(fontSize = 10.sp),
                                color = AppColors.accentSecondary
                            )
                        }
                    }
                }
                
                // Content row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Time section
                    Column {
                        Text(
                            text = "Berikutnya",
                            style = AppTypography.bodyS.copy(fontSize = 13.sp),
                            color = AppColors.textSecondary
                        )
                        Text(
                            text = "Adzan",
                            style = AppTypography.caption.copy(fontSize = 11.sp),
                            color = AppColors.textMuted
                        )
                        Text(
                            text = prayer.adhanTime,
                            style = AppTypography.numericMedium.copy(fontSize = 32.sp),
                            color = AppColors.textPrimary
                        )
                    }
                    
                    // Countdown section
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Dalam",
                            style = AppTypography.caption.copy(fontSize = 11.sp),
                            color = AppColors.textMuted
                        )
                        Text(
                            text = prayer.countdown ?: "--:--",
                            style = AppTypography.numericMedium.copy(fontSize = 28.sp),
                            color = AppColors.accentSecondary
                        )
                    }
                }
                
                // Iqamah row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppColors.divider)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Iqamah: ",
                        style = AppTypography.bodyS.copy(fontSize = 13.sp),
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = prayer.iqamahTime,
                        style = AppTypography.numericSmall.copy(fontSize = 20.sp),
                        color = AppColors.textPrimary
                    )
                }
            }
        }
    }
}
