package com.masjiddisplay.data

data class MasjidConfig(
    val name: String,
    val location: String,
    val tagline: String = "",
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val calculationMethod: String = "Kemenag RI"
)

data class Prayer(
    val name: String,
    val adhanTime: String,
    val iqamahTime: String,
    val status: PrayerStatus = PrayerStatus.UPCOMING
)

enum class PrayerStatus {
    PASSED, CURRENT, UPCOMING
}

data class KasData(
    val balance: Long = 0,
    val incomeMonth: Long = 0,
    val expenseMonth: Long = 0
)
