package com.masjiddisplay.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val JAKARTA_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")

fun jakartaCalendar(date: Date? = null): Calendar {
    val calendar = Calendar.getInstance(JAKARTA_TIME_ZONE)
    if (date != null) {
        calendar.time = date
    }
    return calendar
}

fun jakartaDateFormat(pattern: String, locale: Locale = Locale.getDefault()): SimpleDateFormat {
    return SimpleDateFormat(pattern, locale).apply {
        timeZone = JAKARTA_TIME_ZONE
    }
}

/**
 * Format Gregorian date for display in Indonesian
 */
fun formatGregorianDate(date: Date): String {
    val format = jakartaDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    return format.format(date)
}

/**
 * Format time with seconds
 */
fun formatTimeWithSeconds(date: Date): String {
    val format = jakartaDateFormat("HH:mm:ss", Locale.getDefault())
    return format.format(date)
}

/**
 * Format time without seconds
 */
fun formatTime(date: Date): String {
    val format = jakartaDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Hijri month names for display
 */
private val HIJRI_MONTHS = listOf(
    "Muharram",
    "Safar",
    "Rabi'ul Awwal",
    "Rabi'ul Akhir",
    "Jumadil Awwal",
    "Jumadil Akhir",
    "Rajab",
    "Sya'ban",
    "Ramadhan",
    "Syawwal",
    "Dzulqa'dah",
    "Dzulhijjah"
)

/**
 * Convert Gregorian date to Hijri date using the Kuwaiti algorithm
 * This is a widely-used algorithm that provides accurate conversion
 * Reference: https://www.al-habib.info/islamic-calendar/hijri-calendar-converter.htm
 */
fun gregorianToHijri(date: Date): Triple<Int, Int, Int> {
    val calendar = jakartaCalendar(date)
    
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
    val year = calendar.get(Calendar.YEAR)
    
    // Calculate Julian Day Number
    val jd = if (month <= 2) {
        val adjustedYear = year - 1
        val adjustedMonth = month + 12
        val a = (adjustedYear / 100)
        val b = 2 - a + (a / 4)
        (365.25 * (adjustedYear + 4716)).toLong() + (30.6001 * (adjustedMonth + 1)).toLong() + day + b - 1524
    } else {
        val a = (year / 100)
        val b = 2 - a + (a / 4)
        (365.25 * (year + 4716)).toLong() + (30.6001 * (month + 1)).toLong() + day + b - 1524
    }
    
    // Convert Julian Day to Hijri
    val l = jd - 1948440 + 10632
    val n = ((l - 1) / 10631)
    val remaining = l - 10631 * n + 354
    val j = ((10985 - remaining) / 5316) * ((50 * remaining) / 17719) + (remaining / 5670) * ((43 * remaining) / 15238)
    val adjustedRemaining = remaining - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
    val hijriMonth = ((24 * adjustedRemaining) / 709)
    val hijriDay = (adjustedRemaining - ((709 * hijriMonth) / 24)).toInt()
    val hijriYear = (30 * n + j - 30).toInt()
    
    return Triple(hijriYear, hijriMonth.toInt(), hijriDay)
}

/**
 * Get Hijri date string with accurate conversion
 */
fun getHijriDate(date: Date): String {
    val (hijriYear, hijriMonth, hijriDay) = gregorianToHijri(date)
    val monthName = HIJRI_MONTHS.getOrElse(hijriMonth - 1) { "Unknown" }
    return "$hijriDay $monthName $hijriYear H"
}

/**
 * Check if current date is in Ramadan using accurate Hijri conversion
 */
fun isRamadan(date: Date): Boolean {
    val (_, hijriMonth, _) = gregorianToHijri(date)
    return hijriMonth == 9 // Ramadhan is the 9th month
}

/**
 * Parse time string (HH:mm) to Calendar with given reference date
 */
fun parseTimeToCalendar(time: String, reference: Date): Calendar {
    val parts = time.split(":")
    val hour = parts.getOrNull(0)
        ?.trim()
        ?.toIntOrNull()
        ?.coerceIn(0, 23)
        ?: 0
    val minute = parts.getOrNull(1)
        ?.trim()
        ?.toIntOrNull()
        ?.coerceIn(0, 59)
        ?: 0
    
    val calendar = jakartaCalendar(reference)
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    return calendar
}

/**
 * Format milliseconds to clock format (mm:ss or hh:mm:ss)
 */
fun formatMsToClock(ms: Long): String {
    if (ms <= 0) return "00:00"
    
    val totalSeconds = (ms / 1000).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
