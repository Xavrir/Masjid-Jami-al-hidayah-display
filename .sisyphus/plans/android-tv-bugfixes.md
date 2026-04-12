# Android TV Mosque Display - 3 Bug Fixes

## TL;DR

> **Quick Summary**: Fix 3 visual bugs in Android TV mosque display app (Pengajian not showing, Iqamah text cutoff, TikTok handle clipped) using ULW (ultrawork loop) verification with emulator screenshots.
> 
> **Deliverables**:
> - Bug 1 fixed: Pengajian entries visible in running text ticker
> - Bug 2 fixed: Iqamah popup text fully visible without clipping
> - Bug 3 fixed: All social media handles (including TikTok) properly displayed
> - All changes committed and pushed to main
> 
> **Estimated Effort**: Medium (3-4 hours with verification loops)
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: Environment Setup → Bug 2 (parallel with Bug 1+3 diagnosis) → Build → Verify → Refix Loop

---

## Context

### Original Request
Fix 3 bugs in Android TV mosque display app:
1. Pengajian (Islamic study info) not showing on TV display
2. Iqamah popup text half-visible/cut off
3. TikTok handle cut off in running text

Constraints: ULW loop (fix → build → install → verify on emulator → refix), visual verification via screenshots, commit and push when all 3 fixed.

### Interview Summary
**Key Discussions**:
- User provided detailed root cause analysis for each bug
- All relevant file paths and line numbers identified
- Test panel exists (MENU/INFO key) for triggering prayer overlays
- Build command: `./gradlew assembleDebug --no-daemon`
- Install command: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

**Research Findings**:
- All 4 source files verified: MainActivity.kt, SupabaseRepository.kt, PrayerAlertOverlay.kt, EnhancedRunningText.kt
- PengajianRemote model has nullable `judul` and `pembicara` fields
- socialMediaLinks is ONE list item with 175-char concatenated string
- Iqamah card uses 0.65f width with 48dp horizontal padding - tight for 44sp text

### Metis Review
**Identified Gaps** (addressed):
- Added Task 0: Environment Setup (emulator + ADB connectivity)
- Added Task 0.5: Baseline build verification before changes
- Enhanced acceptance criteria with specific adb commands
- Added guardrails to prevent scope creep
- Clarified exact file paths under `android/app/src/main/java/com/masjiddisplay/`

---

## Work Objectives

### Core Objective
Fix 3 visual bugs in Android TV mosque display app and verify each fix via emulator screenshots before pushing to main.

### Concrete Deliverables
- `android/app/src/main/java/com/masjiddisplay/MainActivity.kt` - Pengajian debug logging + relaxed filter + split social media list
- `android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt` - Layout fixes for Iqamah text visibility

### Definition of Done
- [ ] Pengajian entries appear in running text ticker (screenshot evidence)
- [ ] Iqamah overlay shows "MOHON BERDIRI UNTUK SHALAT [PRAYER]" fully visible (screenshot evidence)
- [ ] All social media handles scroll independently (each gets full visibility cycle)
- [ ] All 3 fixes committed and pushed to main branch

### Must Have
- Visual verification via emulator for each bug
- ULW loop compliance (fix → build → install → verify → refix if needed)
- All changes committed and pushed to main

### Must NOT Have (Guardrails)
- Do NOT modify SupabaseRepository.kt (Bug 1 fix should be in MainActivity.kt only)
- Do NOT change overlay dismiss/countdown logic in PrayerAlertOverlay.kt
- Do NOT modify EnhancedRunningText.kt animation behavior
- Do NOT add persistent logging (debug only, remove after fix confirmed)
- Do NOT refactor unrelated code or update dependencies
- Do NOT change other overlay types (ADHAN, PRE_ADHAN, FRIDAY_REMINDER)
- Do NOT change rotation timing between content types in running text

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO (no unit tests for this UI fix)
- **User wants tests**: Manual-only (visual verification via emulator)
- **Framework**: Playwright/browser skill for automated screenshots

### Automated Verification (ZERO User Intervention)

