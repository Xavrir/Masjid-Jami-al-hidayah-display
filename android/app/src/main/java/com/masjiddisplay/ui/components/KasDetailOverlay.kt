package com.masjiddisplay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.data.KasData
import com.masjiddisplay.data.TransactionType
import com.masjiddisplay.data.TrendDirection
import com.masjiddisplay.ui.theme.*
import com.masjiddisplay.utils.formatCurrency

/**
 * Full-screen overlay showing detailed kas (treasury) information
 */
@Composable
fun KasDetailOverlay(
    visible: Boolean,
    kasData: KasData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background.copy(alpha = 0.65f))
            .clickable { onClose() },
        contentAlignment = Alignment.CenterEnd
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.4f)
                    .background(AppColors.surfaceElevated)
                    .clickable(enabled = false) { }  // Prevent clicks from closing overlay
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl)
                        .padding(bottom = Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ringkasan Kas Masjid",
                        style = AppTypography.titleM,
                        color = AppColors.textPrimary
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(Radii.small))
                            .background(AppColors.surfaceDefault)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            style = AppTypography.titleM,
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
                
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.xl)
                ) {
                    // Balance Card
                    BalanceCard(kasData = kasData)
                    
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    
                    // Income/Expense Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        StatCard(
                            label = "Total Pemasukan Bulan Ini",
                            value = formatCurrency(kasData.incomeMonth),
                            valueColor = AppColors.kasPositive,
                            borderColor = AppColors.kasPositive,
                            modifier = Modifier.weight(1f)
                        )
                        
                        StatCard(
                            label = "Total Pengeluaran Bulan Ini",
                            value = formatCurrency(kasData.expenseMonth),
                            valueColor = AppColors.kasNegative,
                            borderColor = AppColors.kasNegative,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    
                    // Sparkline Card (placeholder)
                    SparklineCard()
                    
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    
                    // Transactions Card
                    TransactionsCard(kasData = kasData)
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(kasData: KasData) {
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
        TrendDirection.UP -> "Meningkat dari bulan lalu"
        TrendDirection.DOWN -> "Menurun dari bulan lalu"
        TrendDirection.FLAT -> "Stabil"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.medium))
            .background(AppColors.surfaceGlass)
            .border(1.dp, AppColors.accentSecondarySoft, RoundedCornerShape(Radii.medium))
            .padding(Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Saldo Saat Ini",
                style = AppTypography.bodyS,
                color = AppColors.textMuted
            )
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            Text(
                text = formatCurrency(kasData.balance),
                style = AppTypography.numericLarge,
                color = balanceColor
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trendIcon,
                    style = AppTypography.titleM,
                    color = AppColors.accentSecondary
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = trendText,
                    style = AppTypography.bodyS,
                    color = AppColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radii.medium))
            .background(AppColors.surfaceDefault)
            .border(2.dp, borderColor, RoundedCornerShape(Radii.medium))
            .padding(Spacing.lg)
    ) {
        Column {
            Text(
                text = label,
                style = AppTypography.bodyS,
                color = AppColors.textMuted
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Text(
                text = value,
                style = AppTypography.numericMedium,
                color = valueColor
            )
        }
    }
}

@Composable
private fun SparklineCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.medium))
            .background(AppColors.surfaceDefault)
            .padding(Spacing.lg)
    ) {
        Column {
            Text(
                text = "Trend 30 Hari Terakhir",
                style = AppTypography.titleS,
                color = AppColors.textPrimary
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(Radii.small))
                    .background(AppColors.surfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Grafik trend kas ditampilkan di sini",
                    style = AppTypography.bodyS,
                    color = AppColors.textMuted
                )
            }
        }
    }
}

@Composable
private fun TransactionsCard(kasData: KasData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.medium))
            .background(AppColors.surfaceDefault)
            .padding(Spacing.lg)
    ) {
        Column {
            Text(
                text = "Transaksi Terbaru",
                style = AppTypography.titleS,
                color = AppColors.textPrimary
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            kasData.recentTransactions.forEach { transaction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.md)
                        .then(
                            Modifier
                                .padding(bottom = Spacing.sm)
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = transaction.date,
                            style = AppTypography.caption,
                            color = AppColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = transaction.description,
                            style = AppTypography.bodyM,
                            color = AppColors.textPrimary
                        )
                    }
                    
                    val amountColor = if (transaction.type == TransactionType.INCOME) {
                        AppColors.kasPositive
                    } else {
                        AppColors.kasNegative
                    }
                    
                    val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
                    
                    Text(
                        text = "$prefix${formatCurrency(transaction.amount)}",
                        style = AppTypography.numericSmall,
                        color = amountColor
                    )
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppColors.divider)
                )
            }
        }
    }
}
