# Draft: Masjid Display App - Multi-Bug Fix Plan

## User Request Summary

Fix 5 bugs/features in the Android TV Masjid Display app:
1. **Running text bug**: Text becomes "trippy" - changes mid-animation
2. **Database check**: Verify Supabase database setup and data fetching
3. **Auto-remove overlay**: After adzan/iqamah overlay, auto-dismiss after 1 min
4. **Imsak/Syuruq switch**: Show Syuruq instead of Imsak when NOT Ramadhan
5. **Friday reminder**: Add Jumat prayer reminder 10 minutes before in running text

---

## Requirements Analysis (Updated with Agent Findings)

### Bug #1: Running Text Animation Synchronization

**Problem Confirmed by Explore Agent**:
- `MultiSourceRunningText` rotates text every `8000ms` (8 seconds)
- `EnhancedRunningText` animation duration is `content.length * 300`, clamped to `20000-60000ms`
- **ROOT CAUSE**: Text changes at 8s intervals while animation takes 20-60s
- When text changes mid-animation, `offsetX` remains at intermediate value (e.g., 0.4)
- New `totalScrollDistance` is computed but applied to same `offsetX` = VISUAL JUMP

**Recommended Fix** (Agent-validated):
```kotlin
// In EnhancedRunningText.kt, change these two lines:
val infiniteTransition = remember(content) { rememberInfiniteTransition(label = "marquee") }
val durationMs = remember(content, textWidth) { (content.length * 300).coerceIn(20000, 60000) }
```
This restarts animation from beginning when content changes.

**Files to modify**:
- `/android/app/src/main/java/com/masjiddisplay/ui/components/EnhancedRunningText.kt`
  - Line ~50: Add `content` key to infiniteTransition
  - Line ~46: Add `content` key to durationMs remember

### Bug #2: Database Verification

**Issues Found by Explore Agent**:
1. **TYPE MISMATCH** (HIGH PRIORITY):
   - Database uses UUID for IDs (gen_random_uuid())
   - Kotlin models expect `Int` for `QuranVerseRemote.id` and `HadithRemote.id`
   - This causes JSON parsing to FAIL silently, falling back to mock data
   - **Fix**: Change `id: Int` to `id: String` in remote models

2. **SECURITY ISSUES** (MEDIUM PRIORITY):
   - API key hardcoded in SupabaseConfig.kt
   - SSL validation disabled unconditionally (not just debug)
   - **Fix**: Move key to BuildConfig, gate unsafe client behind DEBUG flag

3. **MAPPING BUG**:
   - `toLocal()` sets `translation = this.transliteration` (likely wrong)
   - **Fix**: Verify correct field mapping

**Files to modify**:
- `SupabaseApiService.kt` - Change id types from Int to String
- `SupabaseRepository.kt` - Gate unsafe OkHttp client behind BuildConfig.DEBUG

### Bug #3: Overlay Auto-Dismiss

**Status: ALREADY IMPLEMENTED**
- Adzan: 60 seconds auto-dismiss (line 189)
- Iqamah: 60 seconds auto-dismiss (line 204)
- Friday reminder: 10 seconds auto-dismiss (line 232)

**User confirmed this matches request.** Marking as SKIP unless issues observed.

### Bug #4: Imsak/Syuruq Conditional Display

**Current Behavior** (Agent-confirmed):
- `mainPrayers` filters: `it.name.lowercase() !in listOf("shuruq", "syuruq", "sunrise")`
- Syuruq displayed in top-right corner with `SYURUQ $shuruqTime`
- Imsak included in main prayer row from PrayerTimeCalculator

**Required Changes** (Agent-provided):
1. Add `var imsakTime by remember { mutableStateOf("--:--") }` 
2. Compute: `imsakTime = PrayerTimeCalculator.calculateImsakTimeForJakarta(today)`
3. Modify `mainPrayers` filter to conditionally exclude "imsak" when Ramadhan:
```kotlin
val mainPrayers = remember(prayers, currentTime) {
    val excluded = mutableListOf("shuruq", "syuruq", "sunrise")
    if (isRamadan(currentTime)) {
        excluded.add("imsak")
    }
    prayers.filter { it.name.lowercase() !in excluded }
}
```
4. Update top-right display:
```kotlin
val showImsakTop = isRamadan(currentTime)
val topLabel = if (showImsakTop) "IMSAK $imsakTime" else "SYURUQ $shuruqTime"
```

**NOTE**: `isRamadan()` is simplified/inaccurate. Consider improving with proper Hijri calendar.

**Files to modify**:
- `MainDashboard.kt` - All changes above

### Bug #5: Friday Reminder in Running Text

**Current Friday Logic** (Agent-confirmed):
- Trigger: 1 minute before Dzuhur (`dzuhurMinute - 1`)
- Shows overlay for 10 seconds
- Running text does NOT include Friday reminder

**Required Changes** (Agent-provided):
1. Change timing: `dzuhurMinute - 10` instead of `- 1`
2. Make announcements dynamic:
```kotlin
var runningAnnouncements = remember { mutableStateListOf<String>().apply { addAll(MockData.announcements) } }
```
3. Pass to MainDashboard: `announcements = runningAnnouncements`
4. In Friday trigger block, add to ticker:
```kotlin
runningAnnouncements.add(0, "Pengingat Jumat: 10 menit menuju Shalat Jumat")
// Remove after 20s
LaunchedEffect(lastFridayReminderKey) { 
    delay(20_000)
    runningAnnouncements.remove("Pengingat Jumat: 10 menit menuju Shalat Jumat") 
}
```

**Files to modify**:
- `MainActivity.kt` - Timing change, dynamic announcements, ticker injection

---

## Decisions Confirmed

1. **Bug #1**: Restart animation on content change (simple 2-line fix)
2. **Bug #2**: Fix type mismatch so Supabase data works (quick fix)
3. **Bug #3**: SKIP - already working as requested
4. **Bug #4**: Implement conditional swap based on Ramadhan
5. **Bug #5**: Both overlay AND running text, change to 10 minutes

---

## Test Strategy

**Infrastructure**: No automated test framework in project
**Verification approach**: Manual testing via Android TV emulator
- Test panel already exists (MENU key) for testing overlays
- Use test buttons for adzan/iqamah/Friday reminder

---

## Scope Boundaries

**IN SCOPE**:
- Fix running text animation synchronization
- Fix Supabase type mismatch (UUID vs Int)
- Implement Imsak/Syuruq conditional display
- Add Friday reminder to running text + change timing to 10 min

**OUT OF SCOPE**:
- Full security hardening (API key migration, SSL fixes)
- Improving isRamadan() accuracy (would require Hijri calendar library)
- Adding automated tests
- Overlay auto-dismiss (already working)