Each TODO includes EXECUTABLE verification via emulator:

**For Android TV App** (using adb + screenshots):
```bash
# Start emulator if not running
emulator @AndroidTV_API34 -no-snapshot &

# Wait for device
adb wait-for-device

# Build and install
cd android && ./gradlew assembleDebug --no-daemon
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.masjiddisplay/.MainActivity

# Take screenshot
adb exec-out screencap -p > .sisyphus/evidence/screenshot.png
```

**Evidence Requirements** (Agent-Executable):
- Screenshots saved to `.sisyphus/evidence/` directory
- ADB logcat output for debug logs
- Each verification step documented with timestamp

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 0 (Setup - Must Complete First):
└── Task 0: Environment Setup (emulator + ADB + baseline build)

Wave 1 (After Setup):
├── Task 1: Bug 2 - Iqamah Popup Fix (PrayerAlertOverlay.kt) - ISOLATED FILE
└── Task 2: Bug 1 + Bug 3 Combined (MainActivity.kt) - SAME FILE, DIFFERENT SECTIONS

Wave 2 (After Wave 1):
└── Task 3: Build, Install, and Verify All Bugs

Wave 3 (If Needed):
└── Task 4: Refix Loop for Any Failing Bugs

Wave 4 (Final):
└── Task 5: Commit and Push to Main

Critical Path: Task 0 → Task 1/2 (parallel) → Task 3 → Task 5
Parallel Speedup: ~30% faster than sequential (Bug 2 runs parallel with Bug 1+3)
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 0 (Setup) | None | 1, 2 | None |
| 1 (Bug 2) | 0 | 3 | 2 |
| 2 (Bug 1+3) | 0 | 3 | 1 |
| 3 (Verify) | 1, 2 | 4, 5 | None |
| 4 (Refix) | 3 | 5 | None (conditional) |
| 5 (Commit) | 3 or 4 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Agents |
|------|-------|-------------------|
| 0 | Setup | delegate_task(category="quick", load_skills=["android-emulator-skill"]) |
| 1 | 1, 2 | Two parallel: category="quick" for each |
| 2 | 3 | delegate_task(category="quick", load_skills=["android-emulator-skill"]) |
| 3 | 4 | If needed: category="quick" |
| 4 | 5 | delegate_task(category="quick", load_skills=["git-master"]) |

---

## TODOs

- [ ] 0. Environment Setup and Baseline Build

  **What to do**:
  - Check if emulator AndroidTV_API34 exists: `emulator -list-avds`
  - Start emulator if not running: `emulator @AndroidTV_API34 -no-snapshot &`
  - Wait for device: `adb wait-for-device`
  - Verify ADB connectivity: `adb devices` (should show device)
  - Run baseline build to ensure project compiles: `cd android && ./gradlew assembleDebug --no-daemon`
  - Install baseline APK: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
  - Launch app and take baseline screenshot: `adb shell am start -n com.masjiddisplay/.MainActivity && sleep 5 && adb exec-out screencap -p > .sisyphus/evidence/baseline.png`

  **Must NOT do**:
  - Do NOT modify any source files in this task
  - Do NOT skip build verification

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Environment setup is straightforward shell commands
  - **Skills**: [`android-emulator-skill`]
    - `android-emulator-skill`: Emulator lifecycle management and ADB automation

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential (must complete first)
  - **Blocks**: Tasks 1, 2
  - **Blocked By**: None (start immediately)

  **References**:
  - `android/app/build.gradle.kts` - Build configuration and dependencies
  - User context: Emulator is "AndroidTV_API34"
  - User context: Build command is `./gradlew assembleDebug --no-daemon`
  - User context: Install command is `adb install -r app/build/outputs/apk/debug/app-debug.apk`

  **Acceptance Criteria**:

  ```bash
  # Agent runs these commands and captures output:
  
  # 1. Verify emulator exists
  emulator -list-avds | grep -q "AndroidTV_API34"
  # Assert: Exit code 0 (emulator exists)
  
  # 2. Verify ADB shows device
  adb devices | grep -v "List" | grep -q "device"
  # Assert: At least one device listed
  
  # 3. Verify build succeeds
  cd android && ./gradlew assembleDebug --no-daemon 2>&1 | tail -5
  # Assert: Output contains "BUILD SUCCESSFUL"
  
  # 4. Verify APK exists
  ls -la android/app/build/outputs/apk/debug/app-debug.apk
  # Assert: File exists
  
  # 5. Verify app launches
  adb shell am start -n com.masjiddisplay/.MainActivity
  # Assert: Exit code 0
  
  # 6. Capture baseline screenshot
  mkdir -p .sisyphus/evidence
  sleep 5
  adb exec-out screencap -p > .sisyphus/evidence/baseline.png
  ls -la .sisyphus/evidence/baseline.png
  # Assert: File exists and size > 10KB
  ```

  **Evidence to Capture:**
  - [ ] Terminal output from `adb devices`
  - [ ] Terminal output from build command showing "BUILD SUCCESSFUL"
  - [ ] Baseline screenshot: `.sisyphus/evidence/baseline.png`

  **Commit**: NO (no source changes)

