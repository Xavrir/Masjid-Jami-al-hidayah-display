package com.masjiddisplay.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Repository for managing all data from Supabase
 * Handles API calls and data transformation with proper error handling and fallbacks
 */
object SupabaseRepository {
    
    /**
     * Create OkHttpClient that trusts all certificates (for debug/emulator only)
     * This bypasses SSL certificate validation issues on emulators with outdated CA stores
     */
    private fun createUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(SupabaseConfig.SUPABASE_URL + "/")
        .client(createUnsafeOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()
    
    private val apiService = retrofit.create(SupabaseApiService::class.java)
    
    // Authorization header
    private val authHeader: String
        get() = "Bearer ${SupabaseConfig.SUPABASE_KEY}"
    
    /**
     * Fetch Quran verses from Supabase and convert to local model
     * Falls back to mock data on error
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
            quranVerses // Return local mock data on error
        }
    }
    
    /**
     * Fetch Hadiths from Supabase and convert to local model
     * Falls back to mock data on error
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
            hadiths // Return local mock data on error
        }
    }
    
    /**
     * Fetch Pengajian (Islamic teachings) from Supabase
     * Falls back to mock data on error
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
     * Falls back to mock data on error
     */
    suspend fun getKasData(): KasData = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getKasData(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            
            if (response.isNotEmpty()) {
                val data = response[0]
                KasData(
                    balance = (data["balance"] as? Number)?.toLong() ?: 0L,
                    incomeMonth = (data["income_month"] as? Number)?.toLong() ?: 0L,
                    expenseMonth = (data["expense_month"] as? Number)?.toLong() ?: 0L,
                    trendDirection = TrendDirection.valueOf(
                        (data["trend_direction"] as? String)?.uppercase() ?: "FLAT"
                    ),
                    recentTransactions = emptyList(),
                    trendData = emptyList()
                ).also {
                    println("✅ Successfully fetched Kas data from Supabase")
                }
            } else {
                println("⚠️ No Kas data found in Supabase, using mock data")
                getMockKasData()
            }
        } catch (e: Exception) {
            println("⚠️ Error fetching Kas data: ${e.message}")
            e.printStackTrace()
            getMockKasData()
        }
    }
    
    /**
     * Get formatted Quran verses as display strings
     */
    suspend fun getQuranVersesForDisplay(): List<String> {
        return getQuranVerses().map { verse ->
            val text = verse.translation ?: verse.transliteration ?: verse.arabic
            "${verse.surah} (${verse.ayah}): $text"
        }
    }
    
    /**
     * Get formatted Hadiths as display strings
     */
    suspend fun getHadithsForDisplay(): List<String> {
        return getHadiths().map { hadith ->
            val text = hadith.translation ?: hadith.arabic
            "${hadith.source}: $text"
        }
    }
    
    /**
     * Get formatted Pengajian as display strings
     */
    suspend fun getPengajianForDisplay(): List<String> {
        return getPengajian()
            .filter { it.judul != null && it.pembicara != null }
            .map { pengajian ->
                "${pengajian.judul} (${pengajian.hari ?: "-"} - ${pengajian.jam ?: "-"}) - ${pengajian.pembicara}"
            }
    }
    
    /**
     * Fetch Kas transactions from Supabase
     */
    suspend fun getKasTransactions(): List<KasTransaksiRemote> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getKasTransactions(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            println("✅ Successfully fetched ${response.size} Kas transactions from Supabase")
            response
        } catch (e: Exception) {
            println("⚠️ Error fetching Kas transactions: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Calculate monthly pemasukan and pengeluaran from kas_transaksi
     */
    suspend fun getMonthlyKasSummary(): Pair<Long, Long> = withContext(Dispatchers.IO) {
        try {
            val transactions = getKasTransactions()
            val currentMonth = YearMonth.now()
            
            var pemasukan = 0L
            var pengeluaran = 0L
            
            transactions.forEach { tx ->
                try {
                    val dateStr = tx.tanggal.substringBefore('T')
                    val txDate = LocalDate.parse(dateStr)
                    val txMonth = YearMonth.from(txDate)
                    
                    if (txMonth == currentMonth) {
                        val jenisLower = tx.jenis.lowercase()
                        when {
                            jenisLower in listOf("pemasukan", "masuk") -> pemasukan += tx.jumlah
                            jenisLower in listOf("pengeluaran", "keluar") -> pengeluaran += tx.jumlah
                        }
                    }
                } catch (e: Exception) {
                    println("⚠️ Error parsing transaction: ${e.message}")
                }
            }
            
            println("✅ Monthly summary - Pemasukan: Rp${pemasukan.formatCurrency()}, Pengeluaran: Rp${pengeluaran.formatCurrency()}")
            Pair(pemasukan, pengeluaran)
        } catch (e: Exception) {
            println("⚠️ Error calculating monthly summary: ${e.message}")
            e.printStackTrace()
            Pair(0L, 0L)
        }
    }
    
    /**
     * Get formatted Kas data as display string using real transaction data
     */
    suspend fun getKasDataForDisplay(): String {
        val (pemasukan, pengeluaran) = getMonthlyKasSummary()
        return "Pemasukan Bulan Ini: Rp${pemasukan.formatCurrency()} | Pengeluaran Bulan Ini: Rp${pengeluaran.formatCurrency()}"
    }
    
    /**
     * Get mock KAS data as fallback
     */
    private fun getMockKasData(): KasData {
        return MockData.kasData
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
        translation = this.transliteration,
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
