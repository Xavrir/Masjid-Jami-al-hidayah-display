package com.masjiddisplay

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.masjiddisplay.ui.state.AlertType
import com.masjiddisplay.ui.state.rememberPrayerNotificationState
import com.masjiddisplay.ui.theme.MasjidDisplayTheme
import com.masjiddisplay.utils.PrayerTimeCalculator
import kotlinx.coroutines.delay
import java.util.*

class MainActivity : ComponentActivity() {
    
    private var soundService: SoundNotificationService? = null
    private var showTestPanel = mutableStateOf(false)
    
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
                MasjidDisplayApp(soundService, showTestPanel)
            }
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_INFO) {
            showTestPanel.value = !showTestPanel.value
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    
    override fun onStop() {
        super.onStop()
        soundService?.stopAlert()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        SoundNotificationServiceHolder.release()
    }
}

@Composable
fun MasjidDisplayApp(soundService: SoundNotificationService?, showTestPanel: MutableState<Boolean>) {
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
    
    val prayerNotificationState = rememberPrayerNotificationState(soundService)
    
    val testPrayers = remember {
        listOf(
            Prayer("Subuh", "04:30", "04:45", PrayerStatus.UPCOMING),
            Prayer("Dzuhur", "12:00", "12:15", PrayerStatus.UPCOMING),
            Prayer("Ashar", "15:30", "15:45", PrayerStatus.UPCOMING),
            Prayer("Maghrib", "18:15", "18:20", PrayerStatus.UPCOMING),
            Prayer("Isya", "19:30", "19:45", PrayerStatus.UPCOMING)
        )
    }

    LaunchedEffect(Unit) {
        try {
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
        
        KasDetailOverlay(
            visible = kasOverlayVisible,
            kasData = MockData.kasData,
            onClose = { kasOverlayVisible = false }
        )
        
        if (showTestPanel.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(Color(0xFF1A2A3A), RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TEST ALARM WAKTU SHOLAT",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Tekan tombol untuk test alarm:",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    testPrayers.forEach { prayer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prayer.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.width(100.dp)
                            )
                            
                            Button(
                                onClick = {
                                    prayerNotificationState.triggerDebugAlert(AlertType.ADHAN, prayer)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A085))
                            ) {
                                Text("ADZAN", fontSize = 12.sp)
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    prayerNotificationState.triggerDebugAlert(AlertType.IQAMAH, prayer)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                            ) {
                                Text("IQAMAH", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            soundService?.stopAlert()
                            prayerNotificationState.clearAlert()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                    ) {
                        Text("STOP ALARM", fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showTestPanel.value = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("TUTUP", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

sealed class Screen {
    data object Dashboard : Screen()
    data object PrayerInProgress : Screen()
}
