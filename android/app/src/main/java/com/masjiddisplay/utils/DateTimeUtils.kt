package com.masjiddisplay.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Format Gregorian date for display in Indonesian
 */
fun formatGregorianDate(date: Date): String {
    val format = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    return format.format(date)
}

/**
 * Format time with seconds
 */
fun formatTimeWithSeconds(date: Date): String {
    val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return format.format(date)
}

/**
 * Format time without seconds
 */
fun formatTime(date: Date): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Get Hijri date string (simplified approximation)
 * For production, use a proper Hijri calendar library
 */
fun getHijriDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    
    val gregorianYear = calendar.get(Calendar.YEAR)
    val gregorianMonth = calendar.get(Calendar.MONTH)
    val gregorianDay = calendar.get(Calendar.DAY_OF_MONTH)
    
    // Approximate Hijri year (not accurate, use proper library for production)
    val hijriYear = ((gregorianYear - 622) * 1.030684).toInt()
    
    val hijriMonths = listOf(
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
    
    // Approximate month (for display purposes) with bounds checking
    val hijriMonth = hijriMonths.getOrElse(gregorianMonth.coerceIn(0, 11)) { "Unknown" }
    val hijriDay = gregorianDay // Simplified
    
    return "$hijriDay $hijriMonth $hijriYear H"
}

/**
 * Check if current date is in Ramadan
 * This is a simplified check, use proper Islamic calendar library for accuracy
 */
fun isRamadan(date: Date): Boolean {
    val hijriDate = getHijriDate(date)
    return hijriDate.contains("Ramadhan")
}

/**
 * Parse time string (HH:mm) to Calendar with given reference date
 */
fun parseTimeToCalendar(time: String, reference: Date): Calendar {
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    
    val calendar = Calendar.getInstance()
    calendar.time = reference
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
