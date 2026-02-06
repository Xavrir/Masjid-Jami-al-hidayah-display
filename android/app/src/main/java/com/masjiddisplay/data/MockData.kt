package com.masjiddisplay.data

/**
 * Mock data for the application
 */
object MockData {
    val masjidConfig = MasjidConfig(
        name = "Masjid Jami' Al-Hidayah",
        location = "Jl. Tanah Merdeka II No.8, Rambutan, Ciracas, Jakarta Timur 13830",
        tagline = "Memakmurkan Masjid, Mencerahkan Umat",
        latitude = -6.3140892,
        longitude = 106.8776666,
        calculationMethod = "Kemenag RI"
    )

    private val kasTransactions = listOf(
        KasTransaction(
            id = "1",
            date = "2025-11-26",
            description = "Infaq Jumat",
            amount = 2500000,
            type = TransactionType.INCOME
        ),
        KasTransaction(
            id = "2",
            date = "2025-11-25",
            description = "Listrik Bulan November",
            amount = 850000,
            type = TransactionType.EXPENSE
        ),
        KasTransaction(
            id = "3",
            date = "2025-11-24",
            description = "Donasi Umum",
            amount = 1500000,
            type = TransactionType.INCOME
        ),
        KasTransaction(
            id = "4",
            date = "2025-11-23",
            description = "Kebersihan dan Pemeliharaan",
            amount = 500000,
            type = TransactionType.EXPENSE
        ),
        KasTransaction(
            id = "5",
            date = "2025-11-22",
            description = "Infaq Jumat",
            amount = 2800000,
            type = TransactionType.INCOME
        ),
        KasTransaction(
            id = "6",
            date = "2025-11-21",
            description = "Air PDAM",
            amount = 320000,
            type = TransactionType.EXPENSE
        )
    )

    val kasData = KasData(
        balance = 45250000,
        incomeMonth = 28500000,
        expenseMonth = 12750000,
        trendDirection = TrendDirection.UP,
        recentTransactions = kasTransactions,
        trendData = listOf(
            42000000L, 42500000L, 43000000L, 42800000L, 43200000L, 43500000L, 44000000L,
            43800000L, 44200000L, 44500000L, 44800000L, 45000000L, 45250000L
        )
    )

}
