# Dynamic Running Text from Database

## TL;DR

> **Quick Summary**: Remove hardcoded `MockData.announcements`, replace with static mosque reminders, and fix KasDetailOverlay bug to use fetched database data instead of mock data.
> 
> **Deliverables**:
> - Running text displays real Supabase data (Kas, Quran, Hadith, Pengajian) + static reminders
> - KasDetailOverlay shows actual fetched kasData
> - MockData.announcements removed
> 
> **Estimated Effort**: Quick
> **Parallel Execution**: NO - sequential (3 small edits in 2 files)
> **Critical Path**: Task 1 → Task 2 → Task 3

---

## Context

### Original Request
Replace hardcoded template announcements with real data from Supabase. Keep Friday reminder feature. Fix KasDetailOverlay bug where it uses MockData instead of fetched data.

### Interview Summary
**Key Discussions**:
- No `pengumuman` table exists in Supabase - use existing data sources instead
- Running text should show: Friday reminder + Kas summary + Static reminders + Quran + Hadith + Pengajian
- Remove MockData.announcements entirely (no fallback)
- Error handling: empty list on fetch failure

**Research Findings**:
- `MultiSourceRunningText` component already handles multi-source data with emoji prefixes
- `MainDashboard` generates `kasItems` from `kasData` - already working correctly
- Only `announcements` parameter needs to change (from MockData to static reminders)
- KasDetailOverlay bug is on line 298: uses `MockData.kasData` instead of state variable `kasData`

---

## Work Objectives

### Core Objective
Replace mock announcement data with static mosque reminders while ensuring all running text content comes from real database sources.

### Concrete Deliverables
- `MockData.kt`: Remove `val announcements` (lines 73-78)
- `MainActivity.kt`: Update `effectiveAnnouncements` to use static reminders
- `MainActivity.kt`: Fix `KasDetailOverlay` to use fetched `kasData`

### Definition of Done
- [ ] Running text shows: Friday reminder (when applicable) + Static reminders + Kas summary + Quran + Hadith + Pengajian
- [ ] KasDetailOverlay displays fetched database values, not mock values
- [ ] No references to `MockData.announcements` remain in codebase
- [ ] App builds successfully: `./gradlew assembleDebug`

### Must Have
- Friday reminder feature preserved (prepends "🕌 Shalat Jumat dalam X menit!" on Fridays)
- Static mosque reminders (operational messages)
- KasDetailOverlay uses fetched kasData

### Must NOT Have (Guardrails)
- NO new Supabase endpoints or API calls
- NO changes to MultiSourceRunningText component
- NO changes to MainDashboard component (except if announcements param changes)
- NO complex configuration or priority systems
- NO modification to Friday reminder logic

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO (Android TV app - visual verification only)
- **User wants tests**: Manual-only
- **Framework**: None (visual verification via emulator)

### Manual Verification Procedure

**For Running Text changes** (using Android emulator):
```
1. Build APK: ./gradlew assembleDebug
2. Install on emulator: adb install -r app/build/outputs/apk/debug/app-debug.apk
3. Launch app on Android TV emulator
4. Observe running text ticker at bottom of screen
5. Verify content rotates through: Static reminders → Kas summary → Quran verses → Hadiths → Pengajian
6. Take screenshot: adb exec-out screencap -p > running-text-verification.png
```

**For KasDetailOverlay** (using Android emulator):
```
1. With app running, press INFO/MENU button on remote
2. Or trigger kas overlay programmatically
3. Verify displayed balance matches Supabase data (not Rp45.250.000 mock value)
4. Take screenshot for evidence
```

---

## Execution Strategy

### Sequential Execution (Simple Task)

This is a small, tightly-coupled change. Execute sequentially:

