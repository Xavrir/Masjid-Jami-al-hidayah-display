# Testing Guide - Masjid Display TV App

## Pre-Testing Checklist

Before running the app, ensure you have:

1. **Node.js** (version 18 or higher)
2. **Android Studio** with Android SDK installed
3. **Android TV Emulator** or physical Android TV device
4. **npm dependencies** installed (run `npm install`)

## Running the Application

### Method 1: Using Android TV Emulator

1. **Create Android TV Emulator:**
   ```bash
   # Open Android Studio
   # Tools > Device Manager > Create Device
   # Select: TV > 1080p (Full HD) or 4K (Ultra HD)
   # System Image: API 30 or higher
   # Finish setup
   ```

2. **Start the emulator:**
   ```bash
   # From Android Studio Device Manager, click Play button
   # Or via command line:
   emulator -avd <TV_AVD_NAME>
   ```

3. **Start Metro Bundler:**
   ```bash
   cd "c:\Users\rukiaja\Downloads\Rifqi masjid"
   npm start
   ```

4. **Run the app:**
   ```bash
   # In a new terminal
   npm run android
   ```

### Method 2: Using Physical Android TV

1. **Enable Developer Mode on TV:**
   - Go to Settings > About
   - Click on "Build" 7 times
   - Developer Options will appear

2. **Enable USB Debugging:**
   - Go to Developer Options
   - Enable "USB Debugging"

3. **Connect via ADB:**
   ```bash
   # Find your TV's IP address (Settings > Network)
   adb connect <TV_IP_ADDRESS>:5555
   ```

4. **Run the app:**
   ```bash
   npm start  # Terminal 1
   npm run android  # Terminal 2
   ```

## Features to Test

### ✅ 1. Prayer Times Display
- [ ] Verify 5 daily prayers are displayed (Subuh, Dzuhur, Ashar, Maghrib, Isya)
- [ ] Check Adzan and Iqamah times match Jakarta Timur location
- [ ] Confirm status indicators (Passed, Current, Upcoming) work correctly
- [ ] Test real-time countdown updates every second
- [ ] Verify current prayer is highlighted with gold accent

**Expected Behavior:**
- Times should match Jakarta Timur (GMT+7) prayer schedule
- Current prayer gets highlighted background
- Countdown shows time remaining to next event

### ✅ 2. Next Prayer Card
- [ ] Verify it shows the correct next prayer
- [ ] Check countdown is accurate
- [ ] Confirm Iqamah time is displayed

**Expected Behavior:**
- Large, prominent display
- Updates automatically when prayer status changes
- Shows both Adzan and Iqamah times

### ✅ 3. Islamic Content Rotation

#### Quran Verses (📖)
- [ ] Verify verse displays correctly with Arabic text
- [ ] Check Indonesian translation appears
- [ ] Test auto-rotation every 40 seconds
- [ ] Verify smooth fade in/out animation

**Expected Content:**
- 8 different Quran verses rotate
- Surah name and verse number displayed
- Transliteration included (optional display)

#### Hadith (📜)
- [ ] Verify Hadith displays in Arabic
- [ ] Check translation quality
- [ ] Test auto-rotation every 50 seconds
- [ ] Verify narrator and source are shown

**Expected Content:**
- 10 different Hadiths rotate
- Category label displayed
- Source (Bukhari, Muslim, etc.) shown

### ✅ 4. Islamic Studies/Kajian Info (📅)
- [ ] Verify kajian schedule displays correctly
- [ ] Check instructor names appear
- [ ] Test different kajian types (Kajian, Tahfidz, TPA, Halaqah, Daurah)
- [ ] Verify category icons and colors

**Expected Behavior:**
- Shows today's kajian if filtered
- Displays upcoming 5 kajian if not filtered
- Scrollable if more than 3 items

### ✅ 5. Mosque Treasury (Kas) Display
- [ ] Verify current balance is displayed
- [ ] Check monthly income/expense summary
- [ ] Test trend indicator (up/down/flat)
- [ ] Verify currency formatting (IDR)

**Expected Behavior:**
- Green for positive balance
- Red for negative balance
- Properly formatted rupiah (Rp 45.250.000)

### ✅ 6. Announcement Ticker
- [ ] Verify announcements scroll smoothly
- [ ] Check speed is appropriate for reading
- [ ] Test multiple announcements
- [ ] Verify icon appears

**Expected Behavior:**
- Slow, smooth scrolling
- Loops continuously
- Readable from 5-10 meters

### ✅ 7. Header Information
- [ ] Verify Masjid name displays: "MASJID JAMI' AL-HIDAYAH"
- [ ] Check tagline: "Memakmurkan Masjid, Mencerahkan Umat"
- [ ] Verify current time updates every second
- [ ] Check Gregorian date formatting (Indonesian)
- [ ] Verify Hijri date calculation
- [ ] Test location badge shows "Jakarta Timur"

**Expected Display:**
```
MASJID JAMI' AL-HIDAYAH
Memakmurkan Masjid, Mencerahkan Umat

14:25:30
Selasa, 26 November 2025
15 Ramadhan 1447 H

📍 Jakarta Timur
📶 Online
```

### ✅ 8. TV Remote Control Integration
Test with Android TV remote:

- [ ] **Menu Button**: Toggle Kas Detail overlay
- [ ] **Play/Pause Button**: Toggle prayer view (demo mode)
- [ ] **Back Button**: Close overlays / exit app
- [ ] **D-Pad**: Navigate (when focus enabled)

### ✅ 9. Prayer In Progress Screen
To test this screen:

1. Wait for actual prayer time, OR
2. Press Play/Pause on TV remote (demo mode), OR
3. Temporarily modify prayer times in code for testing

**Check:**
- [ ] Full-screen calm design
- [ ] Prayer name displayed prominently
- [ ] Countdown to end of prayer
- [ ] Prayer timeline dots at bottom
- [ ] Adzan and Iqamah time chips

### ✅ 10. Kas Detail Overlay
Trigger: Press Menu button on TV remote

**Check:**
- [ ] Slides in from right
- [ ] Shows detailed balance information
- [ ] Displays recent transactions
- [ ] Transaction list is scrollable
- [ ] Income shown in green (+)
- [ ] Expenses shown in red (-)
- [ ] Close button works

### ✅ 11. Visual Design & Layout
- [ ] Verify dark luxury theme (black background, gold accents)
- [ ] Check text is readable from 5-10 meters
- [ ] Test on 1080p (Full HD) resolution
- [ ] Test on 4K resolution (if available)
- [ ] Verify spacing and breathing room
- [ ] Check animations are smooth and calm

**Color Scheme:**
- Background: #020712 (very dark blue-black)
- Primary Accent: #D4AF37 (gold)
- Secondary Accent: #16A085 (teal)
- Text: White with various opacities

### ✅ 12. Performance Testing
- [ ] Check smooth 60fps animations
- [ ] Verify no memory leaks (run for 1+ hour)
- [ ] Test automatic data refresh
- [ ] Check React Native performance monitor

To enable performance monitor:
```bash
# Shake device or press Ctrl+M
# Select "Show Perf Monitor"
```

## Common Issues & Solutions

### Issue: Prayer times don't update
**Solution:**
- Check system time is correct
- Verify coordinates match Jakarta Timur
- Check console for errors

### Issue: Adhan library error
**Solution:**
- Fallback times will be used automatically
- Check if `adhan` package installed correctly
- Verify `prayerTimesAdhan.ts` imports work

### Issue: Components not visible
**Solution:**
- Check ScrollView functionality
- Verify component imports in `MainDashboardEnhanced.tsx`
- Check for console errors

### Issue: Text too small on TV
**Solution:**
- Adjust typography scale in `src/theme/typography.ts`
- Increase font sizes by 10-20%

### Issue: Animations laggy
**Solution:**
- Enable hardware acceleration
- Check `useNativeDriver: true` in animations
- Reduce animation complexity

## Manual Testing Scenarios

### Scenario 1: Full Day Test
1. Run app in morning
2. Observe prayer status changes throughout day
3. Verify Next Prayer Card updates automatically
4. Check Prayer In Progress triggers at correct times

### Scenario 2: Content Rotation Test
1. Run app for 10 minutes
2. Count how many Quran verses appear (should be ~15 rotations)
3. Count how many Hadiths appear (should be ~12 rotations)
4. Verify smooth transitions

### Scenario 3: Ramadan Mode Test
1. Temporarily modify date to Ramadan month
2. Verify "🌙 Ramadan Kareem" badge appears
3. Check if special theme is applied

### Scenario 4: Remote Control Test
1. Press all remote buttons
2. Verify Menu button toggles Kas overlay
3. Test Play/Pause for prayer view toggle
4. Ensure Back button works properly

## Expected App Behavior

### On Launch:
1. Splash screen (if configured)
2. Load prayer times for current date
3. Calculate current prayer status
4. Start time updates every second
5. Begin content rotation timers
6. Display all components simultaneously

### During Operation:
1. Real-time clock updates (HH:mm:ss)
2. Prayer status updates automatically
3. Countdown timers decrement
4. Quran verses rotate every 40s
5. Hadiths rotate every 50s
6. Announcements scroll continuously

### On Prayer Time:
1. Current prayer gets highlighted
2. Next Prayer Card updates
3. Optional: Trigger Prayer In Progress screen
4. Countdown shows time to next prayer

## Logs to Monitor

Enable React Native dev menu and check console for:

```
✅ Prayer times calculated successfully
✅ Component mounted
✅ Auto-rotation started
⚠️ Warning: [any warnings]
❌ Error: [any errors]
```

## Performance Metrics

Expected values:
- **JS Frame Rate**: 55-60 fps
- **UI Frame Rate**: 55-60 fps
- **Memory Usage**: < 200 MB
- **Bundle Size**: ~15-20 MB

## Final Checklist Before Deployment

- [ ] All features tested and working
- [ ] No console errors
- [ ] Performance metrics acceptable
- [ ] Tested on target TV hardware
- [ ] Prayer times accurate for location
- [ ] Islamic content appropriate and correct
- [ ] Remote control fully functional
- [ ] Runs stable for 24+ hours
- [ ] Kas data updates correctly
- [ ] Announcements relevant and current

## Reporting Issues

If you find issues, note:
1. Device model / emulator spec
2. Android API level
3. Exact steps to reproduce
4. Screenshots/video if possible
5. Console error messages

---

**Ready to test! 🚀**

For questions or issues, please document them in the project repository.
