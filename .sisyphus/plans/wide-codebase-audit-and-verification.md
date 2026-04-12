# Wide Codebase Logic Audit, Risk Assessment & Verification Tests

## TL;DR

> **Quick Summary**: Comprehensive logic audit of the Masjid Display system covering 8 critical feature areas across Android TV app and Admin Panel, with future-bug risk assessment and broad verification tests.
> 
> **Deliverables**:
> - (a) Parallel task graph with 5 execution waves
> - (b) Test matrix covering 47 test scenarios across Android + Admin Panel
> - (c) Evidence checklist with 62 verification commands
> - (d) Prioritization rubric for categorizing findings (P0-P3)
> 
> **Estimated Effort**: Large (3-5 days)
> **Parallel Execution**: YES - 5 waves with parallelization
> **Critical Path**: Wave 1 (Setup) → Wave 2 (Audit) → Wave 3 (Tests) → Wave 4 (Integration) → Wave 5 (Report)

---

## Context

### Original Request
User requested ULTRAWORK wide check and wide tests on all previously implemented changes, covering:
1. Full-screen banner overlay
2. Banner timing window (15-25 min post-iqamah)
3. Slideshow interval derivation
4. Pengajian schema fallback mapping
5. Social-media ticker chunking
6. Iqamah popup layout/clipping risk
7. Kas arithmetic rules
8. Supabase readiness race handling

### Research Summary

**Codebase Architecture**:
- **Android TV App**: Native Kotlin + Jetpack Compose (37 source files)
- **Admin Panel**: Vanilla HTML/CSS/JavaScript + AdminLTE v4 (7 HTML pages)
- **Backend**: Supabase (PostgreSQL + Auth + Storage)
- **Test Infrastructure**: None existing - must be created

**Key Files Identified**:
| Feature Area | Primary Files |
|--------------|---------------|
| Banner Overlay | `BannerSlideshow.kt`, `MainDashboard.kt:149-165` |
| Banner Timing | `MainDashboard.kt:149-165`, `PrayerTimeCalculator.kt` |
| Slideshow Interval | `MainDashboard.kt:162-165`, `BannerSlideshow.kt:38-53` |
| Pengajian Fallback | `SupabaseApiService.kt:70-83`, `SupabaseRepository.kt:247-256` |
| Ticker Chunking | `EnhancedRunningText.kt:121-175` |
| Iqamah Popup | `PrayerAlertOverlay.kt:40-314` |
| Kas Arithmetic | `SupabaseRepository.kt:128-203`, `CurrencyUtils.kt` |
| Supabase Race | `kas_masjid.html:842-1196`, `login.html:378-392` |

---

## Work Objectives

### Core Objective
Produce a complete logic audit identifying bugs, risks, and edge cases, along with executable verification tests for all 8 feature areas.

### Concrete Deliverables
1. **Audit Report**: Documented findings for each feature area with severity ratings
2. **Risk Assessment**: Future-bug risk matrix with mitigation recommendations
3. **Test Matrix**: 47 test scenarios with pass/fail criteria
4. **Evidence Artifacts**: Screenshots, logs, and verification outputs in `.sisyphus/evidence/`

### Definition of Done
- [ ] All 8 feature areas audited with documented findings
- [ ] Each finding has severity (P0-P3) and risk assessment
- [ ] All 47 test scenarios executed with evidence collected
- [ ] Summary report generated with prioritized recommendations

### Must Have
- Audit coverage of ALL 8 specified feature areas
- Executable verification commands (no manual-only tests)
- Pass/fail criteria for every test scenario
- Prioritization rubric applied to all findings

### Must NOT Have (Guardrails)
- NO actual code modifications (audit/test ONLY)
- NO skipping feature areas even if they appear "simple"
- NO assumptions about behavior without verification
- NO tests requiring user interaction (all agent-executable)

---

## (d) PRIORITIZATION RUBRIC FOR FINDINGS

### Severity Levels

| Priority | Name | Definition | Response Time | Examples |
|----------|------|------------|---------------|----------|
| **P0** | Critical | Data loss, security breach, complete feature failure | Immediate fix before release | Race condition causing data corruption, auth bypass |
| **P1** | High | Major feature broken, significant UX degradation | Fix within 24 hours | Banner not displaying, wrong prayer times, layout completely broken |
| **P2** | Medium | Feature partially broken, workaround exists | Fix within 1 week | Minor timing inaccuracy, text truncation on edge cases |
| **P3** | Low | Cosmetic, minor inconvenience | Fix when convenient | Slight animation jitter, non-ideal fallback text |

### Risk Categories

| Risk Type | Definition | Indicators |
|-----------|------------|------------|
| **Data Integrity** | Risk of incorrect/lost data | Arithmetic operations, database writes, state mutations |
| **Race Condition** | Risk of timing-dependent failures | Async operations, event handlers, concurrent access |
| **Layout/UX** | Risk of visual defects | Fixed dimensions, text overflow, responsive design |
| **Edge Case** | Risk of failure on boundary conditions | Empty lists, null values, min/max inputs |
| **Integration** | Risk of component mismatch | API contracts, schema changes, version drift |

### Scoring Matrix

```
SEVERITY SCORE = Impact (1-4) × Likelihood (1-4) × Detectability (1-4)

Impact:       1=Cosmetic, 2=Minor, 3=Major, 4=Critical
Likelihood:   1=Rare, 2=Occasional, 3=Frequent, 4=Certain
Detectability: 1=Always caught, 2=Usually caught, 3=Rarely caught, 4=Never caught

Score 1-8:   P3 (Low)
Score 9-24:  P2 (Medium)
Score 25-48: P1 (High)
Score 49-64: P0 (Critical)
```

---

