package com.masjiddisplay.utils

import com.masjiddisplay.data.Prayer
import com.masjiddisplay.data.PrayerStatus
import java.util.Calendar
import java.util.Date
import kotlin.math.*

/**
 * Prayer time calculation utilities using astronomical calculations
 * Based on Kemenag RI method (Fajr angle: 20°, Isha angle: 18°)
 */
object PrayerTimeCalculator {
    
    private const val FAJR_ANGLE = 20.0
    private const val ISHA_ANGLE = 18.0
    private const val DEFAULT_PRAYER_WINDOW_MINUTES = 20
    private const val MIN_PRAYER_WINDOW_MINUTES = 5
    private const val MAX_PRAYER_WINDOW_MINUTES = 60
    
    // Default coordinates for Masjid Jami' Al-Hidayah, Jakarta Timur
    private const val DEFAULT_LATITUDE = -6.3140892
    private const val DEFAULT_LONGITUDE = 106.8776666
    
    /**
     * Calculate prayer times for Jakarta using Kemenag RI method
     */
    fun calculatePrayerTimesForJakarta(date: Date): List<Prayer> {
        return calculatePrayerTimes(date, DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }
    
    /**
     * Calculate prayer times for given coordinates
     */
    fun calculatePrayerTimes(date: Date, latitude: Double, longitude: Double): List<Prayer> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        
        // Calculate solar parameters
        val d = 367 * year - ((7 * (year + ((1 + 9) / 12))) / 4) + ((275 * 1) / 9) + dayOfYear - 730531.5
        val g = (357.529 + 0.98560028 * d) % 360
        val q = (280.459 + 0.98564736 * d) % 360
        val l = (q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g))) % 360
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15
        val dec = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        
        // Equation of time
        val eqt = q / 15 - ra
        
        // Calculate Dhuhr time
        val dhuhr = 12 + (-longitude / 15) - eqt + 7 // +7 for WIB timezone
        
        // Calculate sunrise/sunset angle
        val sunriseAngle = Math.toDegrees(acos(
            (sin(Math.toRadians(-0.833)) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(dec))) /
            (cos(Math.toRadians(latitude)) * cos(Math.toRadians(dec)))
        )) / 15
        
        // Calculate Fajr angle
        val fajrTime = dhuhr - Math.toDegrees(acos(
            (sin(Math.toRadians(-FAJR_ANGLE)) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(dec))) /
            (cos(Math.toRadians(latitude)) * cos(Math.toRadians(dec)))
        )) / 15
        
        // Calculate Asr time (Shafi'i method - shadow = object height + shadow at noon)
        val asrFactor = 1.0 // Shafi'i method
        val asrAltitude = Math.toDegrees(atan(1 / (asrFactor + tan(Math.toRadians(abs(latitude - dec))))))
        val asrTime = dhuhr + Math.toDegrees(acos(
            (sin(Math.toRadians(asrAltitude)) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(dec))) /
            (cos(Math.toRadians(latitude)) * cos(Math.toRadians(dec)))
        )) / 15
        
        // Calculate Maghrib (sunset)
        val maghribTime = dhuhr + sunriseAngle
        
        // Calculate Isha time
        val ishaTime = dhuhr + Math.toDegrees(acos(
            (sin(Math.toRadians(-ISHA_ANGLE)) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(dec))) /
            (cos(Math.toRadians(latitude)) * cos(Math.toRadians(dec)))
        )) / 15
        
        // Convert decimal hours to HH:mm format
        fun formatPrayerTime(decimalHours: Double): String {
            val hours = decimalHours.toInt()
            val minutes = ((decimalHours - hours) * 60).toInt()
            return "%02d:%02d".format(hours, minutes)
        }
        
        // Create prayer list with iqamah times (15 minutes after adhan, except Maghrib which is 5 minutes)
        val prayers = mutableListOf<Prayer>()
        
        val fajrStr = formatPrayerTime(fajrTime)
        val dhuhrStr = formatPrayerTime(dhuhr)
        val asrStr = formatPrayerTime(asrTime)
        val maghribStr = formatPrayerTime(maghribTime)
        val ishaStr = formatPrayerTime(ishaTime)
        
        prayers.add(Prayer(
            name = "Subuh",
            adhanTime = fajrStr,
            iqamahTime = addMinutesToTime(fajrStr, 15),
            status = PrayerStatus.UPCOMING,
            windowMinutes = 15
        ))
        
        prayers.add(Prayer(
            name = "Dzuhur",
            adhanTime = dhuhrStr,
            iqamahTime = addMinutesToTime(dhuhrStr, 15),
            status = PrayerStatus.UPCOMING,
            windowMinutes = 15
        ))
        
        prayers.add(Prayer(
            name = "Ashar",
            adhanTime = asrStr,
            iqamahTime = addMinutesToTime(asrStr, 15),
            status = PrayerStatus.UPCOMING,
            windowMinutes = 15
        ))
        
        prayers.add(Prayer(
            name = "Maghrib",
            adhanTime = maghribStr,
            iqamahTime = addMinutesToTime(maghribStr, 5),
            status = PrayerStatus.UPCOMING,
            windowMinutes = 5
        ))
        
        prayers.add(Prayer(
            name = "Isya",
            adhanTime = ishaStr,
            iqamahTime = addMinutesToTime(ishaStr, 15),
            status = PrayerStatus.UPCOMING,
            windowMinutes = 15
        ))
        
        return updatePrayerStatuses(prayers, Date())
    }
    
    /**
     * Calculate Shuruq (sunrise) time
     */
    fun calculateShuruqTimeForJakarta(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        
        val d = 367 * year - ((7 * (year + ((1 + 9) / 12))) / 4) + ((275 * 1) / 9) + dayOfYear - 730531.5
        val g = (357.529 + 0.98560028 * d) % 360
        val q = (280.459 + 0.98564736 * d) % 360
        val l = (q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g))) % 360
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15
        val dec = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        
        val eqt = q / 15 - ra
        val dhuhr = 12 + (-DEFAULT_LONGITUDE / 15) - eqt + 7
        
        val sunriseAngle = Math.toDegrees(acos(
            (sin(Math.toRadians(-0.833)) - sin(Math.toRadians(DEFAULT_LATITUDE)) * sin(Math.toRadians(dec))) /
            (cos(Math.toRadians(DEFAULT_LATITUDE)) * cos(Math.toRadians(dec)))
        )) / 15
        
        val sunriseTime = dhuhr - sunriseAngle
        
        val hours = sunriseTime.toInt()
        val minutes = ((sunriseTime - hours) * 60).toInt()
        return "%02d:%02d".format(hours, minutes)
    }
    
    /**
     * Add minutes to a time string
     */
    private fun addMinutesToTime(time: String, minutes: Int): String {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        
        var newMinute = minute + minutes
        var newHour = hour
        
        if (newMinute >= 60) {
            newHour += newMinute / 60
            newMinute %= 60
        }
        if (newHour >= 24) {
            newHour %= 24
        }
        
        return "%02d:%02d".format(newHour, newMinute)
    }
    
    /**
     * Update prayer statuses based on current time
     */
    fun updatePrayerStatuses(prayers: List<Prayer>, currentTime: Date): List<Prayer> {
        var foundCurrent = false
        
        return prayers.map { prayer ->
            val adhanCalendar = parseTimeToCalendar(prayer.adhanTime, currentTime)
            val windowMinutes = prayer.windowMinutes ?: DEFAULT_PRAYER_WINDOW_MINUTES
            val endCalendar = (adhanCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, windowMinutes)
            }
            
            val current = Calendar.getInstance()
            current.time = currentTime
            
            val (status, countdown) = when {
                !foundCurrent && current.timeInMillis >= adhanCalendar.timeInMillis && 
                    current.timeInMillis < endCalendar.timeInMillis -> {
                    foundCurrent = true
                    val remainingMs = endCalendar.timeInMillis - current.timeInMillis
                    val remainingMinutes = (remainingMs / 60000).toInt().coerceAtLeast(0)
                    PrayerStatus.CURRENT to formatCountdown(remainingMinutes)
                }
                current.timeInMillis < adhanCalendar.timeInMillis -> {
                    val remainingMs = adhanCalendar.timeInMillis - current.timeInMillis
                    val remainingMinutes = (remainingMs / 60000).toInt().coerceAtLeast(0)
                    PrayerStatus.UPCOMING to formatCountdown(remainingMinutes)
                }
                else -> PrayerStatus.PASSED to null
            }
            
            prayer.copy(status = status, countdown = countdown)
        }
    }
    
    /**
     * Format countdown string
     */
    fun formatCountdown(minutes: Int): String {
        if (minutes < 0) return "--:--"
        
        val hours = minutes / 60
        val mins = minutes % 60
        
        return if (hours > 0) {
            "${hours}j ${mins}m"
        } else {
            "${mins}m"
        }
    }
    
    /**
     * Get next upcoming prayer
     */
    fun getNextPrayer(prayers: List<Prayer>, tomorrowPrayers: List<Prayer>? = null): Prayer? {
        val upcoming = prayers.find { it.status == PrayerStatus.UPCOMING }
        
        if (upcoming == null && !tomorrowPrayers.isNullOrEmpty()) {
            return tomorrowPrayers.firstOrNull()
        }
        
        return upcoming
    }
    
    /**
     * Get current prayer in progress
     */
    fun getCurrentPrayer(prayers: List<Prayer>): Prayer? {
        return prayers.find { it.status == PrayerStatus.CURRENT }
    }
    
    /**
     * Check if all prayers for the day have passed
     */
    fun allPrayersPassed(prayers: List<Prayer>): Boolean {
        return prayers.all { it.status == PrayerStatus.PASSED }
    }
    
    /**
     * Check if currently within prayer window
     */
    fun isWithinPrayerWindow(prayer: Prayer, currentTime: Date): Boolean {
        val adhanCalendar = parseTimeToCalendar(prayer.adhanTime, currentTime)
        val windowMinutes = prayer.windowMinutes ?: DEFAULT_PRAYER_WINDOW_MINUTES
        val endCalendar = (adhanCalendar.clone() as Calendar).apply {
            add(Calendar.MINUTE, windowMinutes)
        }
        
        val current = Calendar.getInstance()
        current.time = currentTime
        
        return current.timeInMillis >= adhanCalendar.timeInMillis && 
               current.timeInMillis < endCalendar.timeInMillis
    }
    
    /**
     * Get prayer phase (adzan or iqamah)
     */
    fun getPrayerPhase(prayer: Prayer, currentTime: Date): String {
        val iqamahCalendar = parseTimeToCalendar(prayer.iqamahTime, currentTime)
        val current = Calendar.getInstance()
        current.time = currentTime
        
        return if (current.timeInMillis >= iqamahCalendar.timeInMillis) "iqamah" else "adzan"
    }
    
    /**
     * Get prayer window bounds
     */
    fun getPrayerWindowBounds(prayer: Prayer, referenceDate: Date): PrayerWindowBounds {
        val start = parseTimeToCalendar(prayer.adhanTime, referenceDate)
        val iqamahDate = parseTimeToCalendar(prayer.iqamahTime, referenceDate)
        val windowMinutes = prayer.windowMinutes ?: DEFAULT_PRAYER_WINDOW_MINUTES
        val end = (start.clone() as Calendar).apply {
            add(Calendar.MINUTE, windowMinutes)
        }
        
        return PrayerWindowBounds(
            start = start.time,
            end = end.time,
            iqamahDate = iqamahDate.time,
            durationMinutes = windowMinutes
        )
    }
    
    data class PrayerWindowBounds(
        val start: Date,
        val end: Date,
        val iqamahDate: Date,
        val durationMinutes: Int
    )
}
