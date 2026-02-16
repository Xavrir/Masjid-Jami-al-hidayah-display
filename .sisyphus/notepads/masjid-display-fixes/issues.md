# Issues - Masjid Display Fixes

## 2026-02-06 Initial Issues Identified

### CRITICAL: Popup auto-dismiss broken
- Root cause: `LaunchedEffect(appClock, prayers)` in MainActivity.kt line 213
- `appClock` updates every second, causing LaunchedEffect to restart every second
- The `delay(60_000)` never completes because the effect is cancelled
- Fix: Use a separate LaunchedEffect keyed on `prayerAlertVisible` instead

### Iqamah gap is 15 minutes, should be 10
- In PrayerTimeCalculator.kt lines 117-155
- All prayers use `addMinutesToTime(time, 15)` except Maghrib (5 min)
- Need to change to 10 min

### No countdown 10-1 after iqamah wait
- No countdown timer visual exists in the overlay
- Need to add 10-second countdown (10, 9, 8... 1) after the 10-minute wait

### No 3-minute-before-adzan alert
- Feature doesn't exist at all in current code
- Need to add check for currentTime == adhanTime - 3 minutes

### Overlay text too small
- "Ketuk untuk menutup" text is 13.sp (PrayerAlertOverlay.kt line 209)
- Other text sizes in overlay are relatively small for TV viewing

## 2026-02-06 Updates

### RESOLVED: Popup auto-dismiss broken
- Fixed by splitting detection and auto-dismiss `LaunchedEffect` blocks in MainActivity.kt