## (a) PARALLEL TASK GRAPH - EXECUTION WAVES

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ WAVE 1: ENVIRONMENT SETUP (Parallel - 2 tasks)                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ [1.1] Android Build Setup ────────┐                                         │
│ [1.2] Admin Panel Setup ──────────┤                                         │
│                                   ▼                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ WAVE 2: STATIC AUDIT (Parallel - 8 tasks)                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ [2.1] Banner Overlay Audit ───────┐                                         │
│ [2.2] Banner Timing Audit ────────┤                                         │
│ [2.3] Slideshow Interval Audit ───┤                                         │
│ [2.4] Pengajian Fallback Audit ───┼───► All depend on Wave 1                │
│ [2.5] Ticker Chunking Audit ──────┤                                         │
│ [2.6] Iqamah Popup Audit ─────────┤                                         │
│ [2.7] Kas Arithmetic Audit ───────┤                                         │
│ [2.8] Supabase Race Audit ────────┘                                         │
│                                   ▼                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ WAVE 3: DYNAMIC VERIFICATION (Parallel - 4 task groups)                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ [3.1] Android Unit Tests ─────────┐                                         │
│ [3.2] Android UI Tests ───────────┤                                         │
│ [3.3] Admin Panel Tests ──────────┼───► All depend on Wave 2                │
│ [3.4] Integration Tests ──────────┘                                         │
│                                   ▼                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ WAVE 4: EDGE CASE & REGRESSION (Sequential - depends on Wave 3)             │
├─────────────────────────────────────────────────────────────────────────────┤
│ [4.1] Boundary Condition Tests ───┐                                         │
│ [4.2] Error Path Tests ───────────┼───► Sequential within wave              │
│ [4.3] Regression Verification ────┘                                         │
│                                   ▼                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ WAVE 5: SYNTHESIS & REPORTING (Sequential - Final)                          │
├─────────────────────────────────────────────────────────────────────────────┤
│ [5.1] Consolidate Findings ───────┐                                         │
│ [5.2] Apply Prioritization ───────┼───► Sequential                          │
│ [5.3] Generate Final Report ──────┘                                         │
└─────────────────────────────────────────────────────────────────────────────┘

Critical Path: 1.1 → 2.1 → 3.1 → 4.1 → 5.3
Parallel Speedup: ~60% faster than sequential execution
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1.1 | None | 2.1-2.8 | 1.2 |
| 1.2 | None | 2.1-2.8 | 1.1 |
| 2.1-2.8 | 1.1, 1.2 | 3.1-3.4 | All Wave 2 tasks |
| 3.1 | 2.1-2.8 | 4.1-4.3 | 3.2, 3.3, 3.4 |
| 3.2 | 2.1-2.8 | 4.1-4.3 | 3.1, 3.3, 3.4 |
| 3.3 | 2.1-2.8 | 4.1-4.3 | 3.1, 3.2, 3.4 |
| 3.4 | 2.1-2.8 | 4.1-4.3 | 3.1, 3.2, 3.3 |
| 4.1-4.3 | 3.1-3.4 | 5.1-5.3 | None (sequential) |
| 5.1-5.3 | 4.1-4.3 | None | None (sequential) |

---

## (b) EXPLICIT TEST MATRIX

### ANDROID APP TEST MATRIX

#### A1. Banner Overlay Tests (7 scenarios)

| ID | Scenario | Input | Expected | Pass Criteria | Priority |
|----|----------|-------|----------|---------------|----------|
| A1.1 | Banner displays in timing window | Time = iqamah + 16min, banners = [img1, img2] | Full-screen overlay visible | Screenshot shows banner covering entire screen | P1 |
| A1.2 | Banner hidden outside window | Time = iqamah + 10min | Normal dashboard visible | Screenshot shows prayer timeline | P1 |
| A1.3 | Banner hidden after window | Time = iqamah + 26min | Normal dashboard visible | Screenshot shows prayer timeline | P1 |
| A1.4 | Empty banner list | banners = [] | No crash, normal dashboard | App running, no ANR | P0 |
| A1.5 | Single banner (no advance) | banners = [img1] | Static display, no cycling | Same image after 30s | P2 |
| A1.6 | Banner with null title | banner.title = null | Displays "Banner N" fallback | Content description = "Banner 1" | P2 |
| A1.7 | Image load failure | banner.image_url = invalid | Graceful fallback | No crash, placeholder or skip | P1 |

#### A2. Banner Timing Window Tests (8 scenarios)

| ID | Scenario | Input | Expected | Pass Criteria | Priority |
|----|----------|-------|----------|---------------|----------|
| A2.1 | Exact window start (15min) | Time = iqamah + 15min 00sec | Banner appears | shouldShowBanners = true | P1 |
| A2.2 | Exact window end (25min) | Time = iqamah + 25min 00sec | Banner hidden | shouldShowBanners = false | P1 |
| A2.3 | Mid-window (20min) | Time = iqamah + 20min | Banner visible | shouldShowBanners = true | P2 |
| A2.4 | Multiple prayers same window | Dzuhur iqamah + 20min, Ashar iqamah + 20min | At least one triggers | shouldShowBanners = true | P2 |
| A2.5 | Imsak excluded | Time = imsak + 20min | No banner | shouldShowBanners = false | P2 |
| A2.6 | Syuruq excluded | Time = syuruq + 20min | No banner | shouldShowBanners = false | P2 |
| A2.7 | Midnight boundary | Isya iqamah = 23:50, check 00:10 | Correct calculation | No crash, correct result | P1 |
| A2.8 | Clock skew simulation | Device time 1 hour ahead | Banners show at wrong time | Document as known limitation | P3 |

#### A3. Slideshow Interval Tests (5 scenarios)

| ID | Scenario | Input | Expected Interval | Pass Criteria | Priority |
|----|----------|-------|-------------------|---------------|----------|
| A3.1 | Default (no banners) | banners = [] | 8000ms | intervalMs = 8000 | P2 |
| A3.2 | Single banner | banners = [1] | 600000ms (10min) | intervalMs = 600000 | P2 |
| A3.3 | 5 banners | banners = [1,2,3,4,5] | 120000ms (2min) | intervalMs = 120000 | P2 |
| A3.4 | 10 banners | banners = [1..10] | 60000ms (1min) | intervalMs = 60000 | P2 |
| A3.5 | Auto-advance cycle | banners = [1,2,3], wait 3 intervals | All banners shown | currentIndex cycles 0→1→2→0 | P1 |

#### A4. Pengajian Fallback Tests (6 scenarios)

