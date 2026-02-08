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
 * Remote data models for API responses
 */
data class QuranVerseRemote(
    val id: String,
    val surah: String,
    val surah_number: Int,
    val ayah: Int,
    val arabic: String,
    val translation: String,
    val transliteration: String? = null
)

data class HadithRemote(
    val id: String,
    val narrator: String,
    val arabic: String,
    val translation: String,
    val source: String,
    val category: String
)

data class PengajianRemote(
    val id: String,
    val judul: String,
    val pembicara: String,
    val jam: String,
    val hari: String,
    val lokasi: String,
    val deskripsi: String? = null
)

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
)
