package com.masjiddisplay.utils

import com.masjiddisplay.data.Prayer
import com.masjiddisplay.data.PrayerStatus
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

/**
 * Prayer time calculation utilities using NOAA solar position algorithm.
 * Based on Kemenag RI method (Fajr angle: 20°, Isha angle: 18°).
 * Asr: Shafi'i method (shadow = object height + noon shadow).
 *
 * Kemenag RI applies a 2-minute "ihtiyath" (precautionary margin) to all
 * prayer times except sunrise. This matches official Kemenag schedules.
 */
object PrayerTimeCalculator {
    
    private const val FAJR_ANGLE = 20.0            // Kemenag RI
    private const val ISHA_ANGLE = 18.0             // Kemenag RI
    private const val ASR_FACTOR_SHAFII = 1.0       // Shafi'i method
    private const val SUNRISE_ALTITUDE = -0.833     // Standard refraction + solar radius
    private const val IHTIYAT_MINUTES = 3.0         // Precautionary margin (calibrated to match Kemenag RI schedules)
    private const val WIB_OFFSET_HOURS = 7.0        // WIB timezone offset (UTC+7)
    private const val DEFAULT_PRAYER_WINDOW_MINUTES = 20
    
    // Coordinates for Masjid Jami' Al-Hidayah, Kampung Rambutan, Jakarta Timur
    private const val DEFAULT_LATITUDE = -6.3092124
    private const val DEFAULT_LONGITUDE = 106.8816386
    
    private val WIB_TIMEZONE: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")

    // ========================= NOAA Solar Math (stable, no RA wrapping) =========================

    /**
     * Fractional year in radians (NOAA method).
     * gamma = 2*pi/365 * (dayOfYear - 1 + (hour - 12) / 24)
     */
    private fun fractionalYearRad(dayOfYear: Int, hourLocal: Double = 12.0): Double {
        return 2.0 * Math.PI / 365.0 * (dayOfYear - 1 + (hourLocal - 12.0) / 24.0)
    }

    /**
     * NOAA equation of time in minutes.
     */
    private fun equationOfTimeMinutes(gamma: Double): Double {
        return 229.18 * (
            0.000075
                + 0.001868 * cos(gamma)
                - 0.032077 * sin(gamma)
                - 0.014615 * cos(2.0 * gamma)
                - 0.040849 * sin(2.0 * gamma)
            )
    }

    /**
     * NOAA solar declination in radians.
     */
    private fun solarDeclinationRad(gamma: Double): Double {
        return 0.006918 -
            0.399912 * cos(gamma) +
            0.070257 * sin(gamma) -
            0.006758 * cos(2.0 * gamma) +
            0.000907 * sin(2.0 * gamma) -
            0.002697 * cos(3.0 * gamma) +
            0.00148 * sin(3.0 * gamma)
    }

    /**
     * Hour angle for a given solar altitude.
     * cos(H) = (sin(altitude) - sin(lat) * sin(dec)) / (cos(lat) * cos(dec))
     * Returns radians in [0, pi].
     */
    private fun hourAngleRad(latRad: Double, declRad: Double, altitudeRad: Double): Double {
        val cosH = (sin(altitudeRad) - sin(latRad) * sin(declRad)) /
            (cos(latRad) * cos(declRad))
        return acos(cosH.coerceIn(-1.0, 1.0))
    }

    /**
     * Asr altitude angle using Shafi'i method.
     * altitude = atan(1 / (factor + tan(|latitude - declination|)))
     */
    private fun asrAltitudeRad(latRad: Double, declRad: Double): Double {
        return atan(1.0 / (ASR_FACTOR_SHAFII + tan(abs(latRad - declRad))))
    }

    private fun degToRad(deg: Double): Double = deg * Math.PI / 180.0
    private fun radToDeg(rad: Double): Double = rad * 180.0 / Math.PI

    // ========================= Public API =========================

