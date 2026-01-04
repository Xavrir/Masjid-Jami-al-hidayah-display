package com.masjiddisplay.data

object MockData {
    val masjidConfig = MasjidConfig(
        name = "Masjid Jami' Al-Hidayah",
        location = "Jakarta Timur",
        tagline = "Makmurkan Masjid, Makmurkan Umat",
        latitude = -6.2088,
        longitude = 106.8456,
        calculationMethod = "Kemenag RI"
    )

    val kasData = KasData(
        balance = 15_750_000,
        incomeMonth = 8_500_000,
        expenseMonth = 4_200_000
    )

    val announcements = listOf(
        "Mohon menonaktifkan atau membisukan ponsel sebelum salat dimulai",
        "Kajian rutin setiap Ahad ba'da Maghrib",
        "Pendaftaran TPA dibuka untuk anak usia 5-12 tahun",
        "Infaq Jumat pekan ini: Rp 2.450.000"
    )
}