---

- [ ] 1. Bug 2: Fix Iqamah Popup Text Cutoff

  **What to do**:
  - Open `android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt`
  - Line 112: Change `fillMaxWidth(0.65f)` to `fillMaxWidth(0.75f)` (increase card width)
  - Line 114: Remove `graphicsLayer { translationY = 40f }` (remove downward offset)
  - Line 143: Change `padding(horizontal = 48.dp, vertical = 36.dp)` to `padding(horizontal = 32.dp, vertical = 28.dp)` (reduce padding)
  - Line 202: Change `fontSize = 44.sp` to `fontSize = 38.sp` (reduce subtitle text size)
  - Save file

  **Must NOT do**:
  - Do NOT change countdown ring logic (lines 251-299)
  - Do NOT change time display (lines 209-249)
  - Do NOT modify ADHAN, PRE_ADHAN, or FRIDAY_REMINDER overlay behavior
  - Do NOT change dismiss/canDismiss logic (lines 47-60)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple parameter changes in single file
  - **Skills**: []
    - No special skills needed - straightforward Compose layout edits

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Task 3 (Verification)
  - **Blocked By**: Task 0 (Setup)

  **References**:

  **Pattern References**:
  - `PrayerAlertOverlay.kt:110-143` - Card container with current layout values
  - `PrayerAlertOverlay.kt:200-207` - Subtitle text with current font size

  **Layout Context**:
  - Current: 0.65f width = 1248px on 1920px TV, minus 96dp padding = ~1052px content
  - Target: 0.75f width = 1440px on 1920px TV, minus 64dp padding = ~1376px content
  - Text "MOHON BERDIRI UNTUK SHALAT MAGHRIB" at 38sp fits comfortably in 1376px

  **WHY Each Change**:
  - `fillMaxWidth(0.75f)`: More horizontal space for text
  - Remove `translationY = 40f`: Prevents card from being pushed down off-center
  - Reduce padding: More content area within the card
  - Reduce fontSize to 38sp: Ensures text fits even with longest prayer name

  **Acceptance Criteria**:

  ```bash
  # Agent executes via adb after build + install:
  
  # 1. Open test panel
  adb shell input keyevent KEYCODE_MENU
  sleep 2
  
  # 2. Trigger IQAMAH overlay for Maghrib (longest prayer name)
  # Test panel shows buttons - tap IQAMAH for Maghrib (4th row)
  # Using coordinates based on test panel layout
  adb shell input tap 700 500  # Approximate IQAMAH button location
  sleep 1
  
  # 3. Take screenshot
  adb exec-out screencap -p > .sisyphus/evidence/bug2-iqamah-maghrib.png
  
  # 4. Visual verification requirements:
  # - "IQAMAH" title visible at top
  # - "MOHON BERDIRI UNTUK SHALAT MAGHRIB" fully visible, not clipped
  # - Countdown ring visible
  # - Card centered vertically (not pushed down)
  
  # 5. Dismiss overlay
  adb shell input tap 960 540  # Tap to dismiss
  sleep 1
  
  # 6. Close test panel
  adb shell input keyevent KEYCODE_MENU
  ```

  **Evidence to Capture:**
  - [ ] Screenshot: `.sisyphus/evidence/bug2-iqamah-maghrib.png`
  - [ ] Visual confirmation: Full subtitle text visible without horizontal clipping
  - [ ] Visual confirmation: Card appears vertically centered

  **Commit**: YES (groups with Task 2)
  - Message: `fix(ui): resolve iqamah popup text cutoff on TV display`
  - Files: `android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt`
  - Pre-commit: Build must succeed

