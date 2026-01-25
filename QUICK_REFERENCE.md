# Quick Reference - Masjid Display Supabase Integration

## Supabase Project Details
- **URL**: https://wqupptqjbkuldglnpvor.supabase.co
- **Project ID**: wqupptqjbkuldglnpvor
- **Region**: (Configured in dashboard)

## 4 Main Database Tables

### 1. kas_masjid (Treasury Management)
**Display Update**: Treasury balance and income/expense info shown at bottom
```json
{
  "id": "uuid",
  "balance": 45250000,
  "income_month": 28500000,
  "expense_month": 12750000,
  "trend_direction": "UP"
}
```

### 2. ayat_quran (Quranic Verses)
**Display Update**: Rotates every 10 seconds in running text loop
```json
{
  "id": "uuid",
  "surah": "Al-Baqarah",
  "surah_number": 2,
  "ayah": 186,
  "arabic": "وَإِذَا سَأَلَكَ عِبَادِي...",
  "translation": "And when My servants ask you..."
}
```

### 3. hadits (Islamic Teachings)
**Display Update**: Rotates every 10 seconds in running text loop
```json
{
  "id": "uuid",
  "narrator": "Abu Hurairah RA",
  "arabic": "خَيْرُكُمْ مَنْ تَعَلَّمَ...",
  "translation": "The best among you...",
  "source": "HR. Bukhari",
  "category": "Keutamaan Ilmu"
}
```

### 4. pengajian (Teaching Schedules)
**Display Update**: Rotates every 8 seconds in running text loop
```json
{
  "id": "uuid",
  "judul": "Kajian Fikih Praktis",
  "pembicara": "Ustadz Ahmad Abdullah",
  "jam": "19:00",
  "hari": "Senin",
  "lokasi": "Ruang Utama Masjid",
  "deskripsi": "Pembahasan hukum-hukum fikih..."
}
```

## Running Text Loop Schedule
- **Announcements**: 6 seconds each
- **Quran Verses**: 10 seconds each
- **Hadiths**: 10 seconds each
- **Teaching Schedules**: 8 seconds each

## Key Files Reference

| File | Purpose |
|------|---------|
| [SupabaseConfig.kt](android/app/src/main/java/com/masjiddisplay/data/SupabaseConfig.kt) | API URL & Keys |
| [SupabaseApiService.kt](android/app/src/main/java/com/masjiddisplay/data/SupabaseApiService.kt) | REST API Endpoints |
| [SupabaseRepository.kt](android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt) | Data Layer |
| [MainActivity.kt](android/app/src/main/java/com/masjiddisplay/MainActivity.kt) | Data Fetching |
| [MainDashboard.kt](android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt) | Display Logic |
| [EnhancedRunningText.kt](android/app/src/main/java/com/masjiddisplay/ui/components/EnhancedRunningText.kt) | Running Text Display |

## How to Add New Content

### Add New Quranic Verse
1. Go to Supabase Dashboard
2. Open **ayat_quran** table
3. Click "Insert new row"
4. Fill: surah, surah_number, ayah, arabic, translation, transliteration (optional)
5. Restart app - verse appears in rotation

### Add New Hadith
1. Go to Supabase Dashboard
2. Open **hadits** table
3. Click "Insert new row"
4. Fill: narrator, arabic, translation, source, category
5. Restart app - hadith appears in rotation

### Add New Teaching Schedule
1. Go to Supabase Dashboard
2. Open **pengajian** table
3. Click "Insert new row"
4. Fill: judul, pembicara, jam, hari, lokasi, deskripsi (optional)
5. Restart app - schedule appears in rotation

### Update Treasury Data
1. Go to Supabase Dashboard
2. Open **kas_masjid** table
3. Edit the main row: balance, income_month, expense_month, trend_direction
4. Restart app - new balance displays at bottom

## API Endpoints Used
```
GET /rest/v1/kas_masjid
GET /rest/v1/ayat_quran
GET /rest/v1/hadits
GET /rest/v1/pengajian
```

## Error Handling
- ✅ Network errors → Uses mock data automatically
- ✅ Missing data → Shows empty, continues rotation
- ✅ Invalid data → Logged but app continues

## Debug Logging
All Supabase calls print to logcat:
```
✅ Successfully fetched X Quran verses from Supabase
⚠️ Error fetching: Network error
```

## Compilation Status
✅ **Project compiles successfully** - No Kotlin errors
✅ **Ready to build APK** - No breaking issues
✅ **Tested with mock data** - Fallback works perfectly

---
Last Updated: January 25, 2026
