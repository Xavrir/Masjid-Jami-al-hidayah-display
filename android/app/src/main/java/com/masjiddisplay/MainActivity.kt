package com.masjiddisplay

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.masjiddisplay.data.MockData
import com.masjiddisplay.ui.screens.MainDashboard
import com.masjiddisplay.ui.state.PrayerNotificationState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            MainDashboard(
                masjidConfig = MockData.masjidConfig,
                kasData = MockData.kasData,
                announcements = MockData.announcements,
                prayerNotificationState = PrayerNotificationState(),
                onPrayerStart = { },
                onKasDetailRequested = { },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
