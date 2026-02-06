package com.masjiddisplay.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.R
import com.masjiddisplay.data.*
import com.masjiddisplay.ui.components.*
import com.masjiddisplay.ui.theme.*
import com.masjiddisplay.utils.*
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun MainDashboard(
    masjidConfig: MasjidConfig,
    kasData: KasData,
    announcements: List<String>,
    quranVerses: List<String> = emptyList(),
    hadiths: List<String> = emptyList(),
    pengajian: List<String> = emptyList(),
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
    
    LaunchedEffect(Unit) {
        val today = jakartaCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val tomorrow = jakartaCalendar().apply {
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
        while (true) {
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = masjidConfig.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = formatDayDate(currentTime).uppercase(),
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
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "UNTIL",
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
                            color = Color(0xFF4ECDC4),
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
                                            isCurrent -> Color(0xFF4ECDC4)
                                            isNext -> Color(0xFF4ECDC4)
                                            isPassed -> Color(0xFF4ECDC4).copy(alpha = 0.5f)
                                            else -> Color(0xFF4ECDC4).copy(alpha = 0.3f)
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
                                modifier = Modifier.size(32.dp),
                                tint = if (isCurrent) Color(0xFF4ECDC4) else Color.White.copy(alpha = 0.8f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = prayer.name.uppercase(),
                                fontSize = 14.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) Color(0xFFFFA500) else Color.White,
                                letterSpacing = 1.sp
                            )
                            
                            Text(
                                text = prayer.adhanTime,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            MultiSourceRunningText(
                announcements = announcements,
                kasItems = kasItems,
                quranVerses = quranVerses,
                hadiths = hadiths,
                pengajian = pengajian
            )
        }
    }
}

private fun getPrayerIconRes(prayerName: String): Int {
    return when (prayerName.lowercase()) {
        "imsak" -> R.drawable.ic_imsak
        "subuh", "fajr" -> R.drawable.ic_subuh
        "syuruq", "shuruq", "sunrise" -> R.drawable.ic_syuruq
        "dzuhur", "dhuhr", "zuhur" -> R.drawable.ic_dzuhur
        "ashar", "asr" -> R.drawable.ic_ashar
        "maghrib" -> R.drawable.ic_maghrib
        "isya", "isha" -> R.drawable.ic_isya
        else -> R.drawable.ic_subuh
    }
}

private fun formatTimeAmPm(date: Date): String {
    val sdf = jakartaDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(date)
}

private fun formatDayDate(date: Date): String {
    val sdf = jakartaDateFormat("EEEE, MMMM d", Locale.getDefault())
    return sdf.format(date)
}

private fun calculateTimeUntilPrayer(prayer: Prayer, currentTime: Date): String {
    try {
        val timeParts = prayer.adhanTime.split(":")
        if (timeParts.size != 2) return "—"
        
        val prayerCal = jakartaCalendar(currentTime).apply {
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)
        }
        
        if (prayerCal.time.before(currentTime)) {
            prayerCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val diffMs = prayerCal.timeInMillis - currentTime.time
        val hours = diffMs / (1000 * 60 * 60)
        val minutes = (diffMs % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Now"
        }
    } catch (e: Exception) {
        return "—"
    }
}
