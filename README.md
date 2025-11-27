# Masjid Display - Prayer Timetable & Kas Display for Android TV

Sleek, minimal, and luxurious prayer timetable display with mosque treasury (kas) information, specifically designed for Android TV using React Native.

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![React Native](https://img.shields.io/badge/React%20Native-0.73.2-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## Features

### 🕌 Prayer Times Display
- **Real-time Prayer Schedule**: Displays 5 daily prayers (Subuh, Dzuhur, Ashar, Maghrib, Isya)
- **Adzan & Iqamah Times**: Shows both adzan and iqamah times for each prayer
- **Live Countdown**: Real-time countdown to next prayer
- **Status Indicators**: Visual distinction between passed, current, and upcoming prayers
- **Prayer In Progress View**: Special full-screen view when prayer is in progress

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
- **Auto-rotation Disabled**: Fixed landscape orientation
- **TV Launcher Integration**: Appears in Android TV launcher

### 🌙 Special Features
- **Ramadan Mode**: Special theme variant for Ramadan month
- **Hijri Calendar**: Displays both Gregorian and Hijri dates
- **Announcement Ticker**: Scrolling announcements at bottom of screen
- **Offline Support**: Works without internet connection

## Screenshots

### Main Dashboard
The main dashboard displays:
- Prayer times table with status indicators
- Next prayer highlight card
- Current mosque treasury summary
- Announcement ticker
- Current time with both Gregorian and Hijri dates

### Prayer In Progress
Full-screen view when prayer is ongoing:
- Large prayer name display
- Countdown to prayer end
- Prayer timeline indicator
- Calm, focused design

### Treasury (Kas) Detail Overlay
Accessible via TV remote:
- Current balance with trend
- Monthly income/expense breakdown
- Recent transaction list
- 30-day trend visualization

## Technology Stack

- **Framework**: React Native 0.73.2
- **Language**: TypeScript
- **Platform**: Android TV
- **Styling**: StyleSheet API with custom design tokens
- **Animation**: React Native Animated API
- **Date Handling**: date-fns
- **Gradients**: react-native-linear-gradient

## Project Structure

```
Rifqi masjid/
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── PrayerRow.tsx
│   │   ├── NextPrayerCard.tsx
│   │   ├── KasSummary.tsx
│   │   ├── AnnouncementTicker.tsx
│   │   └── KasDetailOverlay.tsx
│   ├── screens/             # Main screen components
│   │   ├── MainDashboard.tsx
│   │   └── PrayerInProgress.tsx
│   ├── theme/               # Design tokens and theme
│   │   ├── colors.ts
│   │   ├── typography.ts
│   │   ├── spacing.ts
│   │   ├── motion.ts
│   │   └── index.ts
│   ├── types/               # TypeScript type definitions
│   │   └── index.ts
│   ├── utils/               # Utility functions
│   │   ├── prayerTimes.ts
│   │   ├── dateTime.ts
│   │   └── currency.ts
│   ├── data/                # Mock data for testing
│   │   └── mockData.ts
│   └── App.tsx              # Main application component
├── android/                 # Android native code
├── package.json
├── tsconfig.json
└── README.md
```

## Installation & Setup

### Prerequisites

- Node.js >= 18
- npm or yarn
- Android Studio with Android SDK
- Android TV emulator or physical Android TV device

### Step 1: Install Dependencies

```bash
npm install
# or
yarn install
```

### Step 2: Setup Android SDK

Make sure you have Android SDK installed and `ANDROID_HOME` environment variable set:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### Step 3: Run on Android TV

#### Using Android TV Emulator

1. Create an Android TV emulator in Android Studio (API 21+)
2. Start the emulator
3. Run the app:

```bash
npm run android
# or
yarn android
```

#### Using Physical Android TV Device

1. Enable Developer Options on your Android TV
2. Enable USB Debugging
3. Connect via USB or network ADB:

```bash
adb connect <TV_IP_ADDRESS>:5555
```

4. Run the app:

```bash
npm run android
# or
yarn android
```

### Step 4: Start Metro Bundler

```bash
npm start
# or
yarn start
```

## Configuration

### Customize Mosque Information

Edit [src/data/mockData.ts](src/data/mockData.ts):

```typescript
export const mockMasjidConfig: MasjidConfig = {
  name: 'Your Masjid Name',
  location: 'Your City, Country',
  tagline: 'Your Tagline',
  coordinates: {
    latitude: YOUR_LATITUDE,
    longitude: YOUR_LONGITUDE,
  },
  calculationMethod: 'Your Calculation Method',
};
```

### Customize Prayer Times

The app currently uses simplified prayer times. For production use with accurate astronomical calculations:

1. Install a prayer time calculation library like `adhan-js`
2. Update [src/utils/prayerTimes.ts](src/utils/prayerTimes.ts) to use the library
3. Configure calculation method, madhab, and high latitude rule

### Customize Treasury Data

Update treasury information in [src/data/mockData.ts](src/data/mockData.ts):

```typescript
export const mockKasData: KasData = {
  balance: YOUR_BALANCE,
  incomeMonth: YOUR_MONTHLY_INCOME,
  expenseMonth: YOUR_MONTHLY_EXPENSE,
  // ... other fields
};
```

### Customize Announcements

Edit announcements array in [src/data/mockData.ts](src/data/mockData.ts):

```typescript
export const mockAnnouncements: string[] = [
  'Your announcement 1',
  'Your announcement 2',
  // Add more announcements
];
```

## TV Remote Control

The app supports standard Android TV remote control:

- **Menu Button**: Toggle treasury detail overlay
- **Play/Pause**: Toggle between dashboard and prayer view (demo mode)
- **Back Button**: Close overlays / exit app
- **D-pad**: Navigate through settings (when implemented)

## Theme Customization

### Colors

Edit [src/theme/colors.ts](src/theme/colors.ts) to customize colors:

```typescript
export const colors = {
  accentPrimary: '#D4AF37',  // Gold
  accentSecondary: '#16A085', // Teal
  // ... other colors
};
```

### Typography

Adjust font sizes in [src/theme/typography.ts](src/theme/typography.ts):

```typescript
export const typography = {
  displayXL: {
    fontSize: 72,
    lineHeight: 80,
    // ... other properties
  },
  // ... other styles
};
```

### Ramadan Mode

To enable Ramadan mode, the app automatically detects Ramadan month. You can also manually enable it by importing and using `ramadanColors` from [src/theme/colors.ts](src/theme/colors.ts).

## Building for Production

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

2. Update [android/app/build.gradle](android/app/build.gradle) with your keystore info

3. Build signed APK:

```bash
cd android
./gradlew assembleRelease
```

## Future Enhancements

- [ ] Backend integration for real-time data updates
- [ ] Admin panel for remote configuration
- [ ] Multiple language support (Arabic, English, Indonesian)
- [ ] Quranic verse display
- [ ] Weather information
- [ ] Hadith of the day
- [ ] Settings screen with TV remote navigation
- [ ] QR code for donation
- [ ] Sound alerts for adzan time
- [ ] Custom calculation methods

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License.

## Credits

Developed based on comprehensive design specifications focusing on:
- Readability from 5-10 meters distance
- Calm, elegant, non-flashy design
- Single-glance clarity
- Minimal interaction, maximum information

## Support

For issues, questions, or suggestions, please open an issue on the GitHub repository.

---

**Made with ❤️ for Muslim communities**
