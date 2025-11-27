# 📁 File Structure - Masjid Display

## Complete Project Structure

```
Rifqi masjid/
│
├── 📱 Application Core
│   ├── index.js                    # App entry point
│   ├── app.json                    # App configuration
│   ├── package.json                # Dependencies & scripts
│   ├── tsconfig.json              # TypeScript configuration
│   ├── babel.config.js            # Babel configuration
│   ├── metro.config.js            # Metro bundler config
│   ├── .eslintrc.js               # Linting rules
│   ├── .prettierrc.js             # Code formatting
│   ├── .gitignore                 # Git ignore patterns
│   └── .watchmanconfig            # Watchman configuration
│
├── 📂 src/
│   │
│   ├── App.tsx                    # ⭐ Main App component
│   │
│   ├── 🖥️ screens/
│   │   ├── MainDashboardEnhanced.tsx   # ⭐⭐ MAIN SCREEN (3-column layout)
│   │   ├── MainDashboard.tsx           # Original backup
│   │   └── PrayerInProgress.tsx        # Prayer in progress view
│   │
│   ├── 🧩 components/
│   │   ├── PrayerRow.tsx               # Prayer time row display
│   │   ├── NextPrayerCard.tsx          # Next prayer highlight card
│   │   ├── KasSummary.tsx              # Treasury summary display
│   │   ├── KasDetailOverlay.tsx        # Detailed treasury overlay
│   │   ├── AnnouncementTicker.tsx      # Scrolling announcements
│   │   ├── QuranVerseCard.tsx          # 📖 NEW: Quran verse display
│   │   ├── HadithCard.tsx              # 📜 NEW: Hadith display
│   │   └── IslamicStudiesCard.tsx      # 📅 NEW: Kajian info display
│   │
│   ├── 🎨 theme/
│   │   ├── colors.ts                   # Color palette & variants
│   │   ├── typography.ts               # Text styles (12-72px)
│   │   ├── spacing.ts                  # Layout spacing & radii
│   │   ├── motion.ts                   # Animation timings
│   │   └── index.ts                    # Theme exports
│   │
│   ├── 📊 types/
│   │   └── index.ts                    # TypeScript definitions
│   │
│   ├── 🛠️ utils/
│   │   ├── prayerTimes.ts              # Original prayer calculations
│   │   ├── prayerTimesAdhan.ts         # ⭐ Accurate adhan calculations
│   │   ├── dateTime.ts                 # Date formatting utilities
│   │   └── currency.ts                 # Currency formatting (IDR)
│   │
│   └── 💾 data/
│       ├── mockData.ts                 # ⭐ Mosque & kas data (EDIT HERE)
│       └── islamicContent.ts           # ⭐ Quran, Hadith, Kajian data (EDIT HERE)
│
├── 🤖 android/
│   ├── build.gradle                # Project build configuration
│   ├── settings.gradle             # Project settings
│   ├── gradle.properties           # Gradle properties
│   │
│   └── app/
│       ├── build.gradle            # App build configuration
│       ├── proguard-rules.pro      # ProGuard rules
│       │
│       └── src/main/
│           ├── AndroidManifest.xml     # ⭐ TV manifest configuration
│           │
│           ├── java/com/masjiddisplay/
│           │   ├── MainActivity.kt     # Main activity
│           │   └── MainApplication.kt  # Application class
│           │
│           └── res/
│               └── values/
│                   ├── strings.xml     # App strings
│                   └── styles.xml      # App styles
│
└── 📚 Documentation/
    ├── README.md                        # ⭐⭐⭐ Main documentation
    ├── TEST_GUIDE.md                    # Complete testing guide
    ├── FEATURE_SUMMARY.md               # Detailed features list
    ├── QUICK_START.md                   # Quick start guide
    ├── PROJECT_COMPLETION_REPORT.md     # Completion report
    └── FILE_STRUCTURE.md                # This file
```

---

## 🔑 Key Files to Know

### For Daily Use

| File | Purpose | Edit Frequency |
|------|---------|----------------|
| `src/data/mockData.ts` | Announcements, kas data | Daily/Weekly |
| `src/data/islamicContent.ts` | Quran, Hadith, Kajian | Monthly |
| `android/app/src/main/res/values/strings.xml` | App name | Once |

### For Configuration

| File | Purpose | Edit Frequency |
|------|---------|----------------|
| `src/utils/prayerTimesAdhan.ts` | Prayer time adjustments | Rarely |
| `src/theme/colors.ts` | Color theme | Rarely |
| `src/theme/typography.ts` | Font sizes | Rarely |
| `package.json` | Dependencies | As needed |

### For Development

