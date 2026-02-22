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
                
                val currentMonth = LocalDate.now().toString().substring(0, 7)
                
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
