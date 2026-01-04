package com.masjiddisplay.utils

import com.masjiddisplay.data.Prayer
import com.masjiddisplay.data.PrayerStatus
import java.util.*
import kotlin.math.*

object PrayerTimeCalculator {
    
    private const val FAJR_ANGLE = 20.0
    private const val ISHA_ANGLE = 18.0
    private const val JAKARTA_LAT = -6.2088
    private const val JAKARTA_LNG = 106.8456
    private const val JAKARTA_TIMEZONE = 7.0

    fun calculatePrayerTimesForJakarta(date: Date): List<Prayer> {
        val cal = Calendar.getInstance().apply { time = date }
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        
        val times = calculateTimes(dayOfYear, year, JAKARTA_LAT, JAKARTA_LNG, JAKARTA_TIMEZONE)
        
        return listOf(
            Prayer("Subuh", times[0], addMinutes(times[0], 15)),
            Prayer("Dzuhur", times[1], addMinutes(times[1], 10)),
            Prayer("Ashar", times[2], addMinutes(times[2], 10)),
            Prayer("Maghrib", times[3], addMinutes(times[3], 5)),
            Prayer("Isya", times[4], addMinutes(times[4], 10))
        )
    }

    fun calculateShuruqTimeForJakarta(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        
        val jd = julianDate(year, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        val sunDec = sunDeclination(jd)
        val eqTime = equationOfTime(jd)
        
        val sunrise = computeSunrise(JAKARTA_LAT, sunDec, eqTime, JAKARTA_LNG, JAKARTA_TIMEZONE)
        return formatTime(sunrise)
    }

    fun updatePrayerStatuses(prayers: List<Prayer>, currentTime: Date, referenceDate: Date? = null): List<Prayer> {
        val cal = Calendar.getInstance().apply { time = currentTime }
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        
        return prayers.mapIndexed { index, prayer ->
            val prayerMinutes = timeToMinutes(prayer.adhanTime)
            val nextPrayerMinutes = if (index < prayers.size - 1) {
                timeToMinutes(prayers[index + 1].adhanTime)
            } else {
                24 * 60
            }
            
            val status = when {
                currentMinutes < prayerMinutes -> PrayerStatus.UPCOMING
                currentMinutes < nextPrayerMinutes -> PrayerStatus.CURRENT
                else -> PrayerStatus.PASSED
            }
            prayer.copy(status = status)
        }
    }

    fun getNextPrayer(prayers: List<Prayer>, tomorrowPrayers: List<Prayer>? = null): Prayer? {
        val upcoming = prayers.find { it.status == PrayerStatus.UPCOMING }
        if (upcoming != null) return upcoming
        
        val current = prayers.find { it.status == PrayerStatus.CURRENT }
        if (current != null) {
            val currentIndex = prayers.indexOf(current)
            if (currentIndex < prayers.size - 1) {
                return prayers[currentIndex + 1]
            }
        }
        
        return tomorrowPrayers?.firstOrNull()
    }

    fun getCurrentPrayer(prayers: List<Prayer>): Prayer? {
        return prayers.find { it.status == PrayerStatus.CURRENT }
    }

    fun allPrayersPassed(prayers: List<Prayer>): Boolean {
        return prayers.all { it.status == PrayerStatus.PASSED }
    }

    private fun calculateTimes(dayOfYear: Int, year: Int, lat: Double, lng: Double, tz: Double): List<String> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.DAY_OF_YEAR, dayOfYear)
        }
        val jd = julianDate(year, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        val sunDec = sunDeclination(jd)
        val eqTime = equationOfTime(jd)

        val fajr = computePrayerTime(lat, sunDec, eqTime, lng, tz, -FAJR_ANGLE)
        val sunrise = computeSunrise(lat, sunDec, eqTime, lng, tz)
        val dhuhr = computeDhuhr(eqTime, lng, tz)
        val asr = computeAsr(lat, sunDec, eqTime, lng, tz)
        val sunset = computeSunset(lat, sunDec, eqTime, lng, tz)
        val isha = computePrayerTime(lat, sunDec, eqTime, lng, tz, -ISHA_ANGLE, afternoon = true)

        return listOf(
            formatTime(fajr),
            formatTime(dhuhr),
            formatTime(asr),
            formatTime(sunset),
            formatTime(isha)
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunDeclination(jd: Double): Double {
        val d = jd - 2451545.0
        val g = Math.toRadians((357.529 + 0.98560028 * d) % 360)
        val q = (280.459 + 0.98564736 * d) % 360
        val l = Math.toRadians((q + 1.915 * sin(g) + 0.020 * sin(2 * g)) % 360)
        val e = Math.toRadians(23.439 - 0.00000036 * d)
        return Math.toDegrees(asin(sin(e) * sin(l)))
    }

    private fun equationOfTime(jd: Double): Double {
        val d = jd - 2451545.0
        val g = Math.toRadians((357.529 + 0.98560028 * d) % 360)
        val q = (280.459 + 0.98564736 * d) % 360
        val l = Math.toRadians((q + 1.915 * sin(g) + 0.020 * sin(2 * g)) % 360)
        val e = Math.toRadians(23.439 - 0.00000036 * d)
        var ra = Math.toDegrees(atan2(cos(e) * sin(l), cos(l))) / 15.0
        if (ra < 0) ra += 24
        return (q / 15.0) - ra
    }

    private fun computeDhuhr(eqTime: Double, lng: Double, tz: Double): Double {
        return 12 + tz - lng / 15 - eqTime
    }

    private fun computeSunrise(lat: Double, dec: Double, eqTime: Double, lng: Double, tz: Double): Double {
        val angle = -0.833
        return computePrayerTime(lat, dec, eqTime, lng, tz, angle)
    }

    private fun computeSunset(lat: Double, dec: Double, eqTime: Double, lng: Double, tz: Double): Double {
        val angle = -0.833
        return computePrayerTime(lat, dec, eqTime, lng, tz, angle, afternoon = true)
    }

    private fun computeAsr(lat: Double, dec: Double, eqTime: Double, lng: Double, tz: Double): Double {
        val dhuhr = computeDhuhr(eqTime, lng, tz)
        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(dec)
        
        val factor = 1.0
        val tanAlt = 1.0 / (factor + tan(abs(latRad - decRad)))
        val altRad = atan(tanAlt)
        
        val cosH = (sin(altRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        if (cosH > 1 || cosH < -1) return dhuhr
        
        val h = Math.toDegrees(acos(cosH)) / 15.0
        return dhuhr + h
    }

    private fun computePrayerTime(lat: Double, dec: Double, eqTime: Double, lng: Double, tz: Double, angle: Double, afternoon: Boolean = false): Double {
        val dhuhr = computeDhuhr(eqTime, lng, tz)
        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(dec)
        val angleRad = Math.toRadians(angle)
        
        val cosH = (sin(angleRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        if (cosH > 1 || cosH < -1) return dhuhr
        
        val h = Math.toDegrees(acos(cosH)) / 15.0
        return if (afternoon) dhuhr + h else dhuhr - h
    }

    private fun formatTime(time: Double): String {
        var t = time
        if (t < 0) t += 24
        if (t >= 24) t -= 24
        val hours = t.toInt()
        val minutes = ((t - hours) * 60).toInt()
        return "%02d:%02d".format(hours, minutes)
    }

    private fun addMinutes(time: String, minutes: Int): String {
        val parts = time.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt() + minutes
        val newH = h + m / 60
        val newM = m % 60
        return "%02d:%02d".format(newH, newM)
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}
