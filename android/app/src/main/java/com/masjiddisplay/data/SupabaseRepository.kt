package com.masjiddisplay.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Repository for managing all data from Supabase
 * Handles API calls and data transformation with proper error handling and fallbacks
 */
object SupabaseRepository {
    
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(SupabaseConfig.SUPABASE_URL + "/")
        .client(createOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()
    
    private val apiService = retrofit.create(SupabaseApiService::class.java)
    
    // Authorization header
    private val authHeader: String
        get() = "Bearer ${SupabaseConfig.SUPABASE_KEY}"
    
    /**
     * Fetch Quran verses from Supabase and convert to local model
     * Returns empty list on error — never falls back to mock data
     */
    suspend fun getQuranVerses(): List<QuranVerse> = withContext(Dispatchers.IO) {
        try {
            val remote = apiService.getQuranVerses(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            remote.map { it.toLocal() }.also {
                println("✅ Successfully fetched ${it.size} Quran verses from Supabase")
            }
        } catch (e: Exception) {
            println("⚠️ Error fetching Quran verses: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Fetch Hadiths from Supabase and convert to local model
     * Returns empty list on error — never falls back to mock data
     */
    suspend fun getHadiths(): List<Hadith> = withContext(Dispatchers.IO) {
        try {
            val remote = apiService.getHadiths(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            remote.map { it.toLocal() }.also {
                println("✅ Successfully fetched ${it.size} Hadiths from Supabase")
            }
        } catch (e: Exception) {
            println("⚠️ Error fetching Hadiths: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Fetch Pengajian (Islamic teachings) from Supabase
     * Returns empty list on error
     */
    suspend fun getPengajian(): List<PengajianRemote> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPengajian(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            println("✅ Successfully fetched ${response.size} Pengajian entries from Supabase")
            response
        } catch (e: Exception) {
            println("⚠️ Error fetching Pengajian: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Fetch Kas (Treasury) data from Supabase
     * Calculates balance, monthly income/expense from kas_transaksi
     * Returns zeroed KasData on error
     */
    suspend fun getKasData(): KasData = withContext(Dispatchers.IO) {
        try {
            val transactions = apiService.getKasTransactions(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            
            if (transactions.isNotEmpty()) {
                var balance: Long = 0
                var incomeMonth: Long = 0
                var expenseMonth: Long = 0
                
                val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"), Locale.ROOT)
                val currentMonth = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                
                transactions.forEach { tx ->
                    val jenisLower = tx.jenis.lowercase()
                    if (jenisLower in listOf("pemasukan", "masuk")) {
                        balance += tx.nominal
                        if (tx.tanggal.startsWith(currentMonth)) {
                            incomeMonth += tx.nominal
                        }
                    } else {
                        balance -= tx.nominal
                        if (tx.tanggal.startsWith(currentMonth)) {
                            expenseMonth += tx.nominal
                        }
                    }
                }
                
                val trend = if (incomeMonth >= expenseMonth) TrendDirection.UP else TrendDirection.DOWN
                
                val recent = transactions.take(5).map { tx ->
                    KasTransaction(
                        id = tx.id.toString(),
                        date = tx.tanggal,
                        description = tx.keterangan ?: tx.kategori ?: "-",
                        amount = tx.nominal,
                        type = if (tx.jenis.lowercase() in listOf("pemasukan", "masuk")) TransactionType.INCOME else TransactionType.EXPENSE
                    )
                }
                
                KasData(
                    balance = balance,
                    incomeMonth = incomeMonth,
                    expenseMonth = expenseMonth,
                    trendDirection = trend,
                    recentTransactions = recent,
                    trendData = emptyList()
                ).also {
                    println("✅ Successfully calculated Kas data from ${transactions.size} transactions")
                }
            } else {
                println("⚠️ No transactions found in Supabase, returning zero data")
                KasData(
                    balance = 0,
                    incomeMonth = 0,
                    expenseMonth = 0,
                    trendDirection = TrendDirection.FLAT,
                    recentTransactions = emptyList(),
                    trendData = emptyList()
                )
            }
        } catch (e: Exception) {
            println("⚠️ Error fetching Kas transactions: ${e.message}")
            e.printStackTrace()
            KasData(
                balance = 0L,
                incomeMonth = 0L,
                expenseMonth = 0L,
                trendDirection = TrendDirection.FLAT,
                recentTransactions = emptyList(),
                trendData = emptyList()
            )
        }
    }
    
    /**
     * Fetch active banners from Supabase ordered by display_order
     * Returns empty list on error
     */
    suspend fun getBanners(): List<BannerRemote> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBanners(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            println("✅ Successfully fetched ${response.size} banners from Supabase")
            response
        } catch (e: Exception) {
            println("⚠️ Error fetching banners: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Get formatted Quran verses as display strings
     */
    suspend fun getQuranVersesForDisplay(): List<String> {
        val verses = getQuranVerses()
        if (verses.isEmpty()) return emptyList()
        
        return verses.map { verse ->
            val surahPart = if (verse.surah != null) "${verse.surah}" else "QS"
            val numPart = if (verse.surahNumber != null && verse.ayah != null) {
                " (${verse.surahNumber}:${verse.ayah})"
            } else if (verse.ayah != null) {
                " (Ayat ${verse.ayah})"
            } else ""
            
            val arabic = verse.arabic ?: ""
            val translation = verse.translation ?: verse.transliteration ?: ""
            
            if (arabic.isNotEmpty() && translation.isNotEmpty()) {
                "$surahPart$numPart: $arabic — $translation"
            } else if (arabic.isNotEmpty()) {
                "$surahPart$numPart: $arabic"
            } else if (translation.isNotEmpty()) {
                "$surahPart$numPart: $translation"
            } else {
                "$surahPart$numPart"
            }
        }
    }
    
    /**
     * Get formatted Hadiths as display strings
     */
    suspend fun getHadithsForDisplay(): List<String> {
        return getHadiths().map { hadith ->
            val arabic = hadith.arabic ?: ""
            val translation = hadith.translation ?: ""
            
            if (arabic.isNotEmpty() && translation.isNotEmpty()) {
                "${hadith.source}: $arabic — $translation"
            } else if (arabic.isNotEmpty()) {
                "${hadith.source}: $arabic"
            } else {
                "${hadith.source}: $translation"
            }
        }
    }
    
    /**
     * Get formatted Pengajian as display strings
     */
    suspend fun getPengajianForDisplay(): List<String> {
        return getPengajian()
            .filter { (it.judul ?: it.tema) != null && (it.pembicara ?: it.ustadz) != null }
            .map { pengajian ->
                val title = pengajian.judul ?: pengajian.tema ?: ""
                val speaker = pengajian.pembicara ?: pengajian.ustadz ?: ""
                val schedule = pengajian.hari ?: pengajian.tanggal ?: "-"
                "$title ($schedule - ${pengajian.jam ?: "-"}) - $speaker"
            }
    }
    
    /**
     * Get formatted Kas data as display string
     */
    suspend fun getKasDataForDisplay(): String {
        val kasData = getKasData()
        return "Pemasukan Bulan Ini: Rp${kasData.incomeMonth.formatCurrency()} | Pengeluaran Bulan Ini: Rp${kasData.expenseMonth.formatCurrency()}"
    }
    
}

/**
 * Extension function to format currency
 */
private fun Long.formatCurrency(): String {
    return this.toString().reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

/**
 * Extension functions to convert remote models to local models
 */
private fun QuranVerseRemote.toLocal(): QuranVerse {
    return QuranVerse(
        id = this.id.toString(),
        surah = this.surah,
        surahNumber = this.surahNumber,
        ayah = this.ayah,
        arabic = this.arabic,
        translation = this.translation,
        transliteration = this.transliteration
    )
}

private fun HadithRemote.toLocal(): Hadith {
    return Hadith(
        id = this.id.toString(),
        narrator = null,
        arabic = this.teks,
        translation = this.terjemahan,
        source = this.sumber,
        category = this.kategori
    )
}