| ID | Scenario | Input | Expected Output | Pass Criteria | Priority |
|----|----------|-------|-----------------|---------------|----------|
| A4.1 | Primary fields present | judul="A", pembicara="B" | "A (...) - B" | Uses primary fields | P2 |
| A4.2 | Fallback to tema | judul=null, tema="C" | "C (...) - ..." | Uses tema | P1 |
| A4.3 | Fallback to ustadz | pembicara=null, ustadz="D" | "... (...) - D" | Uses ustadz | P1 |
| A4.4 | Fallback to tanggal | hari=null, tanggal="2026-01-01" | Uses tanggal in schedule | Schedule shows tanggal | P2 |
| A4.5 | Both null (filtered) | judul=null, tema=null | Entry excluded | Not in display list | P1 |
| A4.6 | Jam null | jam=null | Shows "-" | Schedule includes "-" | P3 |

#### A5. Ticker Chunking Tests (5 scenarios)

| ID | Scenario | Input | Expected Duration | Pass Criteria | Priority |
|----|----------|-------|-------------------|---------------|----------|
| A5.1 | Short content (50 chars) | "Hello World..." (50) | 20000ms (minimum) | durationMs = 20000 | P2 |
| A5.2 | Medium content (100 chars) | 100 character string | 30000ms | durationMs = 30000 | P2 |
| A5.3 | Long content (300 chars) | 300 character string | 60000ms (maximum) | durationMs = 60000 | P2 |
| A5.4 | Empty content list | allContent = [] | Default message | "Selamat datang di Masjid..." | P1 |
| A5.5 | Source prefixes | announcements = ["Test"] | "Pengumuman: Test" | Emoji prefix present | P2 |

#### A6. Iqamah Popup Layout Tests (6 scenarios)

| ID | Scenario | Input | Expected | Pass Criteria | Priority |
|----|----------|-------|----------|---------------|----------|
| A6.1 | Normal display | Prayer = Dzuhur | Full overlay visible | Screenshot shows complete popup | P1 |
| A6.2 | Long prayer name | Prayer = "DZUHUR" | No text clipping | All text visible, ellipsis if needed | P2 |
| A6.3 | Countdown animation | Wait 10 seconds | Ring animates to 0 | Countdown reaches 0, overlay dismisses | P1 |
| A6.4 | Manual dismiss | Tap overlay after 1s | Immediate close | Overlay disappears | P2 |
| A6.5 | Early tap protection | Tap within 350ms | No dismiss | Overlay remains | P2 |
| A6.6 | 1080p screen | Screen = 1920x1080 | No overflow | Content fits within 65% width | P1 |

#### A7. Kas Arithmetic Tests (8 scenarios)

| ID | Scenario | Input | Expected | Pass Criteria | Priority |
|----|----------|-------|----------|---------------|----------|
| A7.1 | Simple income | [{jenis:"masuk", nominal:100000}] | balance = 100000 | Correct sum | P0 |
| A7.2 | Simple expense | [{jenis:"keluar", nominal:50000}] | balance = -50000 | Correct difference | P0 |
| A7.3 | Mixed transactions | [masuk:100k, keluar:30k] | balance = 70000 | Correct net | P0 |
| A7.4 | Large numbers | nominal = 9999999999 | No overflow | Correct display | P1 |
| A7.5 | Zero balance | income = expense | balance = 0, trend = FLAT | Correct trend | P2 |
| A7.6 | Negative balance | expense > income | balance < 0 | Displays correctly with color | P2 |
| A7.7 | Monthly filter | Mixed dates | Only current month counted | incomeMonth correct | P1 |
| A7.8 | Currency formatting | nominal = 1234567 | "Rp 1.234.567" | Correct Indonesian format | P2 |

### ADMIN PANEL TEST MATRIX

#### B1. Supabase Race Condition Tests (6 scenarios)

| ID | Scenario | Setup | Expected | Pass Criteria | Priority |
|----|----------|-------|----------|---------------|----------|
| B1.1 | Normal load | Fresh page load | Data displays | Table populated, no errors | P1 |
| B1.2 | Slow network (3G) | DevTools throttling | Data eventually loads | Loading indicator, then data | P1 |
| B1.3 | Supabase timeout | Block Supabase URL | Error message shown | User-friendly error, no crash | P0 |
| B1.4 | Auth check race | Rapid page navigation | Correct auth state | No auth bypass | P0 |
| B1.5 | Concurrent operations | Submit form while loading | No duplicate entries | Single entry created | P1 |
| B1.6 | Event listener cleanup | Navigate away during load | No memory leak | No console errors | P2 |

#### B2. Admin Panel CRUD Tests (6 scenarios)

| ID | Scenario | Action | Expected | Pass Criteria | Priority |
|----|----------|--------|----------|---------------|----------|
| B2.1 | Create kas transaction | Submit form | New row appears | Data in Supabase, UI updated | P0 |
| B2.2 | Read kas data | Load dashboard | Balance displayed | Correct calculation shown | P0 |
| B2.3 | Update banner order | Drag/drop | Order persisted | Refresh shows same order | P1 |
| B2.4 | Delete transaction | Click delete | Row removed | Data removed from Supabase | P1 |
| B2.5 | Validation error | Submit empty form | Error shown | Field highlighted, helpful message | P2 |
| B2.6 | Upload banner image | Select file | Image uploads | URL returned, preview shown | P1 |

---

## (c) EVIDENCE CHECKLIST WITH EXACT COMMANDS

### Wave 1: Environment Setup

#### 1.1 Android Build Setup
```bash
# Verify Android build environment
cd android && ./gradlew --version
# Expected: Gradle 8.x, JDK 17+

# Build debug APK
./gradlew assembleDebug
# Expected: BUILD SUCCESSFUL in Xs
# Artifact: app/build/outputs/apk/debug/app-debug.apk

# Install on emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
# Expected: Success

# Launch app
adb shell am start -n com.masjiddisplay/.MainActivity
# Expected: App launches
```

**Pass Criteria**: APK builds without errors, installs successfully, app launches

#### 1.2 Admin Panel Setup
```bash
# Verify admin panel structure
ls -la admin-panel/*.html admin-panel/pages/*.html
# Expected: login.html, dashboard.html, 5 pages/*.html

# Start local server
cd admin-panel && python3 -m http.server 8080 &
# Expected: Serving HTTP on 0.0.0.0 port 8080

# Verify Supabase connection
curl -s "https://wqupptqjbkuldglnpvor.supabase.co/rest/v1/" -H "apikey: <key>" | head -c 100
# Expected: JSON response (not error)
```

