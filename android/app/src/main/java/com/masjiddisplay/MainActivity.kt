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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.masjiddisplay.data.MasjidConfig
import com.masjiddisplay.data.Prayer
import com.masjiddisplay.data.PrayerStatus
import com.masjiddisplay.data.SupabaseRepository
import com.masjiddisplay.data.BannerRemote
import com.masjiddisplay.data.KasData
import com.masjiddisplay.data.TrendDirection
import com.masjiddisplay.services.SoundNotificationService
import com.masjiddisplay.services.SoundNotificationServiceHolder
import com.masjiddisplay.ui.components.KasDetailOverlay
import com.masjiddisplay.ui.components.PrayerAlertOverlay
import com.masjiddisplay.ui.components.OverlayType
import com.masjiddisplay.ui.screens.MainDashboard
import com.masjiddisplay.ui.theme.MasjidDisplayTheme
import com.masjiddisplay.utils.PrayerTimeCalculator
import com.masjiddisplay.utils.jakartaDateFormat
import com.masjiddisplay.utils.jakartaCalendar
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    val masjidConfig = remember {
        MasjidConfig(
            name = "Masjid Jami' Al-Hidayah",
            location = "Jl. Tanah Merdeka II No.8, Rambutan, Ciracas, Jakarta Timur 13830",
            tagline = "Memakmurkan Masjid, Mencerahkan Umat",
            latitude = -6.3092124,
            longitude = 106.8816386,
            calculationMethod = "Kemenag RI"
        )
    }
    var kasOverlayVisible by remember { mutableStateOf(false) }
    var appClock by remember { mutableStateOf(Date()) }
    
    var prayerAlertVisible by remember { mutableStateOf(false) }
    var currentOverlayType by remember { mutableStateOf(OverlayType.ADHAN) }
    var alertPrayer by remember { mutableStateOf<Prayer?>(null) }
    val currentDateKey = remember(appClock) {
        jakartaDateFormat("yyyy-MM-dd", Locale.ROOT).format(appClock)
    }
    
    var lastAdhanAlertKey by remember { mutableStateOf("") }
    var lastIqamahAlertKey by remember { mutableStateOf("") }
    var lastFridayReminderKey by remember { mutableStateOf("") }
    var lastPreAdhanAlertKey by remember { mutableStateOf("") }
    
    var prayers by remember { mutableStateOf<List<Prayer>>(emptyList()) }
    
    var kasData by remember { mutableStateOf(KasData(
        balance = 0L,
        incomeMonth = 0L,
        expenseMonth = 0L,
        trendDirection = TrendDirection.FLAT,
        recentTransactions = emptyList(),
        trendData = emptyList()
    )) }
    var quranVerses by remember { mutableStateOf<List<String>>(emptyList()) }
    var hadiths by remember { mutableStateOf<List<String>>(emptyList()) }
    var pengajian by remember { mutableStateOf<List<String>>(emptyList()) }
    var banners by remember { mutableStateOf<List<BannerRemote>>(emptyList()) }
    var fridayReminderAnnouncement by remember { mutableStateOf<String?>(null) }
    
    val testPrayers = remember {
        listOf(
            Prayer("Subuh", "04:30", "04:40", PrayerStatus.UPCOMING),
            Prayer("Dzuhur", "12:00", "12:10", PrayerStatus.UPCOMING),
            Prayer("Ashar", "15:30", "15:40", PrayerStatus.UPCOMING),
            Prayer("Maghrib", "18:15", "18:20", PrayerStatus.UPCOMING),
            Prayer("Isya", "19:30", "19:40", PrayerStatus.UPCOMING)
        )
    }

    LaunchedEffect(Unit) {
        while (currentCoroutineContext().isActive) {
            try {
                kasData = SupabaseRepository.getKasData()
                
                val fetchedQuran = SupabaseRepository.getQuranVerses()
                quranVerses = fetchedQuran.map { 
                    val text = it.translation ?: it.transliteration ?: it.arabic
                    "QS ${it.surah} (${it.surahNumber}):${it.ayah} - $text"
                }
                
                val fetchedHadiths = SupabaseRepository.getHadiths()
                hadiths = fetchedHadiths.map { 
                    val text = it.translation ?: it.arabic
                    "${it.source}: $text"
                }
                
                val fetchedPengajian = SupabaseRepository.getPengajian()
                pengajian = fetchedPengajian
                    .filter { (it.judul ?: it.tema) != null && (it.pembicara ?: it.ustadz) != null }
                    .map { 
                        val title = it.judul ?: it.tema ?: ""
                        val speaker = it.pembicara ?: it.ustadz ?: ""
                        val schedule = it.hari ?: it.tanggal ?: "-"
                        "$title oleh $speaker ($schedule, ${it.jam ?: "-"})"
                    }
                
                banners = SupabaseRepository.getBanners()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(30L * 60 * 1000)
        }
    }
    
    LaunchedEffect(Unit) {
        while (currentCoroutineContext().isActive) {
            appClock = Date()
            delay(1000)
        }
    }
    
    LaunchedEffect(currentDateKey) {
        val today = jakartaCalendar(appClock).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        prayers = PrayerTimeCalculator.calculatePrayerTimesForJakarta(today)
    }
    
    LaunchedEffect(appClock, prayers) {
        val cal = jakartaCalendar(appClock)
        val isFriday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        
        if (isFriday && prayers.isNotEmpty()) {
            val dzuhur = prayers.find { it.name.lowercase() in listOf("dzuhur", "jumat") }
            if (dzuhur != null) {
                val dzuhurParts = dzuhur.adhanTime.split(":")
                if (dzuhurParts.size == 2) {
                    val dzuhurHour = dzuhurParts[0].toIntOrNull() ?: 12
                    val dzuhurMinute = dzuhurParts[1].toIntOrNull() ?: 0
                    
                    val currentHour = cal.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = cal.get(Calendar.MINUTE)
                    val currentTotalMinutes = currentHour * 60 + currentMinute
                    val dzuhurTotalMinutes = dzuhurHour * 60 + dzuhurMinute
                    val minutesUntilJumat = dzuhurTotalMinutes - currentTotalMinutes
                    
                    fridayReminderAnnouncement = if (minutesUntilJumat in 1..10) {
                        "🕌 Shalat Jumat dalam $minutesUntilJumat menit! Persiapkan diri untuk menunaikan ibadah Jumat."
                    } else {
                        null
                    }
                }
            }
        } else {
            fridayReminderAnnouncement = null
        }
    }
    
    val staticReminders = listOf(
        "Mohon nonaktifkan atau membisukan ponsel sebelum shalat",
        "Mari rapatkan shaf dan luruskan barisan saat shalat berjamaah",
        "Jagalah kebersihan masjid, tempat ibadah kita bersama"
    )
    
    val effectiveAnnouncements = remember(fridayReminderAnnouncement) {
        if (fridayReminderAnnouncement != null) {
            listOf(fridayReminderAnnouncement!!) + staticReminders
        } else {
            staticReminders
        }
    }
    
    val socialMediaLinks = remember {
        listOf(
            "Instagram @kurmaalhidayah",
            "Instagram @masjidalhidayah.tanahmerdeka",
            "YouTube Masjidalhidayah.tanahmerdeka",
            "TikTok @kurmaalhidayahofficial"
        )
    }
    
    LaunchedEffect(appClock, prayers) {
        if (prayers.isEmpty() || prayerAlertVisible) return@LaunchedEffect
        
        val cal = jakartaCalendar(appClock)
        val currentTimeStr = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        val currentDateStr = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        val isFriday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        
         for (prayer in prayers) {
             val prayerName = prayer.name.lowercase()
             if (prayerName in listOf("shuruq", "syuruq", "sunrise", "imsak")) continue
             
             // Check for 3-minute pre-adhan alert
             val preAdhanParts = prayer.adhanTime.split(":")
             if (preAdhanParts.size == 2) {
                 val pHour = preAdhanParts[0].toIntOrNull() ?: 0
                 val pMinute = preAdhanParts[1].toIntOrNull() ?: 0
                 var preMinute = pMinute - 3
                 var preHour = pHour
                 if (preMinute < 0) { preMinute += 60; preHour -= 1 }
                 if (preHour < 0) preHour += 24
                 val preAdhanTime = "%02d:%02d".format(preHour, preMinute)
                 if (currentTimeStr == preAdhanTime) {
                     val alertKey = "$currentDateStr-${prayer.name}-preadhan-$preAdhanTime"
                     if (lastPreAdhanAlertKey != alertKey) {
                         lastPreAdhanAlertKey = alertKey
                         alertPrayer = prayer
                         currentOverlayType = OverlayType.PRE_ADHAN
                         prayerAlertVisible = true
                         break
                     }
                 }
             }
             
             if (currentTimeStr == prayer.adhanTime) {
                val alertKey = "$currentDateStr-${prayer.name}-adhan-$currentTimeStr"
                if (lastAdhanAlertKey != alertKey) {
                    lastAdhanAlertKey = alertKey
                    alertPrayer = prayer
                    currentOverlayType = OverlayType.ADHAN
                    prayerAlertVisible = true
                    soundService?.playAdhanAlert()
                    break
                }
            }
            
            if (currentTimeStr == prayer.iqamahTime) {
                val alertKey = "$currentDateStr-${prayer.name}-iqamah-$currentTimeStr"
                if (lastIqamahAlertKey != alertKey) {
                    lastIqamahAlertKey = alertKey
                    alertPrayer = prayer
                    currentOverlayType = OverlayType.IQAMAH
                    prayerAlertVisible = true
                    soundService?.playIqamahAlert()
                    break
                }
            }
            
            if (isFriday && prayerName in listOf("dzuhur", "jumat")) {
                val dzuhurParts = prayer.adhanTime.split(":")
                if (dzuhurParts.size == 2) {
                    val dzuhurHour = dzuhurParts[0].toIntOrNull() ?: 12
                    val dzuhurMinute = dzuhurParts[1].toIntOrNull() ?: 0
                    var reminderMinute = dzuhurMinute - 10
                    var reminderHour = dzuhurHour
                    if (reminderMinute < 0) {
                        reminderMinute += 60
                        reminderHour -= 1
                    }
                    val fridayReminderTime = "%02d:%02d".format(reminderHour, reminderMinute)
                    
                    if (currentTimeStr == fridayReminderTime) {
                        val alertKey = "$currentDateStr-friday-reminder-$fridayReminderTime"
                        if (lastFridayReminderKey != alertKey) {
                            lastFridayReminderKey = alertKey
                            alertPrayer = prayer
                            currentOverlayType = OverlayType.FRIDAY_REMINDER
                            prayerAlertVisible = true
                            soundService?.playAdhanAlert()
                            break
                        }
                    }
                }
            }
        }
    }

     LaunchedEffect(prayerAlertVisible, currentOverlayType) {
         if (prayerAlertVisible) {
             val duration = when (currentOverlayType) {
                 OverlayType.ADHAN -> 60_000L
                 OverlayType.IQAMAH -> 60_000L
                 OverlayType.FRIDAY_REMINDER -> 10_000L
                 OverlayType.PRE_ADHAN -> 15_000L
             }
             delay(duration)
             prayerAlertVisible = false
         }
     }
    
    Box(modifier = Modifier.fillMaxSize()) {
        MainDashboard(
            masjidConfig = masjidConfig,
            kasData = kasData,
            announcements = effectiveAnnouncements,
            quranVerses = quranVerses,
            hadiths = hadiths,
            pengajian = pengajian,
            socialMedia = socialMediaLinks,
            banners = banners,
            onPrayerStart = { },
            onKasDetailRequested = {
                kasOverlayVisible = true
            },
            modifier = Modifier.fillMaxSize()
        )
        
        KasDetailOverlay(
            visible = kasOverlayVisible,
            kasData = kasData,
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
                                    alertPrayer = prayer
                                    currentOverlayType = OverlayType.ADHAN
                                    prayerAlertVisible = true
                                    soundService?.playAdhanAlert()
                                    showTestPanel.value = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A085))
                            ) {
                                Text("ADZAN", fontSize = 12.sp)
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    alertPrayer = prayer
                                    currentOverlayType = OverlayType.IQAMAH
                                    prayerAlertVisible = true
                                    soundService?.playIqamahAlert()
                                    showTestPanel.value = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                            ) {
                                Text("IQAMAH", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            alertPrayer = testPrayers[1]
                            currentOverlayType = OverlayType.FRIDAY_REMINDER
                            prayerAlertVisible = true
                            soundService?.playAdhanAlert()
                            showTestPanel.value = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TEST REMINDER JUMAT", fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            soundService?.stopAlert()
                            prayerAlertVisible = false
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
        
        PrayerAlertOverlay(
            visible = prayerAlertVisible,
            overlayType = currentOverlayType,
            prayer = alertPrayer,
            onDismiss = {
                prayerAlertVisible = false
                soundService?.stopAlert()
            },
            modifier = Modifier.padding(bottom = 0.dp) // Reset any potential padding
        )
    }
}
