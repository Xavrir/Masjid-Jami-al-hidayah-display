# Draft: Dynamic Running Text from Database

## User Request Summary (FINAL)

1. **Remove `MockData.announcements`** - Delete the hardcoded template announcements entirely
2. **Build running text from existing DB data** - Use already-fetched Quran, Hadith, Pengajian, and Kas data
3. **Keep Friday reminder feature** - Already implemented, prepends "🕌 Shalat Jumat dalam X menit!"
4. **Fix KasDetailOverlay bug** - Line 298 uses `MockData.kasData` instead of fetched `kasData`
5. **Add static mosque reminders** - Basic operational reminders like "Mohon nonaktifkan ponsel saat shalat"

## Confirmed Requirements

### Running Text Content (in priority order)
1. **Friday reminder** - When 1-10 min before Jumat (KEEP AS-IS)
2. **Kas Masjid summary** - e.g., "💰 Saldo Kas: Rp45.250.000 | Pemasukan Bulan Ini: Rp28.500.000"
3. **Static mosque reminders** - Operational reminders (new, defined in code)
4. **Quran verses** - Already fetched, formatted as "QS Surah (N):Ayah - text"
5. **Hadiths** - Already fetched, formatted as "Source: text"
6. **Pengajian schedule** - Already fetched, formatted as "Title oleh Speaker (Day, Time)"

### Error Handling
- On fetch failure: Show empty list (only Friday reminder if applicable)
- No mock data fallback

## Current State Analysis

### Files to Modify

| File | Line | Current | Target |
|------|------|---------|--------|
| `MockData.kt:73-78` | `val announcements` | 4 hardcoded strings | **DELETE ENTIRELY** |
| `MainActivity.kt:199-205` | `effectiveAnnouncements` | Uses `MockData.announcements` | Build from DB data + static reminders |
| `MainActivity.kt:298` | `KasDetailOverlay` | `kasData = MockData.kasData` | `kasData = kasData` (state variable) |

### Already Available State Variables

From `MainActivity.kt`:
- `kasData: KasData` - Fetched from Supabase (line 129)
- `quranVerses: List<String>` - Formatted Quran verses (lines 131-135)
- `hadiths: List<String>` - Formatted hadiths (lines 137-141)
- `pengajian: List<String>` - Formatted pengajian (lines 143-146)
- `fridayReminderAnnouncement: String?` - Friday reminder text (lines 169-197)

### Static Reminders (New)

Define static operational reminders in code:
```kotlin
val staticReminders = listOf(
    "📱 Mohon nonaktifkan atau membisukan ponsel sebelum shalat dimulai",
    "🤲 Mari rapatkan shaf dan luruskan barisan",
    "🧹 Jagalah kebersihan masjid, tempat ibadah kita bersama"
)
```

## Technical Decisions (CONFIRMED)

1. **No new Supabase endpoints** - Use existing fetched data
2. **Remove MockData.announcements** - Delete entirely, no fallback
3. **Build announcements dynamically** - Combine all data sources
4. **Add Kas summary to running text** - Format kasData for display
5. **Include static reminders** - Operational messages in code

## Scope Boundaries

### IN SCOPE
- Remove `MockData.announcements` from MockData.kt
- Define static mosque reminders (in MainActivity or separate constant)
- Build `effectiveAnnouncements` from: fridayReminder + kasInfo + staticReminders + quranVerses + hadiths + pengajian
- Fix KasDetailOverlay to use fetched kasData

### OUT OF SCOPE
- New Supabase tables or endpoints
- Admin interface for announcements
- Dynamic priority/ordering of content types

## Bugs to Fix

1. **KasDetailOverlay bug** (line 298): `kasData = MockData.kasData` → `kasData = kasData`
