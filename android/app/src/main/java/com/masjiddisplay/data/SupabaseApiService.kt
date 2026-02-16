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

data class KasTransaksiRemote(
    val id: Int,
    val tanggal: String,
    val keterangan: String,
    val jumlah: Long,
    val jenis: String
)
