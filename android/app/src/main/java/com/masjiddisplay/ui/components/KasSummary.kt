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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.data.KasData
import com.masjiddisplay.data.TrendDirection
import com.masjiddisplay.ui.theme.*
import com.masjiddisplay.utils.formatCurrency

/**
 * Summary card showing mosque treasury (kas) information
 */
@Composable
fun KasSummary(
    kasData: KasData,
    modifier: Modifier = Modifier
) {
    val balanceColor = when {
        kasData.balance > 0 -> AppColors.kasPositive
        kasData.balance < 0 -> AppColors.kasNegative
        else -> AppColors.kasNeutral
    }
    
    val trendIcon = when (kasData.trendDirection) {
        TrendDirection.UP -> "↑"
        TrendDirection.DOWN -> "↓"
        TrendDirection.FLAT -> "→"
    }
    
    val trendText = when (kasData.trendDirection) {
        TrendDirection.UP -> "Meningkat"
        TrendDirection.DOWN -> "Menurun"
        TrendDirection.FLAT -> "Stabil"
    }
    
    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(Radii.medium))
            .clip(RoundedCornerShape(Radii.medium))
            .background(AppColors.surfaceGlass)
            .border(1.dp, AppColors.borderSubtle, RoundedCornerShape(Radii.medium))
            .padding(Spacing.lg)
            .heightIn(max = 180.dp)
    ) {
        Column {
            // Title
            Text(
                text = "Kas Masjid",
                style = AppTypography.bodyM.copy(fontSize = 16.sp),
                color = AppColors.textPrimary
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Balance Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Saldo Saat Ini",
                    style = AppTypography.caption.copy(fontSize = 11.sp),
                    color = AppColors.textMuted
                )
                
                Spacer(modifier = Modifier.height(Spacing.xs))
                
                Text(
                    text = formatCurrency(kasData.balance),
                    style = AppTypography.numericMedium.copy(fontSize = 28.sp),
                    color = balanceColor
                )
                
                Spacer(modifier = Modifier.height(Spacing.xs))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trendIcon,
                        style = AppTypography.bodyM.copy(fontSize = 16.sp),
                        color = AppColors.accentSecondary
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = trendText,
                        style = AppTypography.caption.copy(fontSize = 11.sp),
                        color = AppColors.textSecondary
                    )
                }
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppColors.divider)
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pemasukan Bulan Ini",
                        style = AppTypography.caption.copy(fontSize = 10.sp),
                        color = AppColors.textMuted
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = formatCurrency(kasData.incomeMonth),
                        style = AppTypography.numericSmall.copy(fontSize = 16.sp),
                        color = AppColors.kasPositive
                    )
                }
                
                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(AppColors.divider)
                        .padding(horizontal = Spacing.md)
                )
                
                // Expense
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pengeluaran Bulan Ini",
                        style = AppTypography.caption.copy(fontSize = 10.sp),
                        color = AppColors.textMuted
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = formatCurrency(kasData.expenseMonth),
                        style = AppTypography.numericSmall.copy(fontSize = 16.sp),
                        color = AppColors.kasNegative
                    )
                }
            }
        }
    }
}
