package com.masjiddisplay.ui.state

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import com.masjiddisplay.data.*

/**
 * ViewModel for managing Islamic content (Quran, Hadith, Pengajian) from Supabase
 */
class IslamicContentViewModel {
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    val quranVerses = mutableStateOf<List<QuranVerse>>(com.masjiddisplay.data.quranVerses)
    val hadiths = mutableStateOf<List<Hadith>>(com.masjiddisplay.data.hadiths)
    val pengajian = mutableStateOf<List<PengajianRemote>>(emptyList())
    val kasData = mutableStateOf<KasData?>(null)
    
    val isLoading = mutableStateOf(true)
    val error = mutableStateOf<String?>(null)
    
    private var quranIndex = 0
    private var hadithIndex = 0
    
    init {
        loadAllData()
        // Refresh data every 5 minutes
        scope.launch {
            while (isActive) {
                delay(5 * 60 * 1000) // 5 minutes
                loadAllData()
            }
        }
    }
    
    /**
     * Load all data from Supabase
     */
    fun loadAllData() {
        scope.launch {
            try {
                isLoading.value = true
                error.value = null
                
                // Load Quran verses
                val quranList = SupabaseRepository.getQuranVerses()
                if (quranList.isNotEmpty()) {
                    quranVerses.value = quranList
                }
                
                // Load Hadiths
                val hadithList = SupabaseRepository.getHadiths()
                if (hadithList.isNotEmpty()) {
                    hadiths.value = hadithList
                }
                
                // Load Pengajian
                val pengajianList = SupabaseRepository.getPengajian()
                pengajian.value = pengajianList
                
                // Load Kas data
                val kas = SupabaseRepository.getKasData()
                kasData.value = kas
                
                isLoading.value = false
            } catch (e: Exception) {
                error.value = e.message
                isLoading.value = false
            }
        }
    }
    
    /**
     * Get next Quran verse for rotation
     */
    fun getNextQuranVerse(): QuranVerse {
        if (quranVerses.value.isEmpty()) return defaultQuranVerse
        val verse = quranVerses.value[quranIndex % quranVerses.value.size]
        quranIndex++
        return verse
    }
    
    /**
     * Get next Hadith for rotation
     */
    fun getNextHadith(): Hadith {
        if (hadiths.value.isEmpty()) return defaultHadith
        val hadith = hadiths.value[hadithIndex % hadiths.value.size]
        hadithIndex++
        return hadith
    }
    
    /**
     * Reload data manually
     */
    fun refreshData() {
        loadAllData()
    }
    
    fun onCleared() {
        scope.cancel()
    }
}

// Default content for fallback
internal val defaultQuranVerse = QuranVerse(
    id = "default",
    surah = "Al-Fatihah",
    surahNumber = 1,
    ayah = 1,
    arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
    translation = "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang.",
    transliteration = "Bismillahir-rahmanir-rahim"
)

internal val defaultHadith = Hadith(
    id = "default",
    narrator = "Rasulullah SAW",
    arabic = "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ",
    translation = "Sesungguhnya setiap amalan tergantung pada niatnya.",
    source = "HR. Bukhari & Muslim",
    category = "Niat"
)
