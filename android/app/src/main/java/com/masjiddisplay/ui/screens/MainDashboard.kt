package com.masjiddisplay.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.R
import com.masjiddisplay.data.*
import com.masjiddisplay.data.BannerRemote
import com.masjiddisplay.ui.components.*
import com.masjiddisplay.ui.theme.*
import com.masjiddisplay.utils.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.*

@Composable
fun MainDashboard(
    masjidConfig: MasjidConfig,
    kasData: KasData,
    announcements: List<String>,
    quranVerses: List<String> = emptyList(),
    hadiths: List<String> = emptyList(),
    pengajian: List<String> = emptyList(),
    socialMedia: List<String> = emptyList(),
    banners: List<BannerRemote> = emptyList(),
    onPrayerStart: (Prayer) -> Unit = {},
    onKasDetailRequested: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }
    var prayers by remember { mutableStateOf<List<Prayer>>(emptyList()) }
    var tomorrowPrayers by remember { mutableStateOf<List<Prayer>>(emptyList()) }
    var nextPrayer by remember { mutableStateOf<Prayer?>(null) }
    var shuruqTime by remember { mutableStateOf("05:55") }
    var imsakTime by remember { mutableStateOf("04:24") }
    val isRamadhanNow = remember(currentTime) { isRamadan(currentTime) }
    val currentDateKey = remember(currentTime) {
        jakartaDateFormat("yyyy-MM-dd", Locale.ROOT).format(currentTime)
    }
    
    LaunchedEffect(currentDateKey) {
        val today = jakartaCalendar(currentTime).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val tomorrow = jakartaCalendar(today).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        prayers = PrayerTimeCalculator.calculatePrayerTimesForJakarta(today)
        tomorrowPrayers = PrayerTimeCalculator.calculatePrayerTimesForJakarta(tomorrow)
        shuruqTime = PrayerTimeCalculator.calculateShuruqTimeForJakarta(today)
        imsakTime = PrayerTimeCalculator.calculateImsakTimeForJakarta(today)
    }
    
    LaunchedEffect(Unit) {
        while (currentCoroutineContext().isActive) {
            currentTime = Date()
            delay(1000)
        }
    }
    
    LaunchedEffect(currentTime, prayers) {
        if (prayers.isNotEmpty()) {
            prayers = PrayerTimeCalculator.updatePrayerStatuses(prayers, currentTime)
            
            val allPassed = PrayerTimeCalculator.allPrayersPassed(prayers)
            val updatedTomorrowPrayers = if (allPassed && tomorrowPrayers.isNotEmpty()) {
                val tomorrowRef = jakartaCalendar().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                }.time
                PrayerTimeCalculator.updatePrayerStatuses(tomorrowPrayers, currentTime, tomorrowRef)
            } else {
                tomorrowPrayers
            }
            
            nextPrayer = PrayerTimeCalculator.getNextPrayer(
                prayers,
                if (allPassed) updatedTomorrowPrayers else null
            )
            
            val current = PrayerTimeCalculator.getCurrentPrayer(prayers)
            if (current != null && current.name.lowercase() != "imsak") {
                onPrayerStart(current)
            }
        }
    }
    
    val countdownText = remember(currentTime, nextPrayer) {
        nextPrayer?.let { prayer ->
            val timeToNext = calculateTimeUntilPrayer(prayer, currentTime)
            if (timeToNext.isNotEmpty()) timeToNext else "—"
        } ?: "—"
    }
    
    val mainPrayers = remember(prayers, isRamadhanNow, shuruqTime) {
        if (isRamadhanNow) {
            prayers.filter { 
                it.name.lowercase() !in listOf("shuruq", "syuruq", "sunrise") 
            }
        } else {
            val filtered = prayers.filter { 
                it.name.lowercase() !in listOf("shuruq", "syuruq", "sunrise", "imsak") 
            }.toMutableList()
            val subuhIndex = filtered.indexOfFirst { it.name.lowercase() in listOf("subuh", "fajr") }
            if (subuhIndex >= 0) {
                val syuruqPrayer = Prayer(
                    name = "Syuruq",
                    adhanTime = shuruqTime,
                    iqamahTime = shuruqTime,
                    status = PrayerStatus.UPCOMING
                )
                filtered.add(subuhIndex + 1, syuruqPrayer)
            }
            filtered
        }
    }
    
    val cornerLabel = if (isRamadhanNow) "SYURUQ" else "IMSAK"
    val cornerTime = if (isRamadhanNow) shuruqTime else imsakTime
    val cornerEmoji = if (isRamadhanNow) "☀️" else "🌙"
    val cornerColor = if (isRamadhanNow) Color(0xFFFFA500) else Color(0xFF9C88FF)
    
    val shouldShowBanners = remember(currentTime, prayers, banners) {
        if (banners.isEmpty() || prayers.isEmpty()) return@remember false
        val now = jakartaCalendar(currentTime)
        prayers.any { prayer ->
            val name = prayer.name.lowercase()
            if (name in listOf("imsak", "shuruq", "syuruq", "sunrise")) return@any false
            val iqamahCal = parseTimeToCalendar(prayer.iqamahTime, currentTime)
            val startCal = (iqamahCal.clone() as Calendar).apply { add(Calendar.MINUTE, 15) }
            val endCal = (iqamahCal.clone() as Calendar).apply { add(Calendar.MINUTE, 25) }
            now.timeInMillis >= startCal.timeInMillis && now.timeInMillis < endCal.timeInMillis
        }
    }

    val bannerIntervalMs = remember(banners) {
        if (banners.isEmpty()) 8000L
        else (10L * 60 * 1000) / banners.size.coerceAtLeast(1)
    }
    
    val kasItems = listOf(
        "Saldo: ${formatCurrency(kasData.balance)} | Pemasukan Bulan Ini: ${formatCurrency(kasData.incomeMonth)} | Pengeluaran Bulan Ini: ${formatCurrency(kasData.expenseMonth)}"
    )
    
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.beautiful_wallpapers_for_laptop),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        
        if (isRamadhanNow) {
            val infiniteTransition = rememberInfiniteTransition(label = "ketupat_sway")
            val swayDp by infiniteTransition.animateFloat(
                initialValue = -12f,
                targetValue = 12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(5000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sway_offset"
            )
            val density = LocalDensity.current
            val swayPx = with(density) { swayDp.dp.toPx() }

            Image(
                painter = painterResource(id = R.drawable.ic_ramadhan_ketupat),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 24.dp)
                    .alpha(0.08f)
                    .graphicsLayer {
                        translationX = swayPx
                    }
            )
            Image(
                painter = painterResource(id = R.drawable.ic_ramadhan_ketupat),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-24).dp, y = 24.dp)
                    .alpha(0.08f)
                    .graphicsLayer {
                        scaleX = -1f
                        translationX = -swayPx // Inverted for symmetry
                    }
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_masjid_logo),
                            contentDescription = "Logo Masjid",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = masjidConfig.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatDayDate(currentTime).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = getHijriDate(currentTime).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatTimeAmPm(currentTime),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = cornerEmoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$cornerLabel $cornerTime",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = cornerColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!shouldShowBanners) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                val timelineColor = if (isRamadhanNow) TimelineColors.ramadhan else TimelineColors.normal
                val timelineSoftColor = if (isRamadhanNow) TimelineColors.ramadhanSoft else TimelineColors.normalSoft
                val timelineFaintColor = if (isRamadhanNow) TimelineColors.ramadhanFaint else TimelineColors.normalFaint
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MENUJU",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = countdownText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                    ) {
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                        drawLine(
                            color = timelineColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 3f,
                            pathEffect = dashEffect,
                            cap = StrokeCap.Round
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        mainPrayers.forEach { prayer ->
                            val isPassed = prayer.status == PrayerStatus.PASSED
                            val isCurrent = prayer.status == PrayerStatus.CURRENT
                            val isNext = nextPrayer?.name == prayer.name
                            
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent || isNext) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> timelineColor
                                            isNext -> timelineColor
                                            isPassed -> timelineSoftColor
                                            else -> timelineFaintColor
                                        }
                                    )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    mainPrayers.forEach { prayer ->
                        val isCurrent = prayer.status == PrayerStatus.CURRENT
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = getPrayerIconRes(prayer.name)),
                                contentDescription = prayer.name,
                                modifier = Modifier.size(48.dp),
                                tint = if (isCurrent) timelineColor else Color.White.copy(alpha = 0.8f)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = prayer.name.uppercase(),
                                fontSize = 20.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) Color(0xFFFFA500) else Color.White,
                                letterSpacing = 1.sp
                            )
                            
                            Text(
                                text = prayer.adhanTime,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                }
                }
            }
            
            MultiSourceRunningText(
                announcements = announcements,
                kasItems = kasItems,
                quranVerses = quranVerses,
                hadiths = hadiths,
                pengajian = pengajian,
                socialMedia = socialMedia
            )
        }

        if (shouldShowBanners) {
            BannerSlideshow(
                banners = banners,
                intervalMs = bannerIntervalMs,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun getPrayerIconRes(prayerName: String): Int {
    return when (prayerName.lowercase()) {
        "imsak" -> R.drawable.ic_imsak
        "subuh", "fajr" -> R.drawable.ic_subuh
        "syuruq", "shuruq", "sunrise" -> R.drawable.ic_syuruq
        "dzuhur", "dhuhr", "zuhur", "jumat" -> R.drawable.ic_dzuhur
        "ashar", "asr" -> R.drawable.ic_ashar
        "maghrib" -> R.drawable.ic_maghrib
        "isya", "isha" -> R.drawable.ic_isya
        else -> R.drawable.ic_subuh
    }
}

private fun formatTimeAmPm(date: Date): String {
    val sdf = jakartaDateFormat("HH:mm", Locale("id", "ID"))
    return sdf.format(date)
}

private fun formatDayDate(date: Date): String {
    val sdf = jakartaDateFormat("EEEE, d MMMM", Locale("id", "ID"))
    return sdf.format(date)
}

private fun calculateTimeUntilPrayer(prayer: Prayer, currentTime: Date): String {
    try {
        val timeParts = prayer.adhanTime.split(":")
        if (timeParts.size != 2) return "—"
        val hour = timeParts.getOrNull(0)?.trim()?.toIntOrNull() ?: return "—"
        val minute = timeParts.getOrNull(1)?.trim()?.toIntOrNull() ?: return "—"
        if (hour !in 0..23 || minute !in 0..59) return "—"
        
        val prayerCal = jakartaCalendar(currentTime).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        
        if (prayerCal.time.before(currentTime)) {
            prayerCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val diffMs = prayerCal.timeInMillis - currentTime.time
        val hours = diffMs / (1000 * 60 * 60)
        val minutes = (diffMs % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}j ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Sekarang"
        }
    } catch (e: Exception) {
        return "—"
    }
}