---

- [ ] 2. Bug 1 + Bug 3: Fix Pengajian Display and Social Media Handles

  **What to do**:
  
  **Bug 1 - Pengajian not showing (lines 164-167)**:
  - Add diagnostic logging at line 164 (before filter):
    ```kotlin
    val fetchedPengajian = SupabaseRepository.getPengajian()
    println("📦 Raw pengajian count: ${fetchedPengajian.size}")
    fetchedPengajian.forEach { println("  - judul: ${it.judul}, pembicara: ${it.pembicara}") }
    ```
  - If raw count > 0 but filtered = 0: Relax filter at line 166 to require only `judul != null`:
    ```kotlin
    pengajian = fetchedPengajian
        .filter { it.judul != null }  // Changed: only require judul
        .map { "${it.judul} oleh ${it.pembicara ?: "Ustadz"} (${it.hari ?: "-"}, ${it.jam ?: "-"})" }
    ```
  - After verification works: Remove diagnostic println statements

  **Bug 3 - TikTok handle cut off (lines 236-240)**:
  - Change socialMediaLinks from single concatenated string to separate items:
    ```kotlin
    val socialMediaLinks = remember {
        listOf(
            "Instagram @kurmaalhidayah",
            "Instagram @masjidalhidayah.tanahmerdeka",
            "YouTube Masjidalhidayah.tanahmerdeka",
            "TikTok @kurmaalhidayahofficial"
        )
    }
    ```
  - Each handle will now scroll independently in MultiSourceRunningText rotation

  **Must NOT do**:
  - Do NOT modify SupabaseRepository.kt
  - Do NOT modify EnhancedRunningText.kt or MultiSourceRunningText composable
  - Do NOT change rotation timing or animation speed
  - Do NOT leave debug println statements in final commit

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Small, targeted changes in single file
  - **Skills**: []
    - No special skills needed - straightforward Kotlin edits

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Task 3 (Verification)
  - **Blocked By**: Task 0 (Setup)

  **References**:

  **Pattern References**:
  - `MainActivity.kt:164-167` - Current pengajian fetch and filter logic
  - `MainActivity.kt:236-240` - Current socialMediaLinks definition
  - `MainActivity.kt:152-162` - Similar pattern for quranVerses and hadiths (follow this style)

  **Data Model References**:
  - `SupabaseApiService.kt:70-78` - PengajianRemote model with nullable judul/pembicara

  **Component References**:
  - `EnhancedRunningText.kt:121-175` - MultiSourceRunningText that receives these lists
  - Line 137: socialMedia items get "📱 Ikuti Kami:" prefix

  **WHY Each Change**:
  - Debug logging: Diagnose whether issue is API failure or filter being too strict
  - Relax filter: Allow pengajian with just judul (pembicara can be "Ustadz" default)
  - Split socialMediaLinks: Each handle gets its own scroll cycle instead of one long concatenated string

  **Acceptance Criteria**:

  ```bash
  # Agent executes via adb after build + install:
  
  # 1. Launch app and wait for data load
  adb shell am force-stop com.masjiddisplay
  adb shell am start -n com.masjiddisplay/.MainActivity
  sleep 10  # Wait for Supabase data fetch
  
  # 2. Check logcat for pengajian debug output
  adb logcat -d | grep -i "pengajian"
  # Assert: Shows "📦 Raw pengajian count: N" where N >= 0
  # If N > 0: Should see individual entries logged
  
  # 3. Take screenshot of main dashboard with running text
  adb exec-out screencap -p > .sisyphus/evidence/bug1-3-dashboard.png
  
  # 4. Wait for ticker rotation and capture multiple frames
  for i in 1 2 3 4 5; do
    sleep 8
    adb exec-out screencap -p > .sisyphus/evidence/bug3-ticker-$i.png
  done
  
  # Verification requirements:
  # Bug 1: Running text ticker shows "🎓 Pengajian:" entries (if data exists in Supabase)
  # Bug 3: Screenshots show different social media handles rotating:
  #   - "📱 Ikuti Kami: Instagram @kurmaalhidayah"
  #   - "📱 Ikuti Kami: Instagram @masjidalhidayah.tanahmerdeka"
  #   - "📱 Ikuti Kami: YouTube Masjidalhidayah.tanahmerdeka"
  #   - "📱 Ikuti Kami: TikTok @kurmaalhidayahofficial"
  ```

  **Evidence to Capture:**
  - [ ] Logcat output showing pengajian count
  - [ ] Screenshot: `.sisyphus/evidence/bug1-3-dashboard.png`
  - [ ] Screenshots: `.sisyphus/evidence/bug3-ticker-{1-5}.png` (showing rotation)
  - [ ] Visual confirmation: TikTok handle fully visible in one of the screenshots

  **Commit**: YES (groups with Task 1)
  - Message: `fix(data): display pengajian entries and rotate social media handles`
  - Files: `android/app/src/main/java/com/masjiddisplay/MainActivity.kt`
  - Pre-commit: Build must succeed, remove debug println statements

