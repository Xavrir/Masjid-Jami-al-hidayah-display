# Masjid Display - Prayer Timetable & Kas Display for Android TV

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.01-green.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

A beautiful, native Android TV application for mosque prayer time display, built with **Kotlin** and **Jetpack Compose**.

## Features

### 🕌 Prayer Times Display
- **Real-time Prayer Schedule**: Displays 5 daily prayers (Subuh, Dzuhur, Ashar, Maghrib, Isya) plus Shuruq
- **Adzan & Iqamah Times**: Shows both adzan and iqamah times for each prayer
- **Live Countdown**: Real-time countdown to next prayer
- **Status Indicators**: Visual distinction between passed, current, and upcoming prayers
- **Prayer In Progress View**: Special full-screen view when prayer is in progress
- **Sound Notifications**: Audio alerts for adhan and iqamah times

### 💰 Mosque Treasury (Kas) Management
- **Balance Display**: Current mosque treasury balance
- **Income/Expense Summary**: Monthly income and expense totals
- **Transaction History**: Recent transactions with detailed information
- **Trend Indicators**: Visual trend direction (up/down/stable)
- **Detailed Overlay**: Full treasury details accessible via remote control

### 🎨 Design Philosophy
- **Dark Luxury Theme**: Dark background with gold and teal accents
- **High Contrast**: Optimized for viewing from 5-10 meters distance
- **Calm Motion**: Smooth, non-distracting animations
- **Typography First**: Information hierarchy through text size and weight
- **Breathing Space**: Generous whitespace for visual comfort

### 📺 Android TV Optimized
- **TV Remote Control Support**: Navigate using standard TV remote
- **Leanback UI**: Optimized for 10-foot TV viewing experience
- **Full HD & 4K Support**: Scales beautifully on 1080p, 1440p, and 4K displays
- **Immersive Mode**: Full-screen display without system bars
- **TV Launcher Integration**: Appears in Android TV launcher

### 🌙 Special Features
- **Ramadan Mode**: Special theme variant for Ramadan month
- **Hijri Calendar**: Displays both Gregorian and Hijri dates
- **Announcement Ticker**: Scrolling announcements at bottom of screen
- **Quran Verse Display**: Rotating Quran verses with Arabic text
- **Hadith of the Day**: Daily Hadith with source citation
- **Offline Support**: Works without internet connection

## Technology Stack

- **Language**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose with Material 3
- **Platform**: Android TV (API 21+)
- **Architecture**: Single Activity with Compose Navigation
- **Animation**: Compose Animation API
- **Coroutines**: For async operations and timers
- **Date/Time**: kotlinx-datetime

## Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/masjiddisplay/
│   │   │   ├── MainActivity.kt          # Main entry point
│   │   │   ├── MainApplication.kt       # Application class
│   │   │   ├── data/                    # Data models & mock data
│   │   │   │   ├── Models.kt
│   │   │   │   ├── MockData.kt
│   │   │   │   └── IslamicContent.kt
│   │   │   ├── ui/
│   │   │   │   ├── screens/             # Main screens
│   │   │   │   │   ├── MainDashboard.kt
│   │   │   │   │   └── PrayerInProgress.kt
│   │   │   │   ├── components/          # Reusable UI components
│   │   │   │   │   ├── NextPrayerCard.kt
│   │   │   │   │   ├── AnnouncementTicker.kt
│   │   │   │   │   ├── QuranVerseCard.kt
│   │   │   │   │   ├── HadithCard.kt
│   │   │   │   │   ├── PrayerAlertBanner.kt
│   │   │   │   │   ├── KasSummary.kt
│   │   │   │   │   └── KasDetailOverlay.kt
│   │   │   │   ├── theme/               # Design system
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Typography.kt
│   │   │   │   │   ├── Dimensions.kt
│   │   │   │   │   └── Theme.kt
│   │   │   │   └── state/               # State management
│   │   │   │       └── PrayerNotificationState.kt
│   │   │   ├── services/                # Background services
│   │   │   │   └── SoundNotificationService.kt
│   │   │   └── utils/                   # Utility functions
│   │   │       ├── PrayerTimeCalculator.kt
│   │   │       ├── DateTimeUtils.kt
│   │   │       └── CurrencyUtils.kt
│   │   └── res/                         # Resources
│   │       ├── drawable/
│   │       ├── mipmap-*/
│   │       ├── raw/                     # Sound files
│   │       └── values/
│   └── build.gradle.kts                 # App build config
├── build.gradle                         # Project build config
└── settings.gradle
```

## Installation & Setup

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK with API Level 34
- JDK 17 or later
- Android TV emulator or physical Android TV device

### Step 1: Clone the Repository

```bash
git clone https://github.com/Xavrir/Masjid-Jami-al-hidayah-display.git
cd Masjid-Jami-al-hidayah-display
```

### Step 2: Open in Android Studio

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the `android` folder
4. Wait for Gradle sync to complete

### Step 3: Run on Android TV

#### Using Android TV Emulator

1. Create an Android TV emulator in Android Studio (API 21+)
2. Start the emulator
3. Click Run or press `Shift+F10`

#### Using Physical Android TV Device

1. Enable Developer Options on your Android TV
2. Enable USB Debugging
3. Connect via USB or network ADB:

```bash
adb connect <TV_IP_ADDRESS>:5555
```

4. Click Run in Android Studio

## Configuration

### Customize Mosque Information

Edit `MockData.kt`:

```kotlin
val masjidConfig = MasjidConfig(
    name = "Your Masjid Name",
    location = "Your Location",
    tagline = "Your Tagline",
    latitude = YOUR_LATITUDE,
    longitude = YOUR_LONGITUDE,
    calculationMethod = "Kemenag RI"
)
```

### Customize Prayer Times

The app uses astronomical calculations based on Kemenag RI method. To customize:

1. Update `PrayerTimeCalculator.kt`
2. Modify `FAJR_ANGLE` and `ISHA_ANGLE` constants
3. Update default coordinates

### Customize Treasury Data

Update treasury information in `MockData.kt`:

```kotlin
val kasData = KasData(
    balance = YOUR_BALANCE,
    incomeMonth = YOUR_MONTHLY_INCOME,
    expenseMonth = YOUR_MONTHLY_EXPENSE,
    // ... other fields
)
```

### Customize Announcements

Edit the announcements list in `MockData.kt`:

```kotlin
val announcements = listOf(
    "Your announcement 1",
    "Your announcement 2",
    // Add more announcements
)
```

## Theme Customization

### Colors

Edit `Color.kt` to customize the color palette:

```kotlin
object AppColors {
    val accentPrimary = Color(0xFFD4AF37)  // Gold
    val accentSecondary = Color(0xFF16A085) // Teal
    // ... other colors
}
```

### Typography

Adjust font sizes in `Typography.kt`:

```kotlin
object AppTypography {
    val displayXL = TextStyle(
        fontSize = 72.sp,
        lineHeight = 80.sp,
        fontWeight = FontWeight.Bold
    )
    // ... other styles
}
```

## Building for Production

### Generate Debug APK

```bash
cd android
./gradlew assembleDebug
```

### Generate Release APK

```bash
cd android
./gradlew assembleRelease
```

The APK will be generated at:
```
android/app/build/outputs/apk/release/app-release.apk
```

### Generate Signed APK

1. Generate a keystore:

```bash
keytool -genkey -v -keystore masjid-display.keystore -alias masjid-display -keyalg RSA -keysize 2048 -validity 10000
```

2. Update `build.gradle.kts` with your keystore info

3. Build signed APK:

```bash
./gradlew assembleRelease
```

## Features Implemented

- [x] Prayer times calculation (Kemenag RI method)
- [x] Real-time clock display
- [x] Hijri date display
- [x] Prayer countdown
- [x] Next prayer highlight
- [x] Prayer in progress screen
- [x] Quran verse rotation
- [x] Hadith rotation
- [x] Announcement ticker
- [x] Kas summary display
- [x] Kas detail overlay
- [x] Sound notifications
- [x] Ramadan detection
- [x] TV remote support
- [x] Immersive full-screen mode

## Future Enhancements

- [ ] Backend integration for real-time data updates
- [ ] Admin panel for remote configuration
- [ ] Multiple language support (Arabic, English, Indonesian)
- [ ] Weather information
- [ ] QR code for donation
- [ ] Custom calculation methods

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License.

## Credits

Developed for Masjid Jami' Al-Hidayah, Jakarta Timur.

Design specifications focus on:
- Readability from 5-10 meters distance
- Calm, elegant, non-flashy design
- Single-glance clarity
- Minimal interaction, maximum information

## Support

For issues, questions, or suggestions, please open an issue on the GitHub repository.

---

