package com.masjiddisplay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.data.*
import com.masjiddisplay.services.SoundNotificationService
import com.masjiddisplay.ui.components.*
import com.masjiddisplay.ui.theme.*
import com.masjiddisplay.utils.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main dashboard screen displaying prayer times, clock, and Islamic content
 */
@Composable
fun MainDashboard(
    masjidConfig: MasjidConfig,
    kasData: KasData,
    announcements: List<String>,
    onPrayerStart: (Prayer) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }
    var prayers by remember { mutableStateOf<List<Prayer>>(emptyList()) }
    var tomorrowPrayers by remember { mutableStateOf<List<Prayer>>(emptyList()) }
    var nextPrayer by remember { mutableStateOf<Prayer?>(null) }
    var isNextPrayerTomorrow by remember { mutableStateOf(false) }
    var currentAlert by remember { mutableStateOf<Pair<String?, Prayer?>>(null to null) }
    
    // Track last triggered alerts to prevent duplicates
    var lastAdhanAlert by remember { mutableStateOf("") }
    var lastIqamahAlert by remember { mutableStateOf("") }
    
    // Initialize prayer times
    LaunchedEffect(Unit) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        prayers = PrayerTimeCalculator.calculatePrayerTimesForJakarta(today)
        tomorrowPrayers = PrayerTimeCalculator.calculatePrayerTimesForJakarta(tomorrow)
    }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }
    
    // Update prayer statuses
    LaunchedEffect(currentTime, prayers) {
        if (prayers.isNotEmpty()) {
            prayers = PrayerTimeCalculator.updatePrayerStatuses(prayers, currentTime)
            
            val allPassed = PrayerTimeCalculator.allPrayersPassed(prayers)
            val updatedTomorrowPrayers = if (allPassed && tomorrowPrayers.isNotEmpty()) {
                val tomorrowRef = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                }.time
                PrayerTimeCalculator.updatePrayerStatuses(tomorrowPrayers, currentTime)
            } else {
                tomorrowPrayers
            }
            
            nextPrayer = PrayerTimeCalculator.getNextPrayer(
                prayers,
                if (allPassed) updatedTomorrowPrayers else null
            )
            isNextPrayerTomorrow = allPassed && nextPrayer != null
            
            // Check for current prayer to trigger overlay
            val current = PrayerTimeCalculator.getCurrentPrayer(prayers)
            if (current != null) {
                onPrayerStart(current)
            }
        }
    }
    
    // Check for prayer alerts (adhan/iqamah time)
    LaunchedEffect(currentTime, prayers) {
        val currentTimeStr = formatTime(currentTime)
        val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime)
        
        for (prayer in prayers) {
            if (prayer.name.lowercase() in listOf("shuruq", "syuruq", "sunrise")) continue
            
            if (currentTimeStr == prayer.adhanTime) {
                val alertKey = "$currentDateStr-${prayer.name}-adhan"
                if (lastAdhanAlert != alertKey) {
                    lastAdhanAlert = alertKey
                    SoundNotificationService.playAdhanAlert()
                    currentAlert = "adhan" to prayer
                    delay(10000)
                    currentAlert = null to null
                }
                break
            }
            
            if (currentTimeStr == prayer.iqamahTime) {
                val alertKey = "$currentDateStr-${prayer.name}-iqamah"
                if (lastIqamahAlert != alertKey) {
                    lastIqamahAlert = alertKey
                    SoundNotificationService.playIqamahAlert()
                    currentAlert = "iqamah" to prayer
                    delay(15000)
                    currentAlert = null to null
                }
                break
            }
        }
    }
    
    val isRamadanPeriod = isRamadan(currentTime)
    
    // Combine announcements with kas info
    val announcementsWithKas = announcements + listOf(
        "Kas Masjid - Saldo: ${formatCurrency(kasData.balance)} | Pemasukan Bulan Ini: ${formatCurrency(kasData.incomeMonth)} | Pengeluaran Bulan Ini: ${formatCurrency(kasData.expenseMonth)}"
    )
    
    val isShowingTomorrowSchedule = PrayerTimeCalculator.allPrayersPassed(prayers) && tomorrowPrayers.isNotEmpty()
    val displayPrayers = if (isShowingTomorrowSchedule) tomorrowPrayers else prayers
    
    // Add Shuruq to display prayers
    val shuruqTime = PrayerTimeCalculator.calculateShuruqTimeForJakarta(
        if (isShowingTomorrowSchedule) {
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        } else {
            currentTime
        }
    )
    
    val displayPrayersCompact = remember(displayPrayers, shuruqTime) {
        val shuruqPrayer = Prayer(
            name = "Shuruq",
            adhanTime = shuruqTime,
            iqamahTime = "—",
            status = PrayerStatus.UPCOMING
        )
        
        val subuhIndex = displayPrayers.indexOfFirst { it.name.lowercase() == "subuh" }
        if (subuhIndex == -1) {
            listOf(shuruqPrayer) + displayPrayers
        } else {
            displayPrayers.subList(0, subuhIndex + 1) + 
            listOf(shuruqPrayer) + 
            displayPrayers.subList(subuhIndex + 1, displayPrayers.size)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.backgroundGradientTop.copy(alpha = 0.92f),
                        AppColors.backgroundGradientBottom.copy(alpha = 0.95f)
                    )
                )
            )
            .padding(top = Spacing.lg, bottom = Spacing.lg, start = Spacing.xxl, end = Spacing.xxl)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Alert banner
            currentAlert.first?.let { alertType ->
                currentAlert.second?.let { prayer ->
                    PrayerAlertBanner(
                        type = alertType,
                        prayer = prayer
                    )
                }
            }
            
            // Header section
            Column(
                modifier = Modifier.padding(bottom = Spacing.lg)
            ) {
                // Header top row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = Spacing.lg, vertical = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left - Masjid name
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .background(AppColors.accentPrimary)
                        )
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column {
                            Text(
                                text = masjidConfig.name.uppercase(),
                                style = AppTypography.headlineM.copy(letterSpacing = 2.sp),
                                color = AppColors.textPrimary
                            )
                            masjidConfig.tagline?.let { tagline ->
                                Text(
                                    text = tagline,
                                    style = AppTypography.bodyS.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                    color = AppColors.textSecondary.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                    
                    // Center - Clock
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatTimeWithSeconds(currentTime),
                            style = AppTypography.displayL,
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = formatGregorianDate(currentTime),
                            style = AppTypography.bodyM,
                            color = AppColors.textSecondary
                        )
                        Text(
                            text = getHijriDate(currentTime),
                            style = AppTypography.bodyS,
                            color = AppColors.accentPrimary
                        )
                    }
                    
                    // Right - Badges
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        if (isRamadanPeriod) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radii.small))
                                    .background(AppColors.accentPrimarySoft.copy(alpha = 0.15f))
                                    .border(1.dp, AppColors.accentPrimary, RoundedCornerShape(Radii.small))
                                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.accentPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Text(
                                        text = "RAMADAN KAREEM",
                                        style = AppTypography.caption,
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.xl))
                
                // Compact prayer schedule
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    displayPrayersCompact.forEach { prayer ->
                        val isTheNextPrayer = nextPrayer?.name == prayer.name
                        val isCurrent = prayer.status == PrayerStatus.CURRENT
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Radii.small))
                                .background(
                                    if (isCurrent) AppColors.accentPrimarySoft.copy(alpha = 0.25f)
                                    else AppColors.surfaceGlass.copy(alpha = 0.6f)
                                )
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) AppColors.accentPrimary
                                           else if (isTheNextPrayer) AppColors.accentPrimary
                                           else AppColors.accentPrimarySoft.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(Radii.small)
                                )
                                .padding(vertical = Spacing.md, horizontal = Spacing.sm)
                                .heightIn(min = 72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = prayer.name,
                                    style = AppTypography.bodyS.copy(fontSize = 13.sp),
                                    color = if (isCurrent) AppColors.accentPrimary else AppColors.textSecondary
                                )
                                Text(
                                    text = prayer.adhanTime,
                                    style = AppTypography.numericSmall.copy(fontSize = 20.sp),
                                    color = if (isCurrent) AppColors.accentPrimary else AppColors.textPrimary
                                )
                                Text(
                                    text = prayer.iqamahTime,
                                    style = AppTypography.caption.copy(fontSize = 11.sp),
                                    color = AppColors.textMuted
                                )
                            }
                        }
                    }
                }
            }
            
            // Core content - Three columns
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = 0.dp)
                    .padding(bottom = Spacing.xxl),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Next prayer card
                NextPrayerCard(
                    prayer = nextPrayer,
                    isTomorrow = isNextPrayerTomorrow,
                    modifier = Modifier.weight(1f)
                )
                
                // Quran verse card
                QuranVerseCard(
                    autoRotate = true,
                    rotationInterval = 40000,
                    modifier = Modifier.weight(1f)
                )
                
                // Hadith card
                HadithCard(
                    autoRotate = true,
                    rotationInterval = 50000,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Announcement ticker
            AnnouncementTicker(
                announcements = announcementsWithKas,
                speed = "slow",
                modifier = Modifier.padding(horizontal = Spacing.lg)
            )
        }
    }
}
