# Learnings - Masjid Display Fixes

## 2026-02-06 Initial Analysis

### Architecture
- Kotlin + Jetpack Compose Android TV app
- Single Activity: `MainActivity.kt` with `MasjidDisplayApp` composable
- Prayer times calculated astronomically via `PrayerTimeCalculator.kt`
- Location: Masjid Jami' Al-Hidayah, Jakarta Timur (-6.3140892, 106.8776666)
- Kemenag RI method: Fajr angle 20 deg, Isha angle 18 deg

### Key Files
- `MainActivity.kt` - Main app logic, overlay trigger logic (lines 213-285)
- `PrayerAlertOverlay.kt` - The popup/overlay UI component
- `PrayerTimeCalculator.kt` - Prayer time calculations, iqamah gap logic
- `PrayerNotificationState.kt` - Alert state management (has auto-dismiss)
- `PrayerInProgress.kt` - Prayer in progress screen
- `Models.kt` - Data classes (Prayer, KasData, etc.)

### Current Iqamah Gap Logic (PrayerTimeCalculator.kt lines 117-155)
- Subuh: adhan + 15 min
- Dzuhur: adhan + 15 min
- Ashar: adhan + 15 min
- Maghrib: adhan + 5 min (special case)
- Isya: adhan + 15 min

### Current Auto-Dismiss Logic (MainActivity.kt lines 213-285)
- Adhan overlay: shows for 60_000ms (1 min) then auto-hides
- Iqamah overlay: shows for 60_000ms (1 min) then auto-hides
- Friday reminder: shows for 10_000ms (10 sec)
- Also can be dismissed by tap ("Ketuk untuk menutup")

### Bug: Popup Not Auto-Dismissing
- The auto-dismiss uses coroutine `delay()` inside `LaunchedEffect(appClock, prayers)`
- Problem: `LaunchedEffect(appClock, prayers)` re-launches EVERY SECOND because `appClock` changes every second
- This means the delay(60_000) never completes - it gets cancelled and restarted every second
- The popup ONLY disappears via manual tap currently

### 3-Minute Before Adzan
- Currently NO implementation for 3-minute-before-adzan alert
- Only Friday reminder exists (10 min before Dzuhur on Fridays)

## 2026-02-06 Overlay Auto-Dismiss Fix

### Overlay Timing Pattern
- Split detection and auto-dismiss into separate `LaunchedEffect` blocks
- Detection stays keyed on `appClock`, auto-dismiss keyed on visibility/type to allow `delay()` completion

## 2026-02-06 Iqamah Countdown Overlay

- Added a 10-second countdown in `PrayerAlertOverlay.kt` for IQAMAH overlays, driven by `LaunchedEffect(visible, overlayType)` and a `mutableIntStateOf(10)` state.