    /**
     * Calculate prayer times for Jakarta using Kemenag RI method.
     */
    fun calculatePrayerTimesForJakarta(date: Date): List<Prayer> {
        return calculatePrayerTimes(date, DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }
    
    /**
     * Calculate prayer times for given coordinates using NOAA solar algorithm.
     */
    fun calculatePrayerTimes(date: Date, latitude: Double, longitude: Double): List<Prayer> {
        val calendar = Calendar.getInstance(WIB_TIMEZONE, Locale.ROOT).apply { time = date }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val latRad = degToRad(latitude)
        
        // NOAA fractional year at local noon
        val gamma = fractionalYearRad(dayOfYear, 12.0)
        val eqTimeMin = equationOfTimeMinutes(gamma)
        val declRad = solarDeclinationRad(gamma)
        
        // Solar noon in minutes from midnight (local standard time)
        // NOAA: solarNoon = 720 - 4*longitude - eqTime + timezone*60
        val solarNoonMin = 720.0 - 4.0 * longitude - eqTimeMin + WIB_OFFSET_HOURS * 60.0
        
        // Helper: minutes from midnight for a given solar altitude
        fun minutesForAltitude(altitudeDeg: Double, isMorning: Boolean): Double {
            val hRad = hourAngleRad(latRad, declRad, degToRad(altitudeDeg))
            val deltaMin = 4.0 * radToDeg(hRad)
            return if (isMorning) solarNoonMin - deltaMin else solarNoonMin + deltaMin
        }
        
        // Calculate raw times (minutes from midnight)
        val fajrMin = minutesForAltitude(-FAJR_ANGLE, isMorning = true) + IHTIYAT_MINUTES
        val dhuhrMin = solarNoonMin + IHTIYAT_MINUTES
        
        val asrAltRad = asrAltitudeRad(latRad, declRad)
        val asrHRad = hourAngleRad(latRad, declRad, asrAltRad)
        val asrMin = solarNoonMin + 4.0 * radToDeg(asrHRad) + IHTIYAT_MINUTES
        
        val maghribMin = minutesForAltitude(SUNRISE_ALTITUDE, isMorning = false) + IHTIYAT_MINUTES
        val ishaMin = minutesForAltitude(-ISHA_ANGLE, isMorning = false) + IHTIYAT_MINUTES

        // Convert minutes-from-midnight to "HH:mm" string
        fun formatMinutes(totalMinutes: Double): String {
            var mins = totalMinutes.roundToInt()
            if (mins < 0) mins += 1440
            if (mins >= 1440) mins -= 1440
            val h = mins / 60
            val m = mins % 60
            return "%02d:%02d".format(h, m)
        }
        
        val fajrStr = formatMinutes(fajrMin)
        val dhuhrStr = formatMinutes(dhuhrMin)
        val asrStr = formatMinutes(asrMin)
        val maghribStr = formatMinutes(maghribMin)
        val ishaStr = formatMinutes(ishaMin)
        
        // Build prayer list
        val prayers = mutableListOf<Prayer>()
        
        // Imsak: 10 minutes before Subuh (Kemenag RI standard)
        val imsakStr = subtractMinutesFromTime(fajrStr, 10)
        prayers.add(Prayer(
            name = "Imsak",
            adhanTime = imsakStr,
            iqamahTime = imsakStr,
            status = PrayerStatus.UPCOMING,
            windowMinutes = 1
        ))
        
        prayers.add(Prayer(
            name = "Subuh",
            adhanTime = fajrStr,
            iqamahTime = addMinutesToTime(fajrStr, 10),
            status = PrayerStatus.UPCOMING,
            windowMinutes = 10
        ))
        
        prayers.add(Prayer(
            name = "Dzuhur",
            adhanTime = dhuhrStr,
            iqamahTime = addMinutesToTime(dhuhrStr, 10),
            status = PrayerStatus.UPCOMING,
            windowMinutes = 10
        ))
        
        prayers.add(Prayer(
            name = "Ashar",
            adhanTime = asrStr,
            iqamahTime = addMinutesToTime(asrStr, 10),
            status = PrayerStatus.UPCOMING,
            windowMinutes = 10
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
            iqamahTime = addMinutesToTime(ishaStr, 10),
            status = PrayerStatus.UPCOMING,
            windowMinutes = 10
        ))
        
        return updatePrayerStatuses(prayers, Date(), date)
    }
    
    /**
     * Calculate Imsak time (10 minutes before Subuh per Kemenag RI).
     */
    fun calculateImsakTimeForJakarta(date: Date): String {
        val prayers = calculatePrayerTimesForJakarta(date)
        val subuh = prayers.find { it.name == "Subuh" }
        return if (subuh != null) {
            subtractMinutesFromTime(subuh.adhanTime, 10)
        } else {
            "--:--"
        }
    }

    /**
     * Calculate Shuruq (sunrise) time using NOAA algorithm.
     */
    fun calculateShuruqTimeForJakarta(date: Date): String {
        val calendar = Calendar.getInstance(WIB_TIMEZONE, Locale.ROOT).apply { time = date }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val latRad = degToRad(DEFAULT_LATITUDE)
        
        val gamma = fractionalYearRad(dayOfYear, 12.0)
        val eqTimeMin = equationOfTimeMinutes(gamma)
        val declRad = solarDeclinationRad(gamma)
        
        val solarNoonMin = 720.0 - 4.0 * DEFAULT_LONGITUDE - eqTimeMin + WIB_OFFSET_HOURS * 60.0
        
        val sunriseHRad = hourAngleRad(latRad, declRad, degToRad(SUNRISE_ALTITUDE))
        val sunriseMin = solarNoonMin - 4.0 * radToDeg(sunriseHRad)
        
        var mins = sunriseMin.roundToInt()
        if (mins < 0) mins += 1440
        if (mins >= 1440) mins -= 1440
        val h = mins / 60
        val m = mins % 60
        return "%02d:%02d".format(h, m)
    }

    // ========================= Time arithmetic helpers =========================

    private fun subtractMinutesFromTime(time: String, minutes: Int): String {
        val parts = time.split(":")
        if (parts.size != 2) return time
        val hour = parts[0].toIntOrNull() ?: return time
        val minute = parts[1].toIntOrNull() ?: return time

        var newMinute = minute - minutes
        var newHour = hour

        while (newMinute < 0) {
            newMinute += 60
            newHour -= 1
        }
        if (newHour < 0) {
            newHour += 24
        }

        return "%02d:%02d".format(newHour, newMinute)
    }

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

    // ========================= Status & lifecycle =========================

    /**
     * Update prayer statuses based on current time.
     * Current prayer is highlighted from its adhan time until the next prayer's adhan time.
     */
    fun updatePrayerStatuses(prayers: List<Prayer>, currentTime: Date, prayerDate: Date = currentTime): List<Prayer> {
        val current = jakartaCalendar(currentTime)
        
        var currentPrayerIndex = -1
        
        for (i in prayers.indices) {
            val prayer = prayers[i]
            val adhanCalendar = parseTimeToCalendar(prayer.adhanTime, prayerDate)
            
            if (current.timeInMillis >= adhanCalendar.timeInMillis) {
                val nextPrayer = prayers.getOrNull(i + 1)
                if (nextPrayer != null) {
                    val nextAdhanCalendar = parseTimeToCalendar(nextPrayer.adhanTime, prayerDate)
                    if (current.timeInMillis < nextAdhanCalendar.timeInMillis) {
                        currentPrayerIndex = i
                        break
                    }
                } else {
                    currentPrayerIndex = i
                    break
                }
            }
        }
        
        return prayers.mapIndexed { index, prayer ->
            val adhanCalendar = parseTimeToCalendar(prayer.adhanTime, prayerDate)
            
            val status = when {
                index == currentPrayerIndex -> PrayerStatus.CURRENT
                current.timeInMillis < adhanCalendar.timeInMillis -> PrayerStatus.UPCOMING
                else -> PrayerStatus.PASSED
            }
            
            val countdown = if (status == PrayerStatus.CURRENT) {
                val nextPrayer = prayers.getOrNull(index + 1)
                if (nextPrayer != null) {
                    val nextAdhanCalendar = parseTimeToCalendar(nextPrayer.adhanTime, prayerDate)
                    val remainingMs = nextAdhanCalendar.timeInMillis - current.timeInMillis
                    val remainingMinutes = (remainingMs / 60000).toInt().coerceAtLeast(0)
                    formatCountdown(remainingMinutes)
                } else null
            } else null
            
            prayer.copy(status = status, countdown = countdown)
        }
    }
    
    fun formatCountdown(minutes: Int): String {
        if (minutes < 0) return "--:--"
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours}j ${mins}m" else "${mins}m"
    }
    
