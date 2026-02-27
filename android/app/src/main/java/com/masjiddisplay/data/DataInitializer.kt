package com.masjiddisplay.data

/**
 * Data initializer for populating Supabase tables with sample data
 * This provides reference SQL statements and data structure for the required tables
 */
object DataInitializer {
    
    /**
     * SQL to create kas_masjid table
     */
    const val CREATE_KAS_MASJID_TABLE = """
        CREATE TABLE IF NOT EXISTS kas_masjid (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            balance BIGINT NOT NULL DEFAULT 0,
            income_month BIGINT NOT NULL DEFAULT 0,
            expense_month BIGINT NOT NULL DEFAULT 0,
            trend_direction VARCHAR(20) DEFAULT 'FLAT',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """
    
    /**
     * SQL to create ayat_quran table
     */
    const val CREATE_AYAT_QURAN_TABLE = """
        CREATE TABLE IF NOT EXISTS ayat_quran (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            surah VARCHAR(100) NOT NULL,
            "surahNumber" INTEGER NOT NULL,
            ayah INTEGER NOT NULL,
            arabic TEXT,
            translation TEXT,
            transliteration TEXT,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """
    
    /**
     * SQL to create hadits table
     */
    const val CREATE_HADITS_TABLE = """
        CREATE TABLE IF NOT EXISTS hadits (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            narrator VARCHAR(255) NOT NULL,
            arabic TEXT NOT NULL,
            translation TEXT NOT NULL,
            source VARCHAR(100) NOT NULL,
            category VARCHAR(100),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """
    
    /**
     * SQL to create pengajian table
     */
    const val CREATE_PENGAJIAN_TABLE = """
        CREATE TABLE IF NOT EXISTS pengajian (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            judul VARCHAR(255) NOT NULL,
            pembicara VARCHAR(255) NOT NULL,
            jam VARCHAR(10) NOT NULL,
            hari VARCHAR(50) NOT NULL,
            lokasi VARCHAR(255) NOT NULL,
            deskripsi TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """
    
    /**
     * Sample data for kas_masjid
     */
    val sampleKasData = mapOf(
        "balance" to 45250000L,
        "income_month" to 28500000L,
        "expense_month" to 12750000L,
        "trend_direction" to "UP"
    )
    
    /**
     * Sample data for ayat_quran
     */
    val sampleQuranVerses = listOf(
        mapOf(
            "surah" to "Al-Fatihah",
            "surahNumber" to 1,
            "ayah" to 1,
            "arabic" to "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
            "translation" to "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang",
            "transliteration" to "Bismi Allahi alrrahmani alrraheemi"
        ),
        mapOf(
            "surah" to "Al-Baqarah",
            "surahNumber" to 2,
            "ayah" to 186,
            "arabic" to "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ",
            "translation" to "Dan apabila hamba-hamba-Ku bertanya kepadamu tentang Aku, sesungguhnya Aku dekat",
            "transliteration" to "Wa idza sa-alaka 'ibaadii 'annii fa-innii qariib"
        ),
        mapOf(
            "surah" to "Ali 'Imran",
            "surahNumber" to 3,
            "ayah" to 159,
            "arabic" to "فَاعْفُ عَنْهُمْ وَاسْتَغْفِرْ لَهُمْ وَشَاوِرْهُمْ فِي الْأَمْرِ",
            "translation" to "Maka maafkanlah mereka dan mohonkanlah ampunan untuk mereka",
            "transliteration" to "Fa'fu 'anhum wastaghfir lahum wa syawirhum fil-amr"
        ),
        mapOf(
            "surah" to "Al-Insyirah",
            "surahNumber" to 94,
            "ayah" to 5,
            "arabic" to "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "translation" to "Maka sesungguhnya bersama kesulitan ada kemudahan",
            "transliteration" to "Fa-inna ma'al-'usri yusraa"
        )
    )
    
    /**
     * Sample data for hadits
     */
    val sampleHadiths = listOf(
        mapOf(
            "narrator" to "Abu Hurairah RA",
            "arabic" to "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            "translation" to "Sebaik-baik kalian adalah yang mempelajari Al-Qur'an dan mengajarkannya",
            "source" to "HR. Bukhari",
            "category" to "Keutamaan Ilmu"
        ),
        mapOf(
            "narrator" to "Abu Hurairah RA",
            "arabic" to "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
            "translation" to "Barangsiapa menempuh jalan untuk mencari ilmu, maka Allah akan memudahkan baginya jalan menuju surga",
            "source" to "HR. Muslim",
            "category" to "Keutamaan Ilmu"
        ),
        mapOf(
            "narrator" to "Umar bin Khattab RA",
            "arabic" to "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ",
            "translation" to "Sesungguhnya setiap amalan tergantung pada niatnya",
            "source" to "HR. Bukhari & Muslim",
            "category" to "Niat & Ikhlas"
        )
    )
    
    /**
     * Sample data for pengajian
     */
    val samplePengajian = listOf(
        mapOf(
            "judul" to "Kajian Fikih Praktis",
            "pembicara" to "Ustadz Ahmad Abdullah",
            "jam" to "19:00",
            "hari" to "Senin",
            "lokasi" to "Ruang Utama Masjid",
            "deskripsi" to "Pembahasan hukum-hukum fikih yang praktis dalam kehidupan sehari-hari"
        ),
        mapOf(
            "judul" to "Tafsir Al-Quran Juz Amma",
            "pembicara" to "Ustadz Muhammad Hasan",
            "jam" to "20:00",
            "hari" to "Rabu",
            "lokasi" to "Ruang Utama Masjid",
            "deskripsi" to "Pembacaan dan penjabaran makna surat-surat pendek dalam juz amma"
        ),
        mapOf(
            "judul" to "Adab dan Akhlak Islam",
            "pembicara" to "Ustadzah Siti Nurhaliza",
            "jam" to "17:00",
            "hari" to "Ahad",
            "lokasi" to "Ruang Perempuan",
            "deskripsi" to "Pembahasan tentang akhlak mulia menurut ajaran Islam"
        ),
        mapOf(
            "judul" to "Pendidikan Islam untuk Anak",
            "pembicara" to "Ustadzah Fatimah",
            "jam" to "15:00",
            "hari" to "Sabtu",
            "lokasi" to "Ruang TPA",
            "deskripsi" to "Program pembelajaran Islam untuk anak-anak usia 5-12 tahun"
        )
    )
}

/**
 * Data class for displaying Supabase table information
 */
data class SupabaseTableInfo(
    val tableName: String,
    val rowCount: Int,
    val lastUpdated: String
)