```
Task 1: Remove MockData.announcements
   ↓
Task 2: Update effectiveAnnouncements with static reminders
   ↓
Task 3: Fix KasDetailOverlay to use fetched kasData
   ↓
Build & Verify
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2 | None |
| 2 | 1 | 3 | None |
| 3 | 2 | Build | None |

---

## TODOs

- [ ] 1. Remove MockData.announcements

  **What to do**:
  - Delete `val announcements` list from MockData.kt (lines 73-78)
  - This is the hardcoded template text that needs removal

  **Must NOT do**:
  - Remove other MockData properties (masjidConfig, kasData, kasTransactions)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single file, simple deletion, < 5 lines to remove
  - **Skills**: None needed
    - Simple edit operation, no special domain knowledge required

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential
  - **Blocks**: Task 2 (need to verify compilation after removal)
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - None needed - simple deletion

  **Target File**:
  - `android/app/src/main/java/com/masjiddisplay/data/MockData.kt:73-78` - Lines to delete

  **Current Code (DELETE THIS)**:
  ```kotlin
  val announcements = listOf(
      "Mohon menonaktifkan atau membisukan ponsel sebelum salat dimulai.",
      "Kajian rutin setiap Ahad ba'da Maghrib bersama Ustadz Ahmad.",
      "Infaq pembangunan gedung baru telah mencapai 75% dari target.",
      "Pendaftaran TPA dibuka untuk tahun ajaran baru, hubungi panitia di ruang sekretariat."
  )
  ```

  **Acceptance Criteria**:

  **Automated Verification**:
  ```bash
  # Agent runs:
  grep -n "val announcements" android/app/src/main/java/com/masjiddisplay/data/MockData.kt
  # Assert: No output (announcements removed)
  
  grep -rn "MockData.announcements" android/app/src/main/java/
  # Assert: Only MainActivity.kt should reference it (will be fixed in Task 2)
  ```

  **Commit**: YES
  - Message: `refactor(data): remove hardcoded MockData.announcements`
  - Files: `android/app/src/main/java/com/masjiddisplay/data/MockData.kt`
  - Pre-commit: `grep -c "val announcements" android/app/src/main/java/com/masjiddisplay/data/MockData.kt` returns 0

---

- [ ] 2. Update effectiveAnnouncements to use static reminders

  **What to do**:
  - Define static mosque reminders as a list constant
  - Update `effectiveAnnouncements` (line 199-205) to use static reminders instead of `MockData.announcements`
  - Preserve Friday reminder prepend logic
  - Update `remember` dependencies to include all relevant state variables

  **Must NOT do**:
  - Change Friday reminder logic (lines 169-197)
  - Modify how MainDashboard or MultiSourceRunningText work
  - Add new Supabase fetches

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single file edit, clear pattern to follow
  - **Skills**: None needed
    - Straightforward Kotlin list operations

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential
  - **Blocks**: Task 3
  - **Blocked By**: Task 1 (MockData.announcements must be removed first)

  **References**:

  **Pattern References**:
  - `android/app/src/main/java/com/masjiddisplay/MainActivity.kt:199-205` - Current effectiveAnnouncements (modify this)
  - `android/app/src/main/java/com/masjiddisplay/MainActivity.kt:169-197` - Friday reminder logic (DO NOT MODIFY, just understand)

  **API/Type References**:
  - None - simple List<String> operations

  **Current Code (MODIFY THIS)**:
  ```kotlin
  val effectiveAnnouncements = remember(fridayReminderAnnouncement) {
      if (fridayReminderAnnouncement != null) {
          listOf(fridayReminderAnnouncement!!) + MockData.announcements
      } else {
          MockData.announcements
      }
  }
  ```

  **Target Implementation**:
  ```kotlin
  // Define static mosque reminders
  val staticReminders = listOf(
      "Mohon nonaktifkan atau membisukan ponsel sebelum shalat",
      "Mari rapatkan shaf dan luruskan barisan saat shalat berjamaah",
      "Jagalah kebersihan masjid, tempat ibadah kita bersama"
  )
  
  val effectiveAnnouncements = remember(fridayReminderAnnouncement) {
      if (fridayReminderAnnouncement != null) {
          listOf(fridayReminderAnnouncement!!) + staticReminders
      } else {
          staticReminders
      }
  }
  ```

  **WHY Each Reference Matters**:
  - Lines 199-205: The exact code to modify
  - Lines 169-197: Context for Friday reminder - DO NOT change, just maintain compatibility

  **Acceptance Criteria**:

  **Automated Verification**:
  ```bash
  # Agent runs:
  grep -n "MockData.announcements" android/app/src/main/java/com/masjiddisplay/MainActivity.kt
  # Assert: No output (reference removed)
  
  grep -n "staticReminders" android/app/src/main/java/com/masjiddisplay/MainActivity.kt
  # Assert: Returns line numbers where staticReminders is defined and used
  
  # Build verification
  cd android && ./gradlew compileDebugKotlin
  # Assert: BUILD SUCCESSFUL
  ```

  **Commit**: YES
  - Message: `refactor(ui): replace MockData.announcements with static mosque reminders`
  - Files: `android/app/src/main/java/com/masjiddisplay/MainActivity.kt`
  - Pre-commit: `./gradlew compileDebugKotlin` succeeds

---

- [ ] 3. Fix KasDetailOverlay to use fetched kasData

  **What to do**:
  - Change `KasDetailOverlay(kasData = MockData.kasData, ...)` to `KasDetailOverlay(kasData = kasData, ...)`
  - This ensures the overlay displays real database values, not mock values

  **Must NOT do**:
  - Modify KasDetailOverlay component itself
  - Change how kasData is fetched (line 129)
  - Affect any other component props

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single line change, trivial fix
  - **Skills**: None needed
    - One-liner bug fix

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential
  - **Blocks**: Build & Verify
  - **Blocked By**: Task 2 (ensure compilation works)

  **References**:

  **Bug Location**:
  - `android/app/src/main/java/com/masjiddisplay/MainActivity.kt:296-300` - KasDetailOverlay call

  **Current Code (BUG)**:
  ```kotlin
  KasDetailOverlay(
      visible = kasOverlayVisible,
      kasData = MockData.kasData,  // <-- BUG: Should use fetched kasData
      onClose = { kasOverlayVisible = false }
  )
  ```

  **Target Implementation**:
  ```kotlin
  KasDetailOverlay(
      visible = kasOverlayVisible,
      kasData = kasData,  // <-- FIX: Use state variable
      onClose = { kasOverlayVisible = false }
  )
  ```

  **WHY This Fix Matters**:
  - `kasData` state variable is fetched from Supabase at line 129
  - Currently overlay ignores fetched data and shows hardcoded mock values
  - User will see stale/wrong Kas information in the overlay

  **Acceptance Criteria**:

  **Automated Verification**:
  ```bash
  # Agent runs:
  grep -n "MockData.kasData" android/app/src/main/java/com/masjiddisplay/MainActivity.kt
  # Assert: No output (all MockData.kasData references removed)
  
  grep -A3 "KasDetailOverlay(" android/app/src/main/java/com/masjiddisplay/MainActivity.kt | grep "kasData = kasData"
  # Assert: Returns the fixed line
  
  # Final build
  cd android && ./gradlew assembleDebug
  # Assert: BUILD SUCCESSFUL
  ```

  **Evidence to Capture**:
  - [ ] Screenshot of KasDetailOverlay showing non-mock values (if DB has different data)
  - [ ] Build output showing success

  **Commit**: YES
  - Message: `fix(ui): use fetched kasData in KasDetailOverlay instead of MockData`
  - Files: `android/app/src/main/java/com/masjiddisplay/MainActivity.kt`
  - Pre-commit: `./gradlew assembleDebug` succeeds

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `refactor(data): remove hardcoded MockData.announcements` | MockData.kt | grep check |
| 2 | `refactor(ui): replace MockData.announcements with static mosque reminders` | MainActivity.kt | compileDebugKotlin |
| 3 | `fix(ui): use fetched kasData in KasDetailOverlay instead of MockData` | MainActivity.kt | assembleDebug |

---

## Success Criteria

### Verification Commands
```bash
# No more MockData.announcements references
grep -rn "MockData.announcements" android/app/src/main/java/
# Expected: No output

# No more MockData.kasData in MainActivity
grep -n "MockData.kasData" android/app/src/main/java/com/masjiddisplay/MainActivity.kt
# Expected: No output

# Build succeeds
cd android && ./gradlew assembleDebug
# Expected: BUILD SUCCESSFUL
```

### Final Checklist
- [ ] `MockData.announcements` deleted from MockData.kt
- [ ] `staticReminders` defined in MainActivity.kt
- [ ] `effectiveAnnouncements` uses `staticReminders` instead of `MockData.announcements`
- [ ] Friday reminder logic unchanged and working
- [ ] `KasDetailOverlay` uses fetched `kasData` state variable
- [ ] App builds successfully
- [ ] Running text displays real database content (visual verification)
