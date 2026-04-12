# Android Security & Reliability Fixes

## TL;DR

> **Quick Summary**: Fix 3 Android-only risks: remove insecure trust-all TLS client, add daily prayer schedule refresh at date rollover, and strengthen time parsing validation.
> 
> **Deliverables**:
> - Secure OkHttp client using system CA trust store (replaces insecure trust-all)
> - Date rollover detection with automatic prayer schedule recalculation
> - Robust time parsing with regex validation and error handling
> - Clean lint build (no blocking errors - already verified)
> - Release APK packaged and pushed to main
> 
> **Estimated Effort**: Short (2-3 hours)
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: Task 1 → Task 4 (build) → Task 5 (verify) → Task 6 (commit/push)

---

## Context

### Original Request
Fix all Android-only risks and push to main. Web is out of scope.

### Research Findings

**Issue Analysis:**

| Issue | Status | Details |
|-------|--------|---------|
| Trust-all TLS client | ✅ CONFIRMED | `SupabaseRepository.kt:29-50` bypasses ALL certificate validation |
| Date rollover refresh | ✅ CONFIRMED | `MainDashboard.kt:59-79` calculates prayers once on launch only |
| Time parsing validation | ✅ CONFIRMED | `DateTimeUtils.kt:125-137` uses `toIntOrNull() ?: 0` |
| LongLogTag lint error | ❌ NOT FOUND | TAG is "SoundNotifService" (17 chars, limit is 23) |
| Debug manifest exported | ✅ ALREADY FIXED | `android:exported="true"` present in debug manifest |
| Lint blocking errors | ✅ NONE | Lint passes with warnings only |

---

## Work Objectives

### Core Objective
Eliminate security vulnerabilities and reliability issues in the Android TV mosque display app.

### Concrete Deliverables
- `SupabaseRepository.kt` - Secure OkHttp client with proper TLS
- `MainDashboard.kt` - Date rollover detection and schedule refresh
- `DateTimeUtils.kt` - Robust time parsing with validation
- Release APK: `app/build/outputs/apk/release/app-release.apk`

### Definition of Done
- [ ] TLS client uses system trust store (no trust-all bypass)
- [ ] Prayer schedule auto-refreshes at midnight date change
- [ ] Time parsing validates HH:mm format before parsing
- [ ] `./gradlew assembleRelease` completes successfully
- [ ] All changes committed and pushed to main

### Must Have
- Remove ALL insecure TLS code (trust-all manager, hostname verifier bypass)
- Date rollover detection triggers prayer recalculation
- Time parsing fails gracefully with logged error, not silent default

### Must NOT Have (Guardrails)
- Do NOT add new dependencies (use existing OkHttp patterns)
- Do NOT change prayer calculation algorithm (PrayerTimeCalculator.kt)
- Do NOT modify UI components or layouts
- Do NOT change Supabase API endpoints or authentication
- Do NOT fix lint warnings (only fix blocking errors, which there are none)

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO
- **User wants tests**: Manual verification via build + runtime check
- **Framework**: None (manual verification)

### Automated Verification (Agent-Executable)

**For each fix:**

```bash
# 1. Build verification
cd android && ./gradlew assembleRelease --no-daemon
# Assert: BUILD SUCCESSFUL

# 2. Lint verification  
./gradlew lint
# Assert: No blocking errors (warnings acceptable)

# 3. Static code verification
grep -r "trustAllCerts\|hostnameVerifier.*true\|checkServerTrusted.*{}" app/src/
# Assert: No matches (insecure code removed)
```

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Independent - Can Run In Parallel):
├── Task 1: Remove insecure TLS client (SupabaseRepository.kt)
├── Task 2: Add date rollover detection (MainDashboard.kt)
└── Task 3: Strengthen time parsing (DateTimeUtils.kt)

Wave 2 (After Wave 1):
└── Task 4: Build release APK

Wave 3 (After Wave 2):
└── Task 5: Verify build + static analysis

Wave 4 (Final):
└── Task 6: Commit and push to main

Critical Path: Any of Tasks 1-3 → Task 4 → Task 5 → Task 6
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 4 | 2, 3 |
| 2 | None | 4 | 1, 3 |
| 3 | None | 4 | 1, 2 |
| 4 | 1, 2, 3 | 5 | None |
| 5 | 4 | 6 | None |
| 6 | 5 | None | None (final) |

---

## TODOs

