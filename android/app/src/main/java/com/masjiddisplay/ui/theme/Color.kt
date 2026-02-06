package com.masjiddisplay.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Application color palette - dark luxurious theme for mosque display
 */
object AppColors {
    val background = Color(0xFF020712)
    val backgroundGradientTop = Color(0xFF020712)
    val backgroundGradientBottom = Color(0xFF041622)
    val surfaceDefault = Color(0xFF07121E)
    val surfaceElevated = Color(0xFF0C1926)
    val surfaceGlass = Color(0xBF0C1926)
    
    val accentPrimary = Color(0xFFD4AF37)
    val accentPrimarySoft = Color(0x47D4AF37)
    val accentSecondary = Color(0xFF16A085)
    val accentSecondarySoft = Color(0x2E16A085)
    
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFC1CEDB)
    val textMuted = Color(0xFF7E8BA3)
    val textInverse = Color(0xFF000000)
    
    val divider = Color(0x0FFFFFFF)
    val borderSubtle = Color(0x1FFFFFFF)
    
    val prayerCurrent = Color(0xFFD4AF37)
    val prayerUpcoming = Color(0xFF16A085)
    val prayerPassed = Color(0xFF4C576A)
    
    val kasPositive = Color(0xFF27AE60)
    val kasNegative = Color(0xFFE74C3C)
    val kasNeutral = Color(0xFFBDC3C7)
    
    val badgeInfo = Color(0x4234A2DB)
    val badgeWarning = Color(0x42F1C40F)
    val bannerAlertBackground = Color(0x59C0392B)
    
    val successColor = Color(0xFF27AE60)
    val warningColor = Color(0xFFF39C12)
    val infoColor = Color(0xFF3498DB)
    val errorColor = Color(0xFFE74C3C)
}

/**
 * Ramadan-specific color overrides
 */
object RamadanColors {
    val accentPrimary = Color(0xFF11C76F)
    val accentPrimarySoft = Color(0x5211C76F)
}

/**
 * Timeline colors - switches between normal (teal) and Ramadan (green)
 */
object TimelineColors {
    val normal = Color(0xFF4ECDC4)           // Teal
    val normalSoft = Color(0x804ECDC4)       // 50% alpha
    val normalFaint = Color(0x4D4ECDC4)      // 30% alpha
    val ramadhan = RamadanColors.accentPrimary  // Green
    val ramadhanSoft = Color(0x8011C76F)     // 50% alpha
    val ramadhanFaint = Color(0x4D11C76F)    // 30% alpha
}
