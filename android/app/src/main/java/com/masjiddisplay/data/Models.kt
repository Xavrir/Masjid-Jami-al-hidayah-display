package com.masjiddisplay.data

/**
 * Prayer status indicating the current state relative to current time
 */
enum class PrayerStatus {
    PASSED,
    CURRENT,
    UPCOMING
}

/**
 * Represents a prayer time with its schedule and current status
 */
data class Prayer(
    val name: String,
    val adhanTime: String,
    val iqamahTime: String,
    val status: PrayerStatus,
    val countdown: String? = null,
    val windowMinutes: Int? = null
)

/**
 * Transaction type for kas (treasury) records
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Represents a single kas transaction
 */
data class KasTransaction(
    val id: String,
    val date: String,
    val description: String,
    val amount: Long,
    val type: TransactionType
)

/**
 * Trend direction for kas balance
 */
enum class TrendDirection {
    UP,
    DOWN,
    FLAT
}

/**
 * Represents the mosque's treasury data
 */
data class KasData(
    val balance: Long,
    val incomeMonth: Long,
    val expenseMonth: Long,
    val trendDirection: TrendDirection,
    val recentTransactions: List<KasTransaction>,
    val trendData: List<Long>
)

/**
 * Mosque configuration including location and calculation parameters
 */
data class MasjidConfig(
    val name: String,
    val location: String,
    val tagline: String? = null,
    val latitude: Double,
    val longitude: Double,
    val calculationMethod: String
)