- [ ] 1. Remove Insecure Trust-All TLS Client

  **What to do**:
  - Open `android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt`
  - Delete `createUnsafeOkHttpClient()` function (lines 29-50)
  - Replace with secure OkHttpClient:
    ```kotlin
    private fun createSecureOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    ```
  - Update line 54 to call `createSecureOkHttpClient()` instead of `createUnsafeOkHttpClient()`
  - Remove unused imports: `java.security.SecureRandom`, `java.security.cert.X509Certificate`, `javax.net.ssl.*`

  **Must NOT do**:
  - Do NOT add certificate pinning (requires backend coordination)
  - Do NOT change API endpoints or authentication headers
  - Do NOT modify any fetch methods (getQuranVerses, getHadiths, etc.)

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Task 4
  - **Blocked By**: None

  **References**:
  - `SupabaseRepository.kt:29-50` - Current insecure client implementation
  - `SupabaseRepository.kt:52-56` - Retrofit builder using the client
  - OkHttp default behavior: Uses system CA trust store when no custom SSL configured

  **Acceptance Criteria**:
  ```bash
  # Verify insecure code removed
  grep -c "trustAllCerts\|X509TrustManager\|hostnameVerifier.*true" \
    android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt
  # Assert: Output is 0 (no matches)
  
  # Verify secure client exists
  grep -c "createSecureOkHttpClient" \
    android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt
  # Assert: Output >= 2 (function definition + usage)
  ```

  **Commit**: YES (groups with Tasks 2, 3)
  - Message: `security(android): remove insecure trust-all TLS client`
  - Files: `SupabaseRepository.kt`

---

- [ ] 2. Add Daily Prayer Schedule Refresh at Date Rollover

  **What to do**:
  - Open `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt`
  - Add date tracking state near line 51:
    ```kotlin
    var lastCalculatedDate by remember { mutableStateOf<Int?>(null) }
    ```
  - Modify the time update LaunchedEffect (lines 81-86) to detect date change:
    ```kotlin
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            
            // Check for date rollover
            val currentDayOfYear = jakartaCalendar(currentTime).get(Calendar.DAY_OF_YEAR)
            if (lastCalculatedDate != null && lastCalculatedDate != currentDayOfYear) {
                // Date changed - recalculate prayer times
                val today = jakartaCalendar().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                val tomorrow = jakartaCalendar().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                prayers = PrayerTimeCalculator.calculatePrayerTimesForJakarta(today)
                tomorrowPrayers = PrayerTimeCalculator.calculatePrayerTimesForJakarta(tomorrow)
                shuruqTime = PrayerTimeCalculator.calculateShuruqTimeForJakarta(today)
                imsakTime = PrayerTimeCalculator.calculateImsakTimeForJakarta(today)
            }
            lastCalculatedDate = currentDayOfYear
            
            delay(1000)
        }
    }
    ```

  **Must NOT do**:
  - Do NOT change PrayerTimeCalculator.kt calculation logic
  - Do NOT change initial calculation in first LaunchedEffect (lines 59-79)
  - Do NOT change prayer status update logic (lines 88-112)

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Task 4
  - **Blocked By**: None

  **References**:
  - `MainDashboard.kt:51-57` - Current state declarations
  - `MainDashboard.kt:59-79` - Initial prayer calculation (keep unchanged)
  - `MainDashboard.kt:81-86` - Current time update loop (modify this)
  - `DateTimeUtils.kt:11-17` - jakartaCalendar() helper

  **Acceptance Criteria**:
  ```bash
  # Verify date rollover detection exists
  grep -c "lastCalculatedDate\|DAY_OF_YEAR" \
    android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Output >= 2
  
  # Verify recalculation on date change
  grep -c "Date changed.*recalculate\|calculatePrayerTimesForJakarta" \
    android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Output >= 2 (initial calc + rollover calc)
  ```

  **Commit**: YES (groups with Tasks 1, 3)
  - Message: `fix(android): refresh prayer schedule on date rollover`
  - Files: `MainDashboard.kt`

---

- [ ] 3. Strengthen Time Parsing Validation

  **What to do**:
  - Open `android/app/src/main/java/com/masjiddisplay/utils/DateTimeUtils.kt`
  - Replace `parseTimeToCalendar` function (lines 125-137) with robust version:
    ```kotlin
    /**
     * Parse time string (HH:mm) to Calendar with given reference date.
     * Returns null if format is invalid.
     */
    fun parseTimeToCalendar(time: String, reference: Date): Calendar? {
        // Validate HH:mm format
        val regex = Regex("^([01]?\\d|2[0-3]):([0-5]\\d)$")
        if (!regex.matches(time)) {
            println("⚠️ Invalid time format: $time (expected HH:mm)")
            return null
        }
        
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        
        val calendar = jakartaCalendar(reference)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        return calendar
    }
    ```
  - Update all callers to handle nullable return:
    - `PrayerTimeCalculator.kt:314` - use `?: return prayers` fallback
    - `PrayerTimeCalculator.kt:319` - use `?: continue` in loop
    - `MainDashboard.kt:155,156` - use `?: return@any false` 

  **Must NOT do**:
  - Do NOT change time arithmetic helpers (addMinutesToTime, subtractMinutesFromTime)
  - Do NOT change Hijri calendar conversion
  - Do NOT change date formatting functions

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Task 4
  - **Blocked By**: None

  **References**:
  - `DateTimeUtils.kt:125-137` - Current parseTimeToCalendar implementation
  - `PrayerTimeCalculator.kt:314,319,332,343,378,389,395,396` - All callers of parseTimeToCalendar
  - `MainDashboard.kt:155,156` - Additional callers in UI

  **Acceptance Criteria**:
  ```bash
  # Verify regex validation exists
  grep -c "Regex.*HH:mm\|Invalid time format" \
    android/app/src/main/java/com/masjiddisplay/utils/DateTimeUtils.kt
  # Assert: Output >= 1
  
  # Verify function returns nullable
  grep "fun parseTimeToCalendar.*Calendar?" \
    android/app/src/main/java/com/masjiddisplay/utils/DateTimeUtils.kt
  # Assert: Output shows nullable return type
  ```

  **Commit**: YES (groups with Tasks 1, 2)
  - Message: `fix(android): add robust time parsing validation`
  - Files: `DateTimeUtils.kt`, `PrayerTimeCalculator.kt`, `MainDashboard.kt`