| File | Purpose | Edit Frequency |
|------|---------|----------------|
| `src/screens/MainDashboardEnhanced.tsx` | Main screen layout | For new features |
| `src/components/*.tsx` | Component behavior | For new features |
| `src/App.tsx` | App navigation | For new screens |

---

## 📝 What Each File Does

### Application Core Files

**index.js**
- Entry point that registers the app
- Imports `src/App.tsx`
- Registers app with React Native

**app.json**
```json
{
  "name": "MasjidDisplay",
  "displayName": "Masjid Display"
}
```

**package.json**
- Lists all dependencies (943 packages)
- Defines scripts: `npm start`, `npm run android`
- Specifies Node.js version requirement

**tsconfig.json**
- TypeScript compiler configuration
- Sets path aliases (@/*)
- Enables strict type checking

---

### Source Code (src/)

#### **App.tsx** ⭐ MAIN ENTRY
```typescript
// What it does:
// 1. Manages screen state (dashboard/prayer-in-progress)
// 2. Handles prayer start events
// 3. Controls Kas overlay visibility
// 4. Provides data to screens
```

#### **screens/MainDashboardEnhanced.tsx** ⭐⭐ CORE SCREEN
```typescript
// Layout:
// ┌──────────────────────────────────────┐
// │          HEADER (120px)              │
// ├──────────┬─────────────┬─────────────┤
// │ Prayer   │ Islamic     │ Kajian &    │
// │ Times    │ Content     │ Kas Info    │
// │ (32%)    │ (34%)       │ (34%)       │
// │          │             │             │
// │ • Table  │ • Quran     │ • Studies   │
// │ • Next   │ • Hadith    │ • Treasury  │
// └──────────┴─────────────┴─────────────┘
// │       TICKER (72px)                  │
// └──────────────────────────────────────┘
```

#### **screens/PrayerInProgress.tsx**
```typescript
// Full-screen prayer view
// Triggered when prayer starts
// Shows: Name, countdown, timeline
```

---

### Components (src/components/)

| Component | Rotation | Purpose |
|-----------|----------|---------|
| PrayerRow | - | Display one prayer time |
| NextPrayerCard | - | Highlight next prayer |
| QuranVerseCard | 40s | Show Quran verse |
| HadithCard | 50s | Show Hadith |
| IslamicStudiesCard | - | List kajian schedule |
| KasSummary | - | Show kas balance |
| KasDetailOverlay | Manual | Detailed kas view |
| AnnouncementTicker | Continuous | Scroll announcements |

---

### Theme Files (src/theme/)

**colors.ts**
```typescript
// Defines color palette:
// - background: #020712 (dark)
// - accentPrimary: #D4AF37 (gold)
// - accentSecondary: #16A085 (teal)
// - textPrimary: #FFFFFF (white)
// Also includes Ramadan variant
```

**typography.ts**
```typescript
// 11 text styles from 12px → 72px:
// - displayXL: 72px (main clock)
// - displayL: 56px (salat names)
// - headlineXL: 40px (big titles)
// ...
// - caption: 12px (small labels)
```

**spacing.ts**
```typescript
// Spacing scale: 4 → 40px
// Border radii: 8 → 24px
// Safe area margins: 40-60px
```

**motion.ts**
```typescript
// Animation durations: 80ms → 650ms
// Easing curves: standard, emphasized
```

---

### Utils (src/utils/)

**prayerTimesAdhan.ts** ⭐ IMPORTANT
```typescript
// Functions:
// - calculatePrayerTimesForJakarta(date)
// - updatePrayerStatuses(prayers, time)
// - getNextPrayer(prayers)
// - getCurrentPrayer(prayers)
// - formatCountdown(minutes)
// - getHijriDate(date)
// - isRamadan(date)

// Uses 'adhan' library with:
// - Coordinates: -6.3140892, 106.8776666
// - Fajr angle: 20°, Isha angle: 18°
// - Fallback times if library fails
```

**dateTime.ts**
```typescript
// Format dates in Indonesian:
// - formatGregorianDate(): "Selasa, 26 November 2025"
// - formatTimeWithSeconds(): "14:25:30"
```

**currency.ts**
```typescript
// Format currency:
// - formatCurrency(45250000) → "Rp 45.250.000"
```

---

### Data Files (src/data/)

**mockData.ts** ⭐ EDIT HERE FOR DAILY UPDATES
```typescript
// Contains:
// 1. mockMasjidConfig
//    - name: "Masjid Jami' Al-Hidayah"
//    - location: "Jl. Tanah Merdeka II No.8..."
//    - coordinates: -6.3140892, 106.8776666
//    - calculationMethod: "Kemenag RI"
//
// 2. mockKasData
//    - balance: 45250000
//    - incomeMonth: 28500000
//    - expenseMonth: 12750000
//    - trendDirection: "up"
//    - recentTransactions: [...]
//
// 3. mockAnnouncements
//    - Array of announcement strings
```

**islamicContent.ts** ⭐ EDIT HERE FOR ISLAMIC CONTENT
```typescript
// Contains:
// 1. quranVerses: QuranVerse[]     (8 verses)
// 2. hadiths: Hadith[]             (10 hadiths)
// 3. islamicStudies: IslamicStudy[] (8 programs)
//
// Helper functions:
// - getRandomQuranVerse()
// - getRandomHadith()
// - getTodayStudies()
// - getUpcomingStudies()
```

---

### Android Files (android/)

**app/src/main/AndroidManifest.xml** ⭐ TV CONFIGURATION
```xml
<!-- Key features: -->
<uses-feature android:name="android.software.leanback" required="true" />
<uses-feature android:name="android.hardware.touchscreen" required="false" />

<!-- Appears in TV launcher: -->
<category android:name="android.intent.category.LEANBACK_LAUNCHER" />
```

**app/build.gradle**
```gradle
// Package name: com.masjiddisplay
// Min SDK: 21 (Android 5.0)
// Target SDK: 34 (Android 14)
```

---

## 🎯 Quick Reference: What to Edit

### Add New Announcement
**File:** `src/data/mockData.ts`
```typescript
export const mockAnnouncements: string[] = [
  'Your new announcement here',
  // existing announcements...
];
```

### Add New Quran Verse
**File:** `src/data/islamicContent.ts`
```typescript
export const quranVerses: QuranVerse[] = [
  {
    id: '9',
    surah: 'Surah Name',
    surahNumber: 1,
    ayah: 1,
    arabic: 'Arabic text',
    translation: 'Indonesian translation',
    transliteration: 'Optional transliteration',
  },
  // existing verses...
];
```

### Add New Hadith
**File:** `src/data/islamicContent.ts`
```typescript
export const hadiths: Hadith[] = [
  {
    id: '11',
    narrator: 'Narrator name',
    arabic: 'Arabic text',
    translation: 'Indonesian translation',
    source: 'HR. Source',
    category: 'Category name',
  },
  // existing hadiths...
];
```

### Add New Kajian
**File:** `src/data/islamicContent.ts`
```typescript
export const islamicStudies: IslamicStudy[] = [
  {
    id: '9',
    title: 'Kajian Title',
    instructor: 'Ustadz Name',
    schedule: 'Day, Time',
    location: 'Location',
    description: 'Description',
    recurring: 'weekly' | 'daily' | 'monthly',
    category: 'kajian' | 'tahfidz' | 'tpa' | 'halaqah' | 'daurah',
  },
  // existing studies...
];
```

### Update Kas Balance
**File:** `src/data/mockData.ts`
```typescript
export const mockKasData: KasData = {
  balance: 50000000, // Update here
  incomeMonth: 30000000, // Update here
  expenseMonth: 15000000, // Update here
  // ...
};
```

### Add Kas Transaction
**File:** `src/data/mockData.ts`
```typescript
export const mockKasTransactions: KasTransaction[] = [
  {
    id: '7',
    date: '2025-11-27',
    description: 'Transaction description',
    amount: 1000000, // Positive for income
    type: 'income', // or 'expense'
  },
  // existing transactions...
];
```

---

## 📦 Build Outputs

After building, you'll find:

```
android/app/build/outputs/
└── apk/
    ├── debug/
    │   └── app-debug.apk           # Debug build
    └── release/
        └── app-release.apk         # Production build ⭐
```

**Production APK** can be installed directly on Android TV:
```bash
adb install android/app/build/outputs/apk/release/app-release.apk
```

---

## 🔍 Finding Things Quickly

**Need to change prayer times?**
→ `src/utils/prayerTimesAdhan.ts`

**Need to change colors?**
→ `src/theme/colors.ts`

**Need to change font sizes?**
→ `src/theme/typography.ts`

**Need to add announcements?**
→ `src/data/mockData.ts` → `mockAnnouncements`

**Need to add Quran verses?**
→ `src/data/islamicContent.ts` → `quranVerses`

**Need to add Hadiths?**
→ `src/data/islamicContent.ts` → `hadiths`

**Need to change layout?**
→ `src/screens/MainDashboardEnhanced.tsx`

**Need to change mosque info?**
→ `src/data/mockData.ts` → `mockMasjidConfig`

---

## 📊 File Statistics

```
Total Project Files: 45+
TypeScript Files: 25
Configuration Files: 10
Android Files: 5
Documentation Files: 6

Total Lines of Code: ~5,000+
Total Lines of Docs: ~2,200+
```

---

**Need help finding a file?**
Use your code editor's search (Ctrl+P in VS Code) and type the filename!

---

*This file structure represents the complete Masjid Display application ready for deployment.*