**Pass Criteria**: All files present, server starts, Supabase reachable

---

### Wave 2: Static Audit Commands

#### 2.1 Banner Overlay Audit
```bash
# Check banner timing logic
grep -n "shouldShowBanners" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
# Expected: Line 149-160, timing window logic

# Check BannerSlideshow implementation
grep -n "fillMaxSize\|intervalMs\|currentIndex" android/app/src/main/java/com/masjiddisplay/ui/components/BannerSlideshow.kt
# Expected: Full-screen modifier, interval parameter, index state

# Verify empty list handling
grep -n "isEmpty" android/app/src/main/java/com/masjiddisplay/ui/components/BannerSlideshow.kt
# Expected: Early return on empty list (line 41)
```

#### 2.2 Banner Timing Audit
```bash
# Check timing constants
grep -n "15\|25\|MINUTE" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
# Expected: Lines 156-157 showing 15 and 25 minute offsets

# Check excluded prayers
grep -n "imsak\|shuruq\|syuruq\|sunrise" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
# Expected: These prayers excluded from banner trigger
```

#### 2.3 Slideshow Interval Audit
```bash
# Check interval formula
grep -n "bannerIntervalMs\|coerceAtLeast" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
# Expected: (10 * 60 * 1000) / banners.size.coerceAtLeast(1)

# Check default interval
grep -n "8000L" android/app/src/main/java/com/masjiddisplay/ui/components/BannerSlideshow.kt
# Expected: Default intervalMs = 8000L
```

#### 2.4 Pengajian Fallback Audit
```bash
# Check fallback field definitions
grep -n "judul\|tema\|pembicara\|ustadz" android/app/src/main/java/com/masjiddisplay/data/SupabaseApiService.kt
# Expected: Both primary and fallback fields defined

# Check fallback logic
grep -n "?: " android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt | grep -i "pengajian\|judul\|pembicara"
# Expected: Elvis operators for fallback (judul ?: tema, pembicara ?: ustadz)

# Check filter logic
grep -n "filter" android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt | grep -i pengajian
# Expected: Filter removing entries with null title AND speaker
```

#### 2.5 Ticker Chunking Audit
```bash
# Check duration formula
grep -n "durationMs\|coerceIn" android/app/src/main/java/com/masjiddisplay/ui/components/EnhancedRunningText.kt
# Expected: (content.length * 300).coerceIn(20000, 60000)

# Check source prefixes
grep -n "Pengumuman\|Kas Masjid\|Ayat Quran\|Hadits\|Pengajian\|Ikuti Kami" android/app/src/main/java/com/masjiddisplay/ui/components/EnhancedRunningText.kt
# Expected: All 6 source prefixes with emojis
```

#### 2.6 Iqamah Popup Audit
```bash
# Check layout dimensions
grep -n "fillMaxWidth\|fillMaxSize\|size\|sp\|dp" android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt | head -20
# Expected: 65% width, 36sp fonts, 90dp ring, 48dp padding

# Check text overflow handling
grep -n "maxLines\|Ellipsis\|overflow" android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt
# Expected: maxLines = 2, TextOverflow.Ellipsis

# Check dismissal logic
grep -n "canDismiss\|onDismiss\|clickable" android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt
# Expected: 350ms delay before dismissal enabled
```

#### 2.7 Kas Arithmetic Audit
```bash
# Check arithmetic operations
grep -n "+=" android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt | grep -i "balance\|income\|expense"
# Expected: balance +=, incomeMonth +=, expenseMonth +=

# Check data type
grep -n "Long" android/app/src/main/java/com/masjiddisplay/data/Models.kt | grep -i kas
# Expected: balance: Long, incomeMonth: Long, expenseMonth: Long

# Check trend calculation
grep -n "TrendDirection" android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt
# Expected: incomeMonth >= expenseMonth → UP, else DOWN

# Check currency formatting
grep -n "formatCurrency\|NumberFormat\|Locale" android/app/src/main/java/com/masjiddisplay/utils/CurrencyUtils.kt
# Expected: Indonesian locale, 0 decimal places
```

#### 2.8 Supabase Race Audit
```bash
# Check race condition mitigations in admin panel
grep -n "supabaseReady\|waitForSupabase\|checkInterval" admin-panel/pages/kas_masjid.html
# Expected: Event listener and polling fallback

# Check error handling
grep -n "catch\|error\|Error" admin-panel/pages/kas_masjid.html | head -10
# Expected: Try-catch blocks around Supabase calls

# Check announcements.js (known issue)
cat admin-panel/js/announcements.js
# Expected: Missing error handling (document as P1 finding)

# Check login timeout handling
grep -n "timeout\|Timeout" admin-panel/login.html
# Expected: 5000ms timeout, but callback executes anyway (document as P2 finding)
```

---

### Wave 3: Dynamic Verification Commands

#### 3.1 Android Unit Test Verification

**Using ADB shell for arithmetic verification:**
```bash
# Run app with debug logging
adb logcat -c && adb logcat | grep -i "kas\|balance\|income\|expense" &

# Trigger kas data refresh (via intent or app action)
# Observe logs for correct calculations
adb logcat -d | grep "Successfully calculated Kas data"
# Expected: Log showing correct transaction count and totals
```

**Manual verification via emulator:**
```bash
# Take screenshot of main dashboard
adb shell screencap -p /sdcard/dashboard.png && adb pull /sdcard/dashboard.png .sisyphus/evidence/A0-dashboard.png
# Expected: Screenshot saved

# Take screenshot of kas summary
adb shell screencap -p /sdcard/kas.png && adb pull /sdcard/kas.png .sisyphus/evidence/A7-kas.png
# Expected: Screenshot showing kas balance
```

#### 3.2 Android UI Test Verification

**Banner overlay verification:**
```bash
# Set device time to iqamah + 20 minutes (within banner window)
# Note: Requires root or mock time in app

# Take screenshot
adb shell screencap -p /sdcard/banner.png && adb pull /sdcard/banner.png .sisyphus/evidence/A1-banner.png
# Pass: Full-screen banner visible
# Fail: Dashboard visible instead
```

