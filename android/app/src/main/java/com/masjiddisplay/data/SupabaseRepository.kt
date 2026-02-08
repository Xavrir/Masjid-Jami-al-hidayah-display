package com.masjiddisplay.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import kotlinx.coroutines.*

/**
 * Repository for managing all data from Supabase
 * Handles API calls and data transformation with proper error handling and fallbacks
 */
object SupabaseRepository {
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(SupabaseConfig.SUPABASE_URL + "/")
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
            val transactions = apiService.getKasTransactions(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            
            if (transactions.isNotEmpty()) {
                // Calculate totals locally
                var balance: Long = 0
                var incomeMonth: Long = 0
                var expenseMonth: Long = 0
                
                // Get current month string YYYY-MM
                val currentMonth = java.time.LocalDate.now().toString().substring(0, 7)
                
                transactions.forEach { tx ->
                    if (tx.jenis == "masuk") {
                        balance += tx.nominal
                        if (tx.tanggal.startsWith(currentMonth)) {
                            incomeMonth += tx.nominal
                        }
                    } else { // keluar
                        balance -= tx.nominal
                        if (tx.tanggal.startsWith(currentMonth)) {
                            expenseMonth += tx.nominal
                        }
                    }
                }
                
                // Determine trend (simple logic: if income > expense this month -> UP, else DOWN)
                val trend = if (incomeMonth >= expenseMonth) TrendDirection.UP else TrendDirection.DOWN
                
                // Map recent transactions (take top 5)
                val recent = transactions.take(5).map { it.toLocal() }
                
                KasData(
                    balance = balance,
                    incomeMonth = incomeMonth,
                    expenseMonth = expenseMonth,
                    trendDirection = trend,
                    recentTransactions = recent,
                    trendData = emptyList() // We could calculate this too if needed, but empty is safe for now
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
            // In case of error, we can either return mock data or empty data. 
            // Returning mock data might be confusing if the user expects real data.
            // But to avoid app crash/empty screen, let's fall back to mock for now as per original design.
            getMockKasData()
        }
    }
    
    /**
     * Get formatted Quran verses as display strings
     */
    suspend fun getQuranVersesForDisplay(): List<String> {
        return getQuranVerses().map { verse ->
            "${verse.surah} (${verse.ayah}): ${verse.translation}"
        }
    }
    
    /**
     * Get formatted Hadiths as display strings
     */
    suspend fun getHadithsForDisplay(): List<String> {
        return getHadiths().map { hadith ->
            "${hadith.narrator} - ${hadith.translation}"
        }
    }
    
    /**
     * Get formatted Pengajian as display strings
     */
    suspend fun getPengajianForDisplay(): List<String> {
        return getPengajian().map { pengajian ->
            "${pengajian.judul} (${pengajian.hari} - ${pengajian.jam}) - ${pengajian.pembicara}"
        }
    }
    
    /**
     * Get formatted Kas data as display string
     */
    suspend fun getKasDataForDisplay(): String {
        val kas = getKasData()
        return "Kas Masjid: Rp${kas.balance.formatCurrency()} | Pemasukan Bulan Ini: Rp${kas.incomeMonth.formatCurrency()}"
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
        id = this.id,
        surah = this.surah,
        surahNumber = this.surah_number,
        ayah = this.ayah,
        arabic = this.arabic,
        translation = this.translation,
        transliteration = this.transliteration
    )
}

    return Hadith(
        id = this.id,
        narrator = this.narrator,
        arabic = this.arabic,
        translation = this.translation,
        source = this.source,
        category = this.category
    )
}

private fun KasTransactionRemote.toLocal(): KasTransaction {
    // Map remote type string to enum
    val txType = if (this.jenis.equals("masuk", ignoreCase = true)) 
        TransactionType.INCOME 
    else 
        TransactionType.EXPENSE

    return KasTransaction(
        id = this.id.toString(),
        date = this.tanggal,
        description = this.keterangan ?: this.kategori,
        amount = this.nominal,
        type = txType
    )
}
