package com.masjiddisplay.data

import retrofit2.http.*

/**
 * Retrofit interface for Supabase REST API calls
 */
interface SupabaseApiService {
    
    @GET("rest/v1/kas_masjid")
    suspend fun getKasData(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<Map<String, Any>>
    
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
    val id: String,
    val date: String,
    val description: String,
    val amount: Long,
    val type: String
)
