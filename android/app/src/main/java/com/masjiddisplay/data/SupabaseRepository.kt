package com.masjiddisplay.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.lang.reflect.Type
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Repository for managing all data from Supabase.
 *
 * The TV only has one active client, so the best savings come from:
 * - aggressively caching media on disk
 * - spacing out API refreshes
 * - reusing the last successful payload after app restarts
 */
object SupabaseRepository {

    private const val HTTP_CACHE_SIZE_BYTES = 10L * 1024 * 1024
    private const val HTTP_MAX_AGE_SECONDS = 300

    private const val CACHE_KAS_DATA = "kas_data"
    private const val CACHE_QURAN_DISPLAY = "quran_display"
    private const val CACHE_HADITH_DISPLAY = "hadith_display"
    private const val CACHE_PENGAJIAN_DISPLAY = "pengajian_display"
    private const val CACHE_BANNERS = "banners"

    private lateinit var applicationContext: Context

    private val gson = Gson()
    private val kasDataType = object : TypeToken<KasData>() {}.type
    private val stringListType = object : TypeToken<List<String>>() {}.type
    private val bannerListType = object : TypeToken<List<BannerRemote>>() {}.type

    private val apiService: SupabaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.SUPABASE_URL + "/")
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SupabaseApiService::class.java)
    }

    fun initialize(context: Context) {
        if (::applicationContext.isInitialized) return
        applicationContext = context.applicationContext
    }

    fun getCachedKasData(): KasData? = readCachedValue<KasData>(CACHE_KAS_DATA, kasDataType)

    fun getCachedQuranVersesForDisplay(): List<String> =
        readCachedValue<List<String>>(CACHE_QURAN_DISPLAY, stringListType).orEmpty()

    fun getCachedHadithsForDisplay(): List<String> =
        readCachedValue<List<String>>(CACHE_HADITH_DISPLAY, stringListType).orEmpty()

    fun getCachedPengajianForDisplay(): List<String> =
        readCachedValue<List<String>>(CACHE_PENGAJIAN_DISPLAY, stringListType).orEmpty()

    fun getCachedBanners(): List<BannerRemote> =
        readCachedValue<List<BannerRemote>>(CACHE_BANNERS, bannerListType).orEmpty()

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (!chain.request().method.equals("GET", ignoreCase = true)) {
                    return@addNetworkInterceptor response
                }

                response.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public, max-age=$HTTP_MAX_AGE_SECONDS")
                    .build()
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        if (::applicationContext.isInitialized) {
            builder.cache(Cache(File(applicationContext.cacheDir, "http-cache"), HTTP_CACHE_SIZE_BYTES))
        }

        return builder.build()
    }

    private val authHeader: String
        get() = "Bearer ${SupabaseConfig.SUPABASE_KEY}"

    suspend fun getKasData(maxAgeMs: Long = 0L): KasData = withContext(Dispatchers.IO) {
        val cachedKasData: KasData? = getFreshCachedValue(CACHE_KAS_DATA, kasDataType, maxAgeMs)
        if (cachedKasData != null) {
            return@withContext cachedKasData
        }

        try {
            val transactions = apiService.getKasTransactions(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )

            val result = if (transactions.isNotEmpty()) {
                var balance: Long = 0
                var incomeMonth: Long = 0
                var expenseMonth: Long = 0

                val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"), Locale.ROOT)
                val currentMonth = "%04d-%02d".format(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1
                )

                transactions.forEach { transaction ->
                    val jenisLower = transaction.jenis.lowercase()
                    if (jenisLower in listOf("pemasukan", "masuk")) {
                        balance += transaction.nominal
                        if (transaction.tanggal.startsWith(currentMonth)) {
                            incomeMonth += transaction.nominal
                        }
                    } else {
                        balance -= transaction.nominal
                        if (transaction.tanggal.startsWith(currentMonth)) {
                            expenseMonth += transaction.nominal
                        }
                    }
                }

                val trendDirection = if (incomeMonth >= expenseMonth) {
                    TrendDirection.UP
                } else {
                    TrendDirection.DOWN
                }

                KasData(
                    balance = balance,
                    incomeMonth = incomeMonth,
                    expenseMonth = expenseMonth,
                    trendDirection = trendDirection,
                    recentTransactions = transactions.take(5).map { transaction ->
                        KasTransaction(
                            id = transaction.id.toString(),
                            date = transaction.tanggal,
                            description = transaction.keterangan ?: transaction.kategori ?: "-",
                            amount = transaction.nominal,
                            type = if (transaction.jenis.lowercase() in listOf("pemasukan", "masuk")) {
                                TransactionType.INCOME
                            } else {
                                TransactionType.EXPENSE
                            }
                        )
                    },
                    trendData = emptyList()
                )
            } else {
                KasData(
                    balance = 0L,
                    incomeMonth = 0L,
                    expenseMonth = 0L,
                    trendDirection = TrendDirection.FLAT,
                    recentTransactions = emptyList(),
                    trendData = emptyList()
                )
            }

            println("✅ Successfully calculated Kas data from ${transactions.size} transactions")
            if (transactions.isNotEmpty()) {
                writeCachedValue(CACHE_KAS_DATA, result, kasDataType)
            }
            result
        } catch (e: Exception) {
            println("⚠️ Error fetching Kas transactions: ${e.message}")
            e.printStackTrace()
            readCachedValue<KasData>(CACHE_KAS_DATA, kasDataType) ?: KasData(
                balance = 0L,
                incomeMonth = 0L,
                expenseMonth = 0L,
                trendDirection = TrendDirection.FLAT,
                recentTransactions = emptyList(),
                trendData = emptyList()
            )
        }
    }

    suspend fun getBanners(maxAgeMs: Long = 0L): List<BannerRemote> = withContext(Dispatchers.IO) {
        val cachedBanners: List<BannerRemote>? = getFreshCachedValue(CACHE_BANNERS, bannerListType, maxAgeMs)
        if (cachedBanners != null) {
            return@withContext cachedBanners
        }

        try {
            val response = apiService.getBanners(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            println("✅ Successfully fetched ${response.size} banners from Supabase")
            writeCachedValue(CACHE_BANNERS, response, bannerListType)
            response
        } catch (e: Exception) {
            println("⚠️ Error fetching banners: ${e.message}")
            e.printStackTrace()
            readCachedValue<List<BannerRemote>>(CACHE_BANNERS, bannerListType) ?: emptyList()
        }
    }

    suspend fun getQuranVersesForDisplay(maxAgeMs: Long = 0L): List<String> = withContext(Dispatchers.IO) {
        val cachedQuran: List<String>? = getFreshCachedValue(CACHE_QURAN_DISPLAY, stringListType, maxAgeMs)
        if (cachedQuran != null) {
            return@withContext cachedQuran
        }

        try {
            val remote = apiService.getQuranVerses(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            println("✅ Successfully fetched ${remote.size} Quran verses from Supabase")

            val displayData = remote.toDisplayStrings()
            writeCachedValue(CACHE_QURAN_DISPLAY, displayData, stringListType)
            displayData
        } catch (e: Exception) {
            println("⚠️ Error fetching Quran verses: ${e.message}")
            e.printStackTrace()
            readCachedValue<List<String>>(CACHE_QURAN_DISPLAY, stringListType) ?: emptyList()
        }
    }

    suspend fun getHadithsForDisplay(maxAgeMs: Long = 0L): List<String> = withContext(Dispatchers.IO) {
        val cachedHadiths: List<String>? = getFreshCachedValue(CACHE_HADITH_DISPLAY, stringListType, maxAgeMs)
        if (cachedHadiths != null) {
            return@withContext cachedHadiths
        }

        try {
            val remote = apiService.getHadiths(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            println("✅ Successfully fetched ${remote.size} Hadiths from Supabase")

            val displayData = remote.map { it.toLocal() }.map { hadith ->
                val arabic = hadith.arabic
                val translation = hadith.translation ?: ""

                when {
                    arabic.isNotEmpty() && translation.isNotEmpty() -> "${hadith.source}: $arabic — $translation"
                    arabic.isNotEmpty() -> "${hadith.source}: $arabic"
                    else -> "${hadith.source}: $translation"
                }
            }

            writeCachedValue(CACHE_HADITH_DISPLAY, displayData, stringListType)
            displayData
        } catch (e: Exception) {
            println("⚠️ Error fetching Hadiths: ${e.message}")
            e.printStackTrace()
            readCachedValue<List<String>>(CACHE_HADITH_DISPLAY, stringListType) ?: emptyList()
        }
    }

    suspend fun getPengajianForDisplay(maxAgeMs: Long = 0L): List<String> = withContext(Dispatchers.IO) {
        val cachedPengajian: List<String>? = getFreshCachedValue(CACHE_PENGAJIAN_DISPLAY, stringListType, maxAgeMs)
        if (cachedPengajian != null) {
            return@withContext cachedPengajian
        }

        try {
            val response = apiService.getPengajian(
                apiKey = SupabaseConfig.SUPABASE_KEY,
                auth = authHeader
            )
            println("✅ Successfully fetched ${response.size} Pengajian entries from Supabase")

            val displayData = response
                .filter { (it.judul ?: it.tema) != null && (it.pembicara ?: it.ustadz) != null }
                .map { pengajian ->
                    val title = pengajian.judul ?: pengajian.tema ?: ""
                    val speaker = pengajian.pembicara ?: pengajian.ustadz ?: ""
                    val schedule = pengajian.hari ?: pengajian.tanggal ?: "-"
                    "$title ($schedule - ${pengajian.jam ?: "-"}) - $speaker"
                }

            writeCachedValue(CACHE_PENGAJIAN_DISPLAY, displayData, stringListType)
            displayData
        } catch (e: Exception) {
            println("⚠️ Error fetching Pengajian: ${e.message}")
            e.printStackTrace()
            readCachedValue<List<String>>(CACHE_PENGAJIAN_DISPLAY, stringListType) ?: emptyList()
        }
    }

    private fun <T> getFreshCachedValue(key: String, type: Type, maxAgeMs: Long): T? {
        val cachedValue = LocalCache.read<T>(key, type) ?: return null
        if (maxAgeMs <= 0L) return null

        val ageMs = System.currentTimeMillis() - cachedValue.savedAt
        return cachedValue.value.takeIf { ageMs < maxAgeMs }
    }

    private fun <T> readCachedValue(key: String, type: Type): T? {
        return LocalCache.read<T>(key, type)?.value
    }

    private fun <T> writeCachedValue(key: String, value: T, type: Type) {
        LocalCache.write(key, value, type)
    }
}

private fun Long.formatCurrency(): String {
    return this.toString().reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

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

private fun List<QuranVerseRemote>.toDisplayStrings(): List<String> {
    return map { it.toLocal() }.map { verse ->
        val surahPart = if (verse.surah != null) "${verse.surah}" else "QS"
        val numPart = if (verse.surahNumber != null && verse.ayah != null) {
            " (${verse.surahNumber}:${verse.ayah})"
        } else if (verse.ayah != null) {
            " (Ayat ${verse.ayah})"
        } else {
            ""
        }

        val arabic = verse.arabic ?: ""
        val translation = verse.translation ?: verse.transliteration ?: ""

        when {
            arabic.isNotEmpty() && translation.isNotEmpty() -> "$surahPart$numPart: $arabic — $translation"
            arabic.isNotEmpty() -> "$surahPart$numPart: $arabic"
            translation.isNotEmpty() -> "$surahPart$numPart: $translation"
            else -> "$surahPart$numPart"
        }
    }
}
