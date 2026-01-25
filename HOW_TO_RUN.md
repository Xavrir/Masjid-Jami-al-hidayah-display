# How to Run the Masjid Display App

## 📱 APK Location
```
D:\ProjekMasjid\Masjid-Jami-al-hidayah-display\android\app\build\outputs\apk\debug\app-debug.apk
```

## ✅ BUILD STATUS
- ✅ **APK Built Successfully**: 3.7 MB
- ✅ **Ready to Install**: app-debug.apk
- ✅ **Target**: Android API 21+ (Android TV & Tablets)

---

## 🚀 OPTION 1: Android Emulator (Recommended for Testing)

### Step 1: Open Android Studio
- Open Android Studio
- Click on **Device Manager** (on right side or Tools > Device Manager)

### Step 2: Create/Select Android TV Emulator
```
Device: Android TV (1080p) or Android TV (4K)
API Level: 34 or higher
Target: Android TV System Image
```

### Step 3: Run the App
**Method A - Via Android Studio:**
```
File > Open > Select project folder
Run > Run 'app'
Select the Android TV emulator
```

**Method B - Via Command Line:**
```bash
cd D:\ProjekMasjid\Masjid-Jami-al-hidayah-display\android
.\gradlew.bat installDebug runDebug
```

---

## 🔌 OPTION 2: Real Android TV Device

### Step 1: Enable Developer Mode
1. On Android TV, go to **Settings**
2. Select **About**
3. Press the select button multiple times on **Build number** (until "You are now a developer" appears)

### Step 2: Enable USB Debugging
1. Go to **Settings > Developer options**
2. Enable **USB Debugging**
3. Connect Android TV to PC via USB cable

### Step 3: Install APK
**Via Command Line:**
```bash
cd D:\ProjekMasjid\Masjid-Jami-al-hidayah-display\android
adb devices                    # List connected devices
.\gradlew.bat installDebug     # Install on connected device
```

**Manual Installation:**
1. Copy `app-debug.apk` to USB drive
2. Connect USB to Android TV
3. Use file manager to install APK

---

## 📺 OPTION 3: Direct APK Installation (Easiest)

### On Windows PC:
1. Navigate to: `D:\ProjekMasjid\Masjid-Jami-al-hidayah-display\android\app\build\outputs\apk\debug\`
2. Copy `app-debug.apk` file
3. Transfer to Android TV device or emulator
4. Open with APK installer on device

---

## 🎮 Using Android Emulator (Step-by-Step)

### Quick Setup:
```powershell
# Install APK to emulator
cd D:\ProjekMasjid\Masjid-Jami-al-hidayah-display\android
.\gradlew.bat installDebug
```

### Launch App:
- Look for **"Masjid Display"** or **"com.masjiddisplay"** in app drawer
- Click to launch
- App will display prayer times and running text loop

---

## 📊 What You'll See

When the app runs, you'll see:

### Main Dashboard Shows:
- ✅ **Prayer Times** - Subuh, Dzuhur, Ashar, Maghrib, Isya, Shuruq
- ✅ **Countdown Timer** - Time until next prayer
- ✅ **Kas Masjid** (Treasury) - Balance display
- ✅ **Running Text Loop** at bottom:
  - Announcements (if configured)
  - Quranic Verses (from Supabase)
  - Hadiths (from Supabase)
  - Teaching Schedules (from Supabase)

### Features to Try:
1. **Watch the countdown** - Updates every second
2. **See running text rotate** - Cycles through all content
3. **Press remote buttons** - Navigate UI on TV emulator
4. **Check debug logs** - See Supabase data fetching in logcat

---

## 🔍 Viewing Debug Logs

To see if Supabase data is being fetched:

### Android Studio:
1. Run app in emulator/device
2. Go to **Logcat** tab (bottom of Android Studio)
3. Search for: `✅` or `⚠️` to see Supabase messages
4. Look for lines like:
```
✅ Successfully fetched X Quran verses from Supabase
✅ Successfully fetched X Hadiths from Supabase
⚠️ Error fetching Quran verses: Network error
```

### Command Line:
```bash
adb logcat | findstr "Successfully fetched"
adb logcat | findstr "Error fetching"
```

---

## 🎬 Expected Output

### First Run (If Supabase is configured):
```
✅ Successfully fetched 8 Quran verses from Supabase
✅ Successfully fetched 10 Hadiths from Supabase
✅ Successfully fetched 4 Pengajian entries from Supabase
✅ Successfully fetched Kas data from Supabase
```

### With Mock Data (Fallback):
```
⚠️ Error fetching Quran verses: Network error
⚠️ Using mock data as fallback
App displays with default Islamic content
```

---

## 🛠️ Troubleshooting

### APK Installation Fails
- Ensure target device API is 21 or higher
- Try clearing app first: `adb uninstall com.masjiddisplay`
- Then reinstall: `.\gradlew.bat installDebug`

### App Crashes on Launch
- Check logcat for errors: `adb logcat`
- Verify Supabase configuration in SupabaseConfig.kt
- App will fall back to mock data if Supabase unavailable

### Can't See Supabase Data
- **Normal!** First, set up Supabase tables per [SUPABASE_SETUP.md](../SUPABASE_SETUP.md)
- App will automatically use mock data if no Supabase data available
- Once tables are created, restart app to see live data

### Emulator Issues
- Ensure Android TV emulator is running before building
- Allocate at least 2GB RAM to emulator
- Use API level 30+ for best compatibility

---

## 📝 Next Steps

1. **Build the APK**: ✅ DONE
2. **Install on device**: Follow Option 1, 2, or 3 above
3. **See running display**: Launch app
4. **Setup Supabase tables**: Follow [SUPABASE_SETUP.md](../SUPABASE_SETUP.md)
5. **Update data**: Add/edit entries in Supabase
6. **Restart app**: Changes appear automatically

---

## 🚀 Quick Commands

### Full Build & Install to Emulator:
```bash
cd D:\ProjekMasjid\Masjid-Jami-al-hidayah-display\android
.\gradlew.bat installDebug
```

### Clean Build:
```bash
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

### View App on Device:
```bash
adb shell am start -n com.masjiddisplay/.MainActivity
```

### Remove App:
```bash
adb uninstall com.masjiddisplay
```

---

**Ready to see your mosque display in action!** 🕌
