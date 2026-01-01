package com.masjiddisplay.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.accentPrimary,
    secondary = AppColors.accentSecondary,
    background = AppColors.background,
    surface = AppColors.surfaceDefault,
    onPrimary = AppColors.textPrimary,
    onSecondary = AppColors.textPrimary,
    onBackground = AppColors.textPrimary,
    onSurface = AppColors.textPrimary
)

@Composable
fun MasjidDisplayTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AppColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
