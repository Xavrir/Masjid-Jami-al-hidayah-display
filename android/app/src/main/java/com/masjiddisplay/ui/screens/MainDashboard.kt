package com.masjiddisplay.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masjiddisplay.R
import com.masjiddisplay.data.*
import com.masjiddisplay.ui.components.AnnouncementTicker
import com.masjiddisplay.ui.state.PrayerNotificationState
import com.masjiddisplay.utils.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainDashboard(
    masjidConfig: MasjidConfig,
    kasData: KasData,
    announcements: List<String>,
    prayerNotificationState: PrayerNotificationState? = null,
    onPrayerStart: (Prayer) -> Unit = {},
    onKasDetailRequested: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }
    var prayers by remember { mutableStateOf<List<Prayer>>(emptyList()) }
    var tomorrowPrayers by remember { mutableStateOf<List<Prayer>>(emptyList()) }
    var nextPrayer by remember { mutableStateOf<Prayer?>(null) }
    var isNextPrayerTomorrow by remember { mutableStateOf(false) }
    val latestPrayers by rememberUpdatedState(prayers)
    val latestTime by rememberUpdatedState(currentTime)

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
                val tomorrowRef = Calendar.getInstance().apply {
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
            isNextPrayerTomorrow = allPassed && nextPrayer != null

            val current = PrayerTimeCalculator.getCurrentPrayer(prayers)
            if (current != null) {
                onPrayerStart(current)
            }
        }
    }

    LaunchedEffect(prayerNotificationState) {
        val state = prayerNotificationState ?: return@LaunchedEffect
        while (true) {
            state.checkForAlerts(latestPrayers, latestTime)
            delay(1000)
        }
    }

    val isShowingTomorrowSchedule = PrayerTimeCalculator.allPrayersPassed(prayers) && tomorrowPrayers.isNotEmpty()

    val shuruqTime = PrayerTimeCalculator.calculateShuruqTimeForJakarta(
        if (isShowingTomorrowSchedule) {
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        } else {
            currentTime
        }
    )

    val nextPrayerTargetTime = remember(nextPrayer, isNextPrayerTomorrow) {
        nextPrayer?.let { p ->
            val cal = Calendar.getInstance()
            if (isNextPrayerTomorrow) cal.add(Calendar.DAY_OF_YEAR, 1)
            val parts = p.adhanTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.time
        }
    }

    val countdownString = remember(currentTime, nextPrayerTargetTime) {
        if (nextPrayerTargetTime != null) {
            val diff = nextPrayerTargetTime.time - currentTime.time
            if (diff > 0) {
                val totalSeconds = (diff / 1000).toInt()
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                "%02d:%02d:%02d".format(hours, minutes, seconds)
            } else "00:00:00"
        } else "--:--:--"
    }

    val dateFormat12h = remember { SimpleDateFormat("h:mm a", Locale.US) }
    val dateFormatFull = remember { SimpleDateFormat("EEEE, MMMM d", Locale.US) }
    val clock12h = dateFormat12h.format(currentTime).uppercase()
    val dateString = dateFormatFull.format(currentTime).uppercase()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.mosque_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A5EB8).copy(alpha = 0.85f),
                            Color(0xFF063A7A).copy(alpha = 0.90f)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = masjidConfig.name,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateString,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = clock12h,
                        style = TextStyle(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "☀ SUNRISE ${formatTo12Hour(shuruqTime)}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "UNTIL ${nextPrayer?.name?.uppercase() ?: "—"}",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 2.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = countdownString,
                        style = TextStyle(
                            fontSize = 140.sp,
                            fontWeight = FontWeight.Thin,
                            color = Color(0xFF00E5FF),
                            letterSpacing = (-4).sp
                        )
                    )
                }
            }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    prayers.forEach { prayer ->
                        val isNext = nextPrayer?.name == prayer.name
                        val prayerIcon = getPrayerIcon(prayer.name)

                        PrayerTimeItem(
                            icon = prayerIcon,
                            name = prayer.name.uppercase(),
                            time = formatTo12Hour(prayer.adhanTime),
                            isHighlighted = isNext,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = "☀ SUNRISE  ${formatTo12Hour(shuruqTime)}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnnouncementTicker(
                    announcements = announcements,
                    speed = "slow"
                )
            }
        }
    }
}

@Composable
private fun PrayerTimeItem(
    icon: String,
    name: String,
    time: String,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isHighlighted) Color(0xFF00E5FF).copy(alpha = 0.6f) else Color.Transparent
    val bgColor = if (isHighlighted) Color.White.copy(alpha = 0.1f) else Color.Transparent

    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isHighlighted) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = TextStyle(fontSize = 24.sp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = time,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )
        )

        if (isHighlighted) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF00E5FF))
            )
        }
    }
}

private fun getPrayerIcon(name: String): String {
    return when (name.lowercase()) {
        "subuh", "fajr" -> "🌙"
        "dzuhur", "dhuhr" -> "☀️"
        "ashar", "asr" -> "🌤️"
        "maghrib" -> "🌅"
        "isya", "isha" -> "⭐"
        else -> "🕌"
    }
}

private fun formatTo12Hour(time24: String): String {
    return try {
        val parts = time24.split(":")
        val hour = parts[0].toInt()
        val minute = parts.getOrElse(1) { "00" }
        val amPm = if (hour >= 12) "PM" else "AM"
        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        "$hour12:$minute $amPm"
    } catch (e: Exception) {
        time24
    }
}
