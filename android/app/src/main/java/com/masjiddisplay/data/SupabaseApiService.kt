package com.masjiddisplay.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

/**
 * Retrofit interface for Supabase REST API calls
 */
interface SupabaseApiService {
    
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
        @Header("Authorization") auth: String,
        @Query("order") order: String = "surahNumber.asc,ayah.asc"
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

    @GET("rest/v1/banners")
    suspend fun getBanners(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("select") select: String = "*",
        @Query("aktif") aktif: String = "eq.true",
        @Query("order") order: String = "display_order.asc"
    ): List<BannerRemote>
}

/**
 * Remote data models matching Supabase database schema
 */
data class QuranVerseRemote(
    val id: Any, // Can be String or Int
    val surah: String? = null,
    @SerializedName("surahNumber")
    val surahNumber: Int? = null,
    val ayah: Int? = null,
    val arabic: String? = null,
    val translation: String? = null,
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
    val deskripsi: String? = null,
    // Admin panel uses these column names instead of judul/pembicara
    val tema: String? = null,
    val ustadz: String? = null,
    val tanggal: String? = null,
    val aktif: Boolean? = null
)

data class KasTransaksiRemote(
    val id: Int,
    val tanggal: String,
    val keterangan: String? = null,
    val nominal: Long,
    val jenis: String,
    val kategori: String? = null
)

data class BannerRemote(
    val id: Int,
    val title: String? = null,
    val image_url: String,
    val type: String? = "image",
    val display_order: Int = 1,
    val aktif: Boolean = true,
    val created_at: String? = null
)