**Iqamah popup verification:**
```bash
# Wait for iqamah time and capture popup
adb shell screencap -p /sdcard/iqamah-popup.png && adb pull /sdcard/iqamah-popup.png .sisyphus/evidence/A6-iqamah.png
# Pass: Popup visible with countdown
# Fail: No popup or layout broken
```

#### 3.3 Admin Panel Test Commands

**Using Playwright browser automation:**
```bash
# Navigate to login page
# playwright browser navigate to http://localhost:8080/login.html

# Fill login form
# playwright browser fill input[type="email"] with "test@example.com"
# playwright browser fill input[type="password"] with "password"
# playwright browser click button[type="submit"]

# Verify dashboard loads
# playwright browser wait for ".content-wrapper"
# playwright browser screenshot .sisyphus/evidence/B1-dashboard.png
# Pass: Dashboard elements visible
# Fail: Error message or blank page
```

**Supabase race condition test:**
```bash
# Test with network throttling
# 1. Open DevTools → Network → Slow 3G
# 2. Refresh dashboard page
# 3. Observe loading state
# Expected: Loading indicator shown, then data loads
# Fail: Error or blank table
```

#### 3.4 Integration Test Commands

**End-to-end data flow verification:**
```bash
# 1. Add transaction via admin panel
# 2. Wait 5 seconds for Supabase sync
# 3. Check Android app shows updated balance

# Admin: Add transaction
curl -X POST "https://wqupptqjbkuldglnpvor.supabase.co/rest/v1/kas_transaksi" \
  -H "apikey: <key>" \
  -H "Authorization: Bearer <key>" \
  -H "Content-Type: application/json" \
  -d '{"tanggal":"2026-02-24","jenis":"masuk","nominal":100000,"keterangan":"Test"}'
# Expected: 201 Created

# Android: Refresh and verify
adb shell am broadcast -a com.masjiddisplay.REFRESH_DATA
adb logcat -d | grep "Successfully fetched"
# Expected: New balance reflected
```

---

### Wave 4: Edge Case & Regression Commands

#### 4.1 Boundary Condition Tests

```bash
# Test empty banner list
curl -X DELETE "https://wqupptqjbkuldglnpvor.supabase.co/rest/v1/banners?aktif=eq.true" \
  -H "apikey: <key>" -H "Authorization: Bearer <key>"
# Then verify app doesn't crash
adb logcat | grep -i "crash\|exception\|error" | head -5
# Expected: No crash-related logs

# Test very long pengajian title (200 chars)
curl -X POST "https://wqupptqjbkuldglnpvor.supabase.co/rest/v1/pengajian" \
  -H "apikey: <key>" -H "Authorization: Bearer <key>" \
  -H "Content-Type: application/json" \
  -d '{"tema":"<200 char string>","ustadz":"Test","jam":"08:00"}'
# Verify ticker handles gracefully
adb shell screencap -p /sdcard/long-ticker.png && adb pull /sdcard/long-ticker.png .sisyphus/evidence/edge-long-ticker.png
# Expected: Text scrolls, no overflow
```

#### 4.2 Error Path Tests

```bash
# Test Supabase offline
# 1. Block Supabase URL in hosts file or firewall
# 2. Load admin panel
# 3. Verify error handling
grep -i "error\|fail\|unable" /tmp/admin-panel-console.log
# Expected: User-friendly error message

# Test invalid banner image URL
curl -X POST "https://wqupptqjbkuldglnpvor.supabase.co/rest/v1/banners" \
  -H "apikey: <key>" -H "Authorization: Bearer <key>" \
  -H "Content-Type: application/json" \
  -d '{"image_url":"https://invalid.example.com/404.png","aktif":true}'
# Verify Android handles gracefully
adb logcat | grep -i "coil\|image\|error"
# Expected: Fallback or skip, no crash
```

#### 4.3 Regression Verification

```bash
# Verify all previously working features still function

# 1. Prayer time calculation
adb logcat | grep "calculatePrayerTimes"
# Expected: Times calculated correctly

# 2. Hijri date display
adb shell screencap -p /sdcard/hijri.png && adb pull /sdcard/hijri.png .sisyphus/evidence/reg-hijri.png
# Expected: Hijri date visible

# 3. Running text animation
adb shell screencap -p /sdcard/ticker1.png && sleep 5 && adb shell screencap -p /sdcard/ticker2.png
adb pull /sdcard/ticker1.png .sisyphus/evidence/reg-ticker1.png
adb pull /sdcard/ticker2.png .sisyphus/evidence/reg-ticker2.png
# Expected: Different positions (animation working)
```

---

### Wave 5: Synthesis Commands

#### 5.1 Consolidate Findings
```bash
# Collect all evidence
mkdir -p .sisyphus/evidence
ls -la .sisyphus/evidence/
# Expected: All screenshots and logs collected

# Generate findings summary
cat << 'EOF' > .sisyphus/audit-findings.md
# Audit Findings Summary
[To be populated by auditor]
EOF
```

#### 5.2 Apply Prioritization
```bash
# Count findings by priority
grep -c "P0\|P1\|P2\|P3" .sisyphus/audit-findings.md
# Expected: Findings categorized

# Generate priority summary
grep "P0" .sisyphus/audit-findings.md | wc -l  # Critical
grep "P1" .sisyphus/audit-findings.md | wc -l  # High
grep "P2" .sisyphus/audit-findings.md | wc -l  # Medium
grep "P3" .sisyphus/audit-findings.md | wc -l  # Low
```

#### 5.3 Generate Final Report
```bash
# Verify report completeness checklist
# [ ] All 8 feature areas audited
# [ ] All 47 test scenarios executed
# [ ] All findings prioritized
# [ ] Evidence artifacts collected
# [ ] Recommendations documented

cat .sisyphus/audit-report.md | wc -l
# Expected: Comprehensive report (100+ lines)
```

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO
- **User wants tests**: Manual verification (no TDD - audit only)
- **Framework**: None - using shell commands and screenshots

### Evidence Requirements
All verification must be **agent-executable** with:
- Command output captured in `.sisyphus/evidence/`
- Screenshots saved with descriptive names
- Pass/fail determined by comparing actual vs expected output
- No manual user intervention required

