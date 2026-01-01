package com.masjiddisplay

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.masjiddisplay.data.MockData
import com.masjiddisplay.data.Prayer
import com.masjiddisplay.services.SoundNotificationService
import com.masjiddisplay.ui.screens.MainDashboard
import com.masjiddisplay.ui.screens.PrayerInProgress
import com.masjiddisplay.ui.theme.MasjidDisplayTheme
import com.masjiddisplay.utils.PrayerTimeCalculator
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable immersive mode for TV display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            MasjidDisplayTheme {
                MasjidDisplayApp()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        SoundNotificationService.stopAlert()
    }
}

@Composable
fun MasjidDisplayApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var currentPrayer by remember { mutableStateOf<Prayer?>(null) }
    var forcePrayerDebug by remember { mutableStateOf(false) }
    
    when (currentScreen) {
        is Screen.Dashboard -> {
            MainDashboard(
                masjidConfig = MockData.masjidConfig,
                kasData = MockData.kasData,
                announcements = MockData.announcements,
                onPrayerStart = { prayer ->
                    val now = Date()
                    if (PrayerTimeCalculator.isWithinPrayerWindow(prayer, now)) {
                        forcePrayerDebug = false
                        currentPrayer = prayer
                        currentScreen = Screen.PrayerInProgress
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        is Screen.PrayerInProgress -> {
            currentPrayer?.let { prayer ->
                PrayerInProgress(
                    prayer = prayer.copy(status = com.masjiddisplay.data.PrayerStatus.CURRENT),
                    onComplete = {
                        forcePrayerDebug = false
                        currentScreen = Screen.Dashboard
                        currentPrayer = null
                    },
                    masjidName = MockData.masjidConfig.name,
                    masjidLocation = MockData.masjidConfig.location,
                    forceDebug = forcePrayerDebug,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

sealed class Screen {
    data object Dashboard : Screen()
    data object PrayerInProgress : Screen()
}
