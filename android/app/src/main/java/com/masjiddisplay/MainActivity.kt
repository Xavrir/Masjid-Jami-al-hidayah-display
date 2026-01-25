package com.masjiddisplay

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.masjiddisplay.data.MockData
import com.masjiddisplay.data.Prayer
import com.masjiddisplay.data.PrayerStatus
import com.masjiddisplay.data.SupabaseRepository
import com.masjiddisplay.data.KasData
import com.masjiddisplay.services.SoundNotificationService
import com.masjiddisplay.services.SoundNotificationServiceHolder
import com.masjiddisplay.ui.components.KasDetailOverlay
import com.masjiddisplay.ui.screens.MainDashboard
import com.masjiddisplay.ui.screens.PrayerInProgress
import com.masjiddisplay.ui.state.rememberPrayerNotificationState
import com.masjiddisplay.ui.theme.MasjidDisplayTheme
import com.masjiddisplay.utils.PrayerTimeCalculator
import kotlinx.coroutines.delay
import java.util.*

class MainActivity : ComponentActivity() {
    
    private var soundService: SoundNotificationService? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize sound service
        soundService = SoundNotificationServiceHolder.getInstance(this)
        
        // Enable immersive mode for TV display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            MasjidDisplayTheme {
                MasjidDisplayApp(soundService)
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        // Stop any playing alert when activity is stopped
        soundService?.stopAlert()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Full cleanup only on destroy
        SoundNotificationServiceHolder.release()
    }
}

@Composable
fun MasjidDisplayApp(soundService: SoundNotificationService?) {
    val context = LocalContext.current
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var currentPrayer by remember { mutableStateOf<Prayer?>(null) }
    var forcePrayerDebug by remember { mutableStateOf(false) }
    var kasOverlayVisible by remember { mutableStateOf(false) }
    var appClock by remember { mutableStateOf(Date()) }
    
    // Data State from Supabase
    var kasData by remember { mutableStateOf(MockData.kasData) }
    var quranVerses by remember { mutableStateOf<List<String>>(emptyList()) }
    var hadiths by remember { mutableStateOf<List<String>>(emptyList()) }
    var pengajian by remember { mutableStateOf<List<String>>(emptyList()) }

    // Fetch data from Supabase
    LaunchedEffect(Unit) {
        try {
            // Run fetches in parallel or sequence
            kasData = SupabaseRepository.getKasData()
            
            val fetchedQuran = SupabaseRepository.getQuranVerses()
            quranVerses = fetchedQuran.map { "QS ${it.surah} (${it.surahNumber}):${it.ayah} - ${it.translation}" }
            
            val fetchedHadiths = SupabaseRepository.getHadiths()
            hadiths = fetchedHadiths.map { "${it.source}: ${it.translation}" }
            
            val fetchedPengajian = SupabaseRepository.getPengajian()
            pengajian = fetchedPengajian.map { "${it.judul} oleh ${it.pembicara} (${it.hari}, ${it.jam})" }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Initialize prayer notification state
    val prayerNotificationState = rememberPrayerNotificationState(soundService)
    
    // Update clock every second
    LaunchedEffect(Unit) {
        while (true) {
            appClock = Date()
            delay(1000)
        }
    }
    
    // Auto-hide prayer overlay when outside prayer window
    LaunchedEffect(appClock, currentPrayer, currentScreen, forcePrayerDebug) {
        if (currentPrayer == null || currentScreen != Screen.PrayerInProgress || forcePrayerDebug) {
            return@LaunchedEffect
        }
        
        currentPrayer?.let { prayer ->
            if (!PrayerTimeCalculator.isWithinPrayerWindow(prayer, appClock)) {
                forcePrayerDebug = false
                currentScreen = Screen.Dashboard
                currentPrayer = null
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            is Screen.Dashboard -> {
                MainDashboard(
                    masjidConfig = MockData.masjidConfig,
                    kasData = kasData,
                    announcements = MockData.announcements,
                    quranVerses = quranVerses,
                    hadiths = hadiths,
                    pengajian = pengajian,
                    onPrayerStart = { prayer ->
                        val now = Date()
                        if (PrayerTimeCalculator.isWithinPrayerWindow(prayer, now)) {
                            forcePrayerDebug = false
                            currentPrayer = prayer
                            currentScreen = Screen.PrayerInProgress
                        }
                    },
                    onKasDetailRequested = {
                        kasOverlayVisible = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            is Screen.PrayerInProgress -> {
                currentPrayer?.let { prayer ->
                    PrayerInProgress(
                        prayer = prayer.copy(status = PrayerStatus.CURRENT),
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
        
        // Kas Detail Overlay
        KasDetailOverlay(
            visible = kasOverlayVisible,
            kasData = MockData.kasData,
            onClose = { kasOverlayVisible = false }
        )
    }
}

sealed class Screen {
    data object Dashboard : Screen()
    data object PrayerInProgress : Screen()
}