---

- [ ] 3. Build, Install, and Verify All Bugs

  **What to do**:
  - Build: `cd android && ./gradlew assembleDebug --no-daemon`
  - Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
  - Launch app: `adb shell am start -n com.masjiddisplay/.MainActivity`
  - Verify Bug 1: Check logcat and ticker for pengajian entries
  - Verify Bug 2: Trigger IQAMAH overlay via test panel, screenshot
  - Verify Bug 3: Wait for social media rotation, capture multiple screenshots
  - Document any failures for refix

  **Must NOT do**:
  - Do NOT modify source files in this task
  - Do NOT skip verification steps

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Verification commands are straightforward
  - **Skills**: [`android-emulator-skill`]
    - `android-emulator-skill`: Screenshot capture and ADB automation

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential (Wave 2)
  - **Blocks**: Task 4 (Refix) or Task 5 (Commit)
  - **Blocked By**: Tasks 1 and 2

  **References**:
  - Task 1 acceptance criteria - Bug 2 verification steps
  - Task 2 acceptance criteria - Bug 1 and Bug 3 verification steps

  **Acceptance Criteria**:

  ```bash
  # All three bugs verified:
  
  # Bug 1 PASS: Logcat shows "📦 Raw pengajian count: N" where N >= 0
  #            If N > 0, ticker shows "🎓 Pengajian:" entries
  #            If N = 0, this is a Supabase data issue (acceptable, not code bug)
  
  # Bug 2 PASS: Screenshot shows "MOHON BERDIRI UNTUK SHALAT MAGHRIB" fully visible
  #            Card is vertically centered
  #            No horizontal text clipping
  
  # Bug 3 PASS: Multiple screenshots show different social handles rotating:
  #            At least one screenshot shows "@kurmaalhidayahofficial" (TikTok)
  ```

  **Evidence to Capture:**
  - [ ] Build output showing "BUILD SUCCESSFUL"
  - [ ] All screenshots from Task 1 and Task 2 acceptance criteria
  - [ ] Summary: PASS/FAIL status for each bug

  **Commit**: NO (verification only)

---

