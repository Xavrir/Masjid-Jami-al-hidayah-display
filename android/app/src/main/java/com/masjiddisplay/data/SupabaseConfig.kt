package com.masjiddisplay.data

/**
 * Supabase configuration constants
 * URL: https://wqupptqjbkuldglnpvor.supabase.co
 */
object SupabaseConfig {
    const val SUPABASE_URL = "https://wqupptqjbkuldglnpvor.supabase.co"
    // Using anon key for public data access
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndxdXBwdHFqYmt1bGRnbG5wdm9yIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTkwMDAwMDAwMH0.PLACEHOLDER"
    
    // Table names for Islamic content and mosque management
    const val TABLE_KAS_MASJID = "kas_masjid"
    const val TABLE_AYAT_QURAN = "ayat_quran"
    const val TABLE_HADITS = "hadits"
    const val TABLE_PENGAJIAN = "pengajian"
    
    // Endpoints for REST API
    const val ENDPOINT_KAS = "/rest/v1/kas_masjid"
    const val ENDPOINT_QURAN = "/rest/v1/ayat_quran"
    const val ENDPOINT_HADITS = "/rest/v1/hadits"
    const val ENDPOINT_PENGAJIAN = "/rest/v1/pengajian"
}