---

## Execution Strategy

### Agent Dispatch Summary

| Wave | Tasks | Recommended Agents | Run In Background |
|------|-------|-------------------|-------------------|
| 1 | 1.1, 1.2 | `category="quick"` | YES (parallel) |
| 2 | 2.1-2.8 | `category="ultrabrain"`, `skills=["git-master"]` | YES (all 8 parallel) |
| 3 | 3.1-3.4 | `category="visual-engineering"`, `skills=["playwright"]` | YES (4 parallel) |
| 4 | 4.1-4.3 | `category="ultrabrain"` | NO (sequential) |
| 5 | 5.1-5.3 | `category="writing"` | NO (sequential) |

---

## TODOs

### WAVE 1: Environment Setup

- [ ] 1.1. **Android Build Environment Setup**

  **What to do**:
  - Verify Gradle and JDK versions
  - Build debug APK
  - Install on emulator
  - Launch app and verify startup

  **Must NOT do**:
  - Do not modify any source files
  - Do not change build configuration

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with 1.2)
  - **Blocks**: 2.1-2.8
  - **Blocked By**: None

  **References**:
  - `android/build.gradle` - Project build config
  - `android/app/build.gradle.kts` - App build config
  - `android/gradlew` - Gradle wrapper script

  **Acceptance Criteria**:
  ```bash
  cd android && ./gradlew assembleDebug
  # Assert: Exit code 0
  # Assert: app/build/outputs/apk/debug/app-debug.apk exists
  
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  # Assert: Output contains "Success"
  
  adb shell am start -n com.masjiddisplay/.MainActivity
  # Assert: Exit code 0
  # Screenshot: .sisyphus/evidence/1.1-app-launch.png
  ```

  **Commit**: NO

---

- [ ] 1.2. **Admin Panel Environment Setup**

  **What to do**:
  - Verify all HTML files present
  - Start local HTTP server
  - Test Supabase connectivity

  **Must NOT do**:
  - Do not modify any files
  - Do not commit credentials

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with 1.1)
  - **Blocks**: 2.1-2.8
  - **Blocked By**: None

  **References**:
  - `admin-panel/` - Admin panel directory
  - `admin-panel/vercel.json` - Deployment config

  **Acceptance Criteria**:
  ```bash
  ls admin-panel/*.html admin-panel/pages/*.html | wc -l
  # Assert: Output is 7 (login, dashboard, 5 pages)
  
  cd admin-panel && python3 -m http.server 8080 &
  sleep 2 && curl -s http://localhost:8080/login.html | head -c 50
  # Assert: Contains "<!DOCTYPE html" or similar
  ```

  **Commit**: NO

---

### WAVE 2: Static Audit (8 parallel tasks)

- [ ] 2.1. **Banner Overlay Logic Audit**

  **What to do**:
  - Read and analyze BannerSlideshow.kt
  - Read and analyze MainDashboard.kt banner display logic
  - Document all banner-related state variables
  - Identify edge cases and potential bugs
  - Rate findings using prioritization rubric

  **Must NOT do**:
  - Do not modify any code
  - Do not skip documenting findings

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with 2.2-2.8)
  - **Blocks**: 3.1-3.4
  - **Blocked By**: 1.1, 1.2

  **References**:
  - `android/app/src/main/java/com/masjiddisplay/ui/components/BannerSlideshow.kt` - Full slideshow implementation
  - `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:149-165` - Banner display trigger
  - `android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt:209-222` - Banner fetching

  **Acceptance Criteria**:
  ```bash
  # Verify banner trigger logic exists
  grep -c "shouldShowBanners" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Output >= 1
  
  # Verify empty list handling
  grep -c "isEmpty" android/app/src/main/java/com/masjiddisplay/ui/components/BannerSlideshow.kt
  # Assert: Output >= 1
  
  # Document findings in audit report
  # Assert: .sisyphus/audit-findings.md contains "## 2.1 Banner Overlay"
  ```

  **Commit**: NO

---

- [ ] 2.2. **Banner Timing Window Audit**

  **What to do**:
  - Analyze timing calculation (15-25 min post-iqamah)
  - Verify prayer exclusions (imsak, shuruq)
  - Check Calendar arithmetic for edge cases
  - Document findings with severity ratings

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2

  **References**:
  - `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:149-165` - Timing window logic
  - `android/app/src/main/java/com/masjiddisplay/utils/PrayerTimeCalculator.kt` - Prayer time calculations
  - `android/app/src/main/java/com/masjiddisplay/utils/DateTimeUtils.kt:125-137` - Time parsing

  **Acceptance Criteria**:
  ```bash
  grep -n "add(Calendar.MINUTE, 15)" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Line number found (window start)
  
  grep -n "add(Calendar.MINUTE, 25)" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Line number found (window end)
  ```

  **Commit**: NO

---

- [ ] 2.3. **Slideshow Interval Derivation Audit**

  **What to do**:
  - Analyze interval calculation formula
  - Verify division safety (coerceAtLeast)
  - Document all interval-related constants
  - Test formula with various banner counts

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2

  **References**:
  - `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:162-165` - Interval formula
  - `android/app/src/main/java/com/masjiddisplay/ui/components/BannerSlideshow.kt:38-53` - Auto-advance logic

  **Acceptance Criteria**:
  ```bash
  grep "coerceAtLeast" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Found (prevents division by zero)
  
  # Manual calculation verification:
  # 1 banner: 600000 / 1 = 600000ms (10 min)
  # 5 banners: 600000 / 5 = 120000ms (2 min)
  ```

  **Commit**: NO

---

- [ ] 2.4. **Pengajian Schema Fallback Audit**

  **What to do**:
  - Document all schema fields and fallbacks
  - Verify null handling in repository
  - Check filter logic for missing data
  - Test fallback chains

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2

  **References**:
  - `android/app/src/main/java/com/masjiddisplay/data/SupabaseApiService.kt:70-83` - Schema definition
  - `android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt:247-256` - Fallback logic
  - `admin-panel/pages/pengajian.html:861-879` - Admin panel field mapping

  **Acceptance Criteria**:
  ```bash
  # Verify fallback operators
  grep "judul ?: tema" android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt
  # Assert: Found
  
  grep "pembicara ?: ustadz" android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt
  # Assert: Found
  ```

  **Commit**: NO