---

- [ ] 4. Build Release APK

  **What to do**:
  - Run lint check: `./gradlew lint`
  - Run release build: `./gradlew assembleRelease --no-daemon`
  - Verify APK exists: `ls -la app/build/outputs/apk/release/`

  **Must NOT do**:
  - Do NOT fix lint warnings (only errors, which there are none)
  - Do NOT modify build.gradle.kts

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 2 (sequential)
  - **Blocks**: Task 5
  - **Blocked By**: Tasks 1, 2, 3

  **References**:
  - `android/app/build.gradle.kts` - Build configuration
  - `android/build.gradle` - Project build config

  **Acceptance Criteria**:
  ```bash
  cd android
  
  # Lint check (warnings OK, errors fail)
  ./gradlew lint 2>&1 | tail -10
  # Assert: No "BUILD FAILED" in output
  
  # Release build
  ./gradlew assembleRelease --no-daemon 2>&1 | tail -5
  # Assert: Contains "BUILD SUCCESSFUL"
  
  # APK exists
  ls -la app/build/outputs/apk/release/app-release.apk
  # Assert: File exists
  ```

  **Commit**: NO (build verification only)

---

- [ ] 5. Verify Build and Static Analysis

  **What to do**:
  - Verify no insecure TLS code remains
  - Verify all modified files compile without errors
  - Run final lint check

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (sequential)
  - **Blocks**: Task 6
  - **Blocked By**: Task 4

  **References**:
  - Task 1-3 acceptance criteria

  **Acceptance Criteria**:
  ```bash
  # Security verification - no insecure TLS
  grep -r "trustAllCerts\|X509TrustManager\|hostnameVerifier.*true" \
    android/app/src/main/java/
  # Assert: No matches (exit code 1)
  
  # Date rollover exists
  grep -l "lastCalculatedDate" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: File found
  
  # Time parsing validation exists
  grep -l "Invalid time format" android/app/src/main/java/com/masjiddisplay/utils/DateTimeUtils.kt
  # Assert: File found
  
  # Build is clean
  cd android && ./gradlew compileReleaseKotlin --no-daemon 2>&1 | tail -3
  # Assert: Contains "BUILD SUCCESSFUL"
  ```

  **Commit**: NO (verification only)

---

- [ ] 6. Commit and Push to Main

  **What to do**:
  - Stage all modified files
  - Create atomic commit with clear message
  - Push to main branch

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`git-master`]

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 4 (final)
  - **Blocks**: None
  - **Blocked By**: Task 5

  **References**:
  - Modified files from Tasks 1-3

  **Acceptance Criteria**:
  ```bash
  # Stage files
  git add android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt
  git add android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  git add android/app/src/main/java/com/masjiddisplay/utils/DateTimeUtils.kt
  git add android/app/src/main/java/com/masjiddisplay/utils/PrayerTimeCalculator.kt
  
  # Verify staged files
  git diff --cached --name-only
  # Assert: Shows expected files only
  
  # Commit
  git commit -m "fix(android): resolve security and reliability risks

  - Remove insecure trust-all TLS client (CVE risk)
  - Add date rollover detection for prayer schedule refresh
  - Strengthen time parsing with regex validation"
  
  # Push
  git push origin main
  # Assert: Push succeeds
  ```

  **Commit**: YES
  - Message: See above
  - Files: `SupabaseRepository.kt`, `MainDashboard.kt`, `DateTimeUtils.kt`, `PrayerTimeCalculator.kt`

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 6 | `fix(android): resolve security and reliability risks` | 4 files | Build passes, static analysis clean |

---

## Success Criteria

### Verification Commands
```bash
# Final verification
cd android

# 1. No insecure TLS code
grep -r "trustAllCerts" app/src/ && echo "FAIL: insecure code found" || echo "PASS: secure"

# 2. Date rollover detection
grep "lastCalculatedDate" app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt && echo "PASS" || echo "FAIL"

# 3. Time parsing validation
grep "Invalid time format" app/src/main/java/com/masjiddisplay/utils/DateTimeUtils.kt && echo "PASS" || echo "FAIL"

# 4. Release build
./gradlew assembleRelease --no-daemon
ls app/build/outputs/apk/release/app-release.apk && echo "PASS: APK exists"

# 5. Pushed to main
git log --oneline -1
# Should show the security fix commit
```

### Final Checklist
- [ ] Insecure trust-all TLS client removed
- [ ] Date rollover triggers prayer schedule refresh
- [ ] Time parsing validates format before conversion
- [ ] Lint passes (no blocking errors)
- [ ] Release APK builds successfully
- [ ] All changes committed and pushed to main