    fun getNextPrayer(prayers: List<Prayer>, tomorrowPrayers: List<Prayer>? = null): Prayer? {
        val upcoming = prayers.find { it.status == PrayerStatus.UPCOMING }
        if (upcoming == null && !tomorrowPrayers.isNullOrEmpty()) {
            return tomorrowPrayers.firstOrNull()
        }
        return upcoming
    }
    
    fun getCurrentPrayer(prayers: List<Prayer>): Prayer? {
        return prayers.find { it.status == PrayerStatus.CURRENT }
    }
    
    fun allPrayersPassed(prayers: List<Prayer>): Boolean {
        return prayers.all { it.status == PrayerStatus.PASSED }
    }
    
    fun isWithinPrayerWindow(prayer: Prayer, currentTime: Date): Boolean {
        val adhanCalendar = parseTimeToCalendar(prayer.adhanTime, currentTime)
        val windowMinutes = prayer.windowMinutes ?: DEFAULT_PRAYER_WINDOW_MINUTES
        val endCalendar = (adhanCalendar.clone() as Calendar).apply {
            add(Calendar.MINUTE, windowMinutes)
        }
        val current = jakartaCalendar(currentTime)
        return current.timeInMillis >= adhanCalendar.timeInMillis && 
               current.timeInMillis < endCalendar.timeInMillis
    }
    
    fun getPrayerPhase(prayer: Prayer, currentTime: Date): String {
        val iqamahCalendar = parseTimeToCalendar(prayer.iqamahTime, currentTime)
        val current = jakartaCalendar(currentTime)
        return if (current.timeInMillis >= iqamahCalendar.timeInMillis) "iqamah" else "adzan"
    }
    
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

    /**
     * Round Double to nearest Int (Kotlin stdlib equivalent).
     */
    private fun Double.roundToInt(): Int = Math.round(this).toInt()
}