---

- [ ] 2.5. **Social-Media Ticker Chunking Audit**

  **What to do**:
  - Analyze content aggregation logic
  - Document duration calculation formula
  - Verify source prefix handling
  - Check empty list fallback

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2

  **References**:
  - `android/app/src/main/java/com/masjiddisplay/ui/components/EnhancedRunningText.kt:121-175` - Multi-source aggregation
  - `android/app/src/main/java/com/masjiddisplay/ui/components/EnhancedRunningText.kt:48-50` - Duration formula
  - `android/app/src/main/java/com/masjiddisplay/MainActivity.kt:241-248` - Social media links

  **Acceptance Criteria**:
  ```bash
  # Verify duration bounds
  grep "coerceIn(20000, 60000)" android/app/src/main/java/com/masjiddisplay/ui/components/EnhancedRunningText.kt
  # Assert: Found
  
  # Verify social media prefix
  grep "Ikuti Kami" android/app/src/main/java/com/masjiddisplay/ui/components/EnhancedRunningText.kt
  # Assert: Found
  ```

  **Commit**: NO

---

- [ ] 2.6. **Iqamah Popup Layout Audit**

  **What to do**:
  - Document all layout dimensions
  - Identify clipping risks for various screen sizes
  - Check text overflow handling
  - Verify dismissal logic

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2

  **References**:
  - `android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt:40-314` - Full implementation
  - `android/app/src/main/java/com/masjiddisplay/ui/theme/Dimensions.kt` - Theme dimensions

  **Acceptance Criteria**:
  ```bash
  # Verify width constraint
  grep "fillMaxWidth(0.65f)" android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt
  # Assert: Found (65% width)
  
  # Verify text overflow handling
  grep "TextOverflow.Ellipsis" android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt
  # Assert: Found
  ```

  **Commit**: NO

---

- [ ] 2.7. **Kas Arithmetic Rules Audit**

  **What to do**:
  - Document all arithmetic operations
  - Verify data types (Long, no floating point)
  - Check trend calculation logic
  - Verify currency formatting

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2

  **References**:
  - `android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt:128-203` - Balance calculation
  - `android/app/src/main/java/com/masjiddisplay/utils/CurrencyUtils.kt` - Currency formatting
  - `android/app/src/main/java/com/masjiddisplay/data/Models.kt` - Data types
  - `admin-panel/pages/kas_masjid.html:1049-1063` - Admin panel calculation

  **Acceptance Criteria**:
  ```bash
  # Verify data type
  grep "balance: Long" android/app/src/main/java/com/masjiddisplay/data/Models.kt
  # Assert: Found (64-bit integer, no precision loss)
  
  # Verify no division operations
  grep -c "/" android/app/src/main/java/com/masjiddisplay/data/SupabaseRepository.kt | grep -v "//" 
  # Assert: Only in comments or non-arithmetic context
  ```

  **Commit**: NO

---

- [ ] 2.8. **Supabase Readiness Race Audit**

  **What to do**:
  - Document all race condition mitigations
  - Identify unprotected Supabase access
  - Check error handling completeness
  - Document announcements.js issue

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2

  **References**:
  - `admin-panel/pages/kas_masjid.html:842-1196` - Race condition handling
  - `admin-panel/login.html:378-392` - Timeout handling
  - `admin-panel/js/announcements.js` - Missing error handling (P1 issue)
  - `admin-panel/js/services/storage.js` - Storage operations

  **Acceptance Criteria**:
  ```bash
  # Verify event-based readiness
  grep "supabaseReady" admin-panel/pages/kas_masjid.html
  # Assert: Found (event listener approach)
  
  # Document known issue
  cat admin-panel/js/announcements.js | grep -c "catch"
  # Assert: 0 (NO error handling - document as P1)
  ```

  **Commit**: NO

---

### WAVE 3: Dynamic Verification (4 parallel task groups)

- [ ] 3.1. **Android Unit Test Execution**

  **What to do**:
  - Run app and capture logs for kas calculation
  - Verify prayer time calculation accuracy
  - Test pengajian fallback with mock data
  - Document all test results

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: `["android-emulator-skill"]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with 3.2, 3.3, 3.4)
  - **Blocks**: 4.1-4.3
  - **Blocked By**: 2.1-2.8

  **References**:
  - All Wave 2 audit findings
  - Test matrix A1-A7

  **Acceptance Criteria**:
  ```bash
  # Launch app and capture logs
  adb logcat -c && adb shell am start -n com.masjiddisplay/.MainActivity
  sleep 10
  adb logcat -d > .sisyphus/evidence/3.1-app-logs.txt
  
  # Verify kas calculation logged
  grep "Successfully calculated Kas" .sisyphus/evidence/3.1-app-logs.txt
  # Assert: Found
  
  # Verify no exceptions
  grep -i "exception\|error\|crash" .sisyphus/evidence/3.1-app-logs.txt | grep -v "Successfully" | wc -l
  # Assert: 0 or only expected warnings
  ```

  **Commit**: NO

---

- [ ] 3.2. **Android UI Test Execution**

  **What to do**:
  - Capture screenshots of all major UI states
  - Verify banner overlay display
  - Verify iqamah popup layout
  - Test responsive behavior

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: `["android-emulator-skill"]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3

  **References**:
  - Test matrix A1, A6

  **Acceptance Criteria**:
  ```bash
  # Capture main dashboard
  adb shell screencap -p /sdcard/ui-dashboard.png
  adb pull /sdcard/ui-dashboard.png .sisyphus/evidence/3.2-dashboard.png
  # Assert: File exists, size > 10KB
  
  # Capture kas summary
  adb shell screencap -p /sdcard/ui-kas.png
  adb pull /sdcard/ui-kas.png .sisyphus/evidence/3.2-kas.png
  # Assert: File exists
  ```

  **Commit**: NO

---

