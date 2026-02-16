package com.masjiddisplay.data

import retrofit2.http.*

/**
 * Retrofit interface for Supabase REST API calls
 */
interface SupabaseApiService {
    
    @GET("rest/v1/kas_transaksi?select=*&order=tanggal.desc")
    suspend fun getKasTransactions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<KasTransactionRemote>
    
    @GET("rest/v1/kas_transaksi")
    suspend fun getKasTransactions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "tanggal.desc"
    ): List<KasTransaksiRemote>
    
    @GET("rest/v1/ayat_quran")
    suspend fun getQuranVerses(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<QuranVerseRemote>
    
    @GET("rest/v1/hadits")
    suspend fun getHadiths(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<HadithRemote>
    
    @GET("rest/v1/pengajian")
    suspend fun getPengajian(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<PengajianRemote>
}

/**
 * Remote data models matching Supabase database schema
 */
data class QuranVerseRemote(
    val id: String,
    val surah: String,
    val surahNumber: Int,
    val ayah: Int,
    val arabic: String,
    val transliteration: String? = null
)

data class HadithRemote(
    val id: String,
    val teks: String,
    val sumber: String,
    val kategori: String? = null,
    val aktif: Boolean = true,
    val terjemahan: String? = null
)

data class PengajianRemote(
    val id: String,
    val judul: String? = null,
    val pembicara: String? = null,
    val jam: String? = null,
    val hari: String? = null,
    val lokasi: String? = null,
    val deskripsi: String? = null
)

<<<<<<< HEAD
data class KasTransactionRemote(
    val id: Long, // Supabase ID is usually Int/Long, but let's check. Admin panel uses it as ID. 
    // Wait, the admin panel uses `id` which is auto increment int usually.
    // Let's use Long to be safe or String if unsure. 
    // In MockData it was String "1".
    // In admin panel JS: `transactions = (data || []).map(item => ({ id: item.id ...`
    // Let's check `KasTransaction` model in Models.kt -> `val id: String`.
    // So we can map it later.
    // Models in `SupabaseApiService` should match JSON response.
    // `kas_transaksi` columns: id, jenis, nominal, keterangan, tanggal, kategori
    val id: Long, 
    val tanggal: String,
    val keterangan: String?,
    val nominal: Long,
    val jenis: String,
    val kategori: String
=======
data class KasTransaksiRemote(
    val id: Int,
    val tanggal: String,
    val keterangan: String? = null,
    val nominal: Long,
    val jenis: String,
    val kategori: String? = null
>>>>>>> 28f3afdf1a41b50cfbfc7feb5ae653a6d8c689b2
)