- [ ] 4. Refix Loop (Conditional - Only if Task 3 Has Failures)

  **What to do**:
  - Review failed bug(s) from Task 3
  - Analyze screenshots/logs to identify remaining issue
  - Apply targeted fix
  - Rebuild and re-verify
  - Repeat until all 3 bugs pass

  **Must NOT do**:
  - Do NOT change code that is already working
  - Do NOT introduce scope creep fixes

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Targeted fixes based on verification feedback
  - **Skills**: [`android-emulator-skill`]
    - For re-verification after each fix attempt

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential (conditional)
  - **Blocks**: Task 5 (Commit)
  - **Blocked By**: Task 3

  **References**:
  - Task 3 verification results (failures)
  - Original bug descriptions and root cause analysis

  **Acceptance Criteria**:

  ```bash
  # Loop exits when:
  # - Bug 1: PASS (pengajian displayed or confirmed no data in Supabase)
  # - Bug 2: PASS (iqamah text fully visible)
  # - Bug 3: PASS (all social handles rotate independently)
  ```

  **Evidence to Capture:**
  - [ ] Before/after screenshots for any refix
  - [ ] Final passing screenshots for all 3 bugs

  **Commit**: NO (wait for Task 5)

---

- [ ] 5. Commit and Push to Main

  **What to do**:
  - Remove any remaining debug println statements from MainActivity.kt
  - Stage all changed files:
    - `android/app/src/main/java/com/masjiddisplay/MainActivity.kt`
    - `android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt`
  - Create commit with message describing all 3 fixes
  - Push to main branch

  **Must NOT do**:
  - Do NOT push if any bug verification is still failing
  - Do NOT include debug logging in final commit
  - Do NOT commit unrelated files

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard git operations
  - **Skills**: [`git-master`]
    - `git-master`: Atomic commits and proper commit message formatting

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Final (Wave 4)
  - **Blocks**: None (final task)
  - **Blocked By**: Task 3 (all PASS) or Task 4 (if refixes needed)

  **References**:
  - Task 1 and Task 2 file lists
  - Evidence screenshots confirming all fixes work

  **Acceptance Criteria**:

  ```bash
  # 1. Verify no debug logging remains
  grep -r "📦 Raw pengajian" android/app/src/main/java/
  # Assert: No matches (debug logging removed)
  
  # 2. Stage and commit
  git add android/app/src/main/java/com/masjiddisplay/MainActivity.kt
  git add android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt
  git status
  # Assert: Only expected files staged
  
  git commit -m "fix: resolve 3 TV display bugs (pengajian, iqamah popup, social media)

  - Fix pengajian filter to show entries with just judul (Bug 1)
  - Increase iqamah popup width and reduce text size for visibility (Bug 2)
  - Split social media handles into separate rotating items (Bug 3)"
  
  # 3. Push to main
  git push origin main
  # Assert: Push succeeds
  ```

  **Evidence to Capture:**
  - [ ] Git status showing only expected files
  - [ ] Commit hash
  - [ ] Push success output

  **Commit**: YES (this is the commit task)
  - Message: See acceptance criteria above
  - Files: `MainActivity.kt`, `PrayerAlertOverlay.kt`
  - Pre-commit: All 3 bugs verified passing

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 5 | `fix: resolve 3 TV display bugs (pengajian, iqamah popup, social media)` | MainActivity.kt, PrayerAlertOverlay.kt | All 3 bugs verified via screenshots |

---

## Success Criteria

### Verification Commands
```bash
# Final verification (run after Task 5 push):
cd android && ./gradlew assembleDebug --no-daemon
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.masjiddisplay/.MainActivity

# Screenshot shows:
# 1. Running text ticker working (announcements, quran, hadith, pengajian, social media rotating)
# 2. IQAMAH overlay (via test panel) shows full text without clipping
# 3. Social media rotation shows all 4 handles individually
```

### Final Checklist
- [ ] Bug 1: Pengajian entries visible in ticker (or confirmed no Supabase data)
- [ ] Bug 2: Iqamah popup text fully visible
- [ ] Bug 3: All social media handles rotate independently
- [ ] Debug logging removed from code
- [ ] Changes committed and pushed to main
- [ ] All evidence screenshots captured in `.sisyphus/evidence/`