- [ ] 3.3. **Admin Panel Test Execution**

  **What to do**:
  - Load each admin panel page via Playwright
  - Test login flow
  - Test CRUD operations
  - Capture evidence screenshots

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: `["playwright"]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3

  **References**:
  - Test matrix B1, B2
  - `admin-panel/login.html`
  - `admin-panel/dashboard.html`

  **Acceptance Criteria**:
  ```bash
  # Using playwright skill:
  # 1. Navigate to http://localhost:8080/login.html
  # 2. Screenshot: .sisyphus/evidence/3.3-login.png
  # 3. Fill login form and submit
  # 4. Wait for dashboard
  # 5. Screenshot: .sisyphus/evidence/3.3-dashboard.png
  # Assert: Both screenshots captured successfully
  ```

  **Commit**: NO

---

- [ ] 3.4. **Integration Test Execution**

  **What to do**:
  - Test data flow from admin panel to Android app
  - Verify Supabase sync timing
  - Test concurrent operations

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: `["playwright", "android-emulator-skill"]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3

  **References**:
  - All Supabase endpoints
  - Both apps running simultaneously

  **Acceptance Criteria**:
  ```bash
  # Insert test data via API
  curl -X POST "https://wqupptqjbkuldglnpvor.supabase.co/rest/v1/kas_transaksi" \
    -H "apikey: <key>" -H "Authorization: Bearer <key>" \
    -H "Content-Type: application/json" \
    -d '{"tanggal":"2026-02-24","jenis":"masuk","nominal":12345,"keterangan":"IntegrationTest"}'
  # Assert: HTTP 201
  
  # Verify admin panel shows new entry
  # Assert: Entry visible in kas_masjid page
  
  # Verify Android app shows updated data after refresh
  # Assert: Balance includes new transaction
  ```

  **Commit**: NO

---

### WAVE 4: Edge Case & Regression (Sequential)

- [ ] 4.1. **Boundary Condition Tests**

  **What to do**:
  - Test empty data scenarios
  - Test maximum length inputs
  - Test zero/negative values
  - Document all edge case behaviors

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 4 (sequential)
  - **Blocks**: 5.1-5.3
  - **Blocked By**: 3.1-3.4

  **References**:
  - All test matrix edge case scenarios
  - Wave 2 audit findings

  **Acceptance Criteria**:
  - All boundary tests documented with pass/fail
  - Evidence collected for each test
  - Findings prioritized using rubric

  **Commit**: NO

---

- [ ] 4.2. **Error Path Tests**

  **What to do**:
  - Test Supabase offline behavior
  - Test invalid data responses
  - Test network timeout handling
  - Document error messages and recovery

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: `["playwright"]`

  **Parallelization**:
  - **Can Run In Parallel**: NO (depends on 4.1)
  - **Parallel Group**: Wave 4 (sequential)

  **References**:
  - Supabase error handling code
  - Test matrix B1 scenarios

  **Acceptance Criteria**:
  - All error paths tested
  - User-facing error messages documented
  - Recovery behavior verified
  - Findings prioritized

  **Commit**: NO

---

- [ ] 4.3. **Regression Verification**

  **What to do**:
  - Verify all existing features still work
  - Compare current behavior to CHANGES_SUMMARY.md
  - Document any regressions found

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: NO (depends on 4.2)
  - **Parallel Group**: Wave 4 (sequential)

  **References**:
  - `CHANGES_SUMMARY.md` - List of implemented features
  - All component files

  **Acceptance Criteria**:
  - All features from CHANGES_SUMMARY.md verified working
  - No regressions found OR all regressions documented
  - Evidence screenshots for each verified feature

  **Commit**: NO

---

### WAVE 5: Synthesis & Reporting (Sequential)

- [ ] 5.1. **Consolidate Findings**

  **What to do**:
  - Collect all findings from Waves 2-4
  - Organize by feature area
  - Collect all evidence artifacts

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 5 (sequential)
  - **Blocks**: 5.2
  - **Blocked By**: 4.1-4.3

  **Acceptance Criteria**:
  - All findings in single document
  - All evidence files in `.sisyphus/evidence/`
  - Findings linked to evidence

  **Commit**: NO

---

- [ ] 5.2. **Apply Prioritization Rubric**

  **What to do**:
  - Score each finding using rubric
  - Assign P0-P3 priority
  - Group by severity for action planning

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: NO (depends on 5.1)
  - **Parallel Group**: Wave 5 (sequential)
  - **Blocks**: 5.3

  **Acceptance Criteria**:
  - Every finding has priority assigned
  - Scoring rationale documented
  - Summary counts by priority level

  **Commit**: NO

---

- [ ] 5.3. **Generate Final Audit Report**

  **What to do**:
  - Create executive summary
  - Document all findings with priorities
  - Provide recommendations
  - Include evidence links

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: NO (depends on 5.2)
  - **Parallel Group**: Wave 5 (final task)
  - **Blocks**: None

  **References**:
  - All Wave 2-4 outputs
  - Prioritization rubric
  - Evidence directory

  **Acceptance Criteria**:
  - Report saved to `.sisyphus/audit-report.md`
  - Contains all 8 feature area findings
  - Contains 47 test scenario results
  - Contains prioritized recommendations
  - Contains evidence file references

  **Commit**: YES (audit report only)
  - Message: `docs(audit): add wide codebase audit report with findings and recommendations`
  - Files: `.sisyphus/audit-report.md`, `.sisyphus/audit-findings.md`

---

## Success Criteria

### Verification Commands
```bash
# Verify all evidence collected
ls -la .sisyphus/evidence/ | wc -l
# Expected: 20+ files

# Verify all feature areas covered
grep -c "## 2\.[1-8]" .sisyphus/audit-findings.md
# Expected: 8

# Verify all test scenarios documented
grep -c "\[A[1-7]\|B[1-2]\]" .sisyphus/audit-report.md
# Expected: Matches test matrix count

# Verify prioritization applied
grep -c "P[0-3]" .sisyphus/audit-findings.md
# Expected: >= number of findings
```

### Final Checklist
- [ ] All 8 feature areas audited (Wave 2)
- [ ] All 47 test scenarios executed (Wave 3-4)
- [ ] All findings prioritized using rubric (Wave 5)
- [ ] Evidence artifacts collected in `.sisyphus/evidence/`
- [ ] Final report generated with recommendations
- [ ] No source code modified (audit only)
