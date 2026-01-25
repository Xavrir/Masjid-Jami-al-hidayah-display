# Supabase Integration Guide for Masjid Display

## Overview
This application uses Supabase to manage and display dynamic content for mosque displays including:
- **Kas Masjid** (Treasury Management)
- **Ayat Quran** (Quranic Verses)
- **Hadits** (Islamic Teachings)
- **Pengajian** (Teaching Schedule)

## Supabase Configuration

### Database URL
```
https://wqupptqjbkuldglnpvor.supabase.co
```

### Tables to Create

#### 1. kas_masjid (Treasury Data)
Stores mosque treasury information and transactions.

```sql
CREATE TABLE IF NOT EXISTS kas_masjid (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    balance BIGINT NOT NULL DEFAULT 0,
    income_month BIGINT NOT NULL DEFAULT 0,
    expense_month BIGINT NOT NULL DEFAULT 0,
    trend_direction VARCHAR(20) DEFAULT 'FLAT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Sample Data:**
```json
{
    "balance": 45250000,
    "income_month": 28500000,
    "expense_month": 12750000,
    "trend_direction": "UP"
}
```

#### 2. ayat_quran (Quranic Verses)
Stores Quranic verses for daily display rotation.

```sql
CREATE TABLE IF NOT EXISTS ayat_quran (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    surah VARCHAR(100) NOT NULL,
    surah_number INTEGER NOT NULL,
    ayah INTEGER NOT NULL,
    arabic TEXT NOT NULL,
    translation TEXT NOT NULL,
    transliteration TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Sample Data:**
```json
{
    "surah": "Al-Baqarah",
    "surah_number": 2,
    "ayah": 186,
    "arabic": "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ",
    "translation": "And when My servants ask you (Muhammad) about Me, I am indeed near",
    "transliteration": "Wa idza sa-alaka 'ibaadii 'annii fa-innii qariib"
}
```

#### 3. hadits (Islamic Teachings)
Stores Hadith (Islamic traditions) for display rotation.

```sql
CREATE TABLE IF NOT EXISTS hadits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    narrator VARCHAR(255) NOT NULL,
    arabic TEXT NOT NULL,
    translation TEXT NOT NULL,
    source VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Sample Data:**
```json
{
    "narrator": "Abu Hurairah RA",
    "arabic": "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
    "translation": "The best among you are those who learn the Quran and teach it",
    "source": "HR. Bukhari",
    "category": "Keutamaan Ilmu"
}
```

#### 4. pengajian (Teaching Schedule)
Stores information about Islamic teachings schedules.

```sql
CREATE TABLE IF NOT EXISTS pengajian (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    judul VARCHAR(255) NOT NULL,
    pembicara VARCHAR(255) NOT NULL,
    jam VARCHAR(10) NOT NULL,
    hari VARCHAR(50) NOT NULL,
    lokasi VARCHAR(255) NOT NULL,
    deskripsi TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Sample Data:**
```json
{
    "judul": "Kajian Fikih Praktis",
    "pembicara": "Ustadz Ahmad Abdullah",
    "jam": "19:00",
    "hari": "Senin",
    "lokasi": "Ruang Utama Masjid",
    "deskripsi": "Pembahasan hukum-hukum fikih yang praktis dalam kehidupan sehari-hari"
}
```

## Setup Steps

### 1. Create Tables in Supabase
1. Log in to your Supabase dashboard
2. Go to SQL Editor
3. Copy and paste each CREATE TABLE statement above
4. Execute each statement to create the tables

### 2. Add Sample Data
1. Go to the Table Editor in Supabase
2. For each table (kas_masjid, ayat_quran, hadits, pengajian)
3. Click "Insert new row" or use the bulk upload feature
4. Add the sample data provided above

### 3. Enable RLS (Row Level Security) - Optional but Recommended
1. Go to Authentication > Policies in Supabase
2. Create a policy that allows anonymous SELECT on all tables
3. Restrict INSERT/UPDATE/DELETE to authenticated users only

### 4. Configure API Key in Android App
The app uses the Supabase public API key (anon key) which is already configured in:
- File: `SupabaseConfig.kt`
- Constant: `SUPABASE_URL` and `SUPABASE_KEY`

## Running Text Display

The app automatically displays content in a rotating loop on the bottom of the screen:

1. **Announcements** - From MockData.announcements (6 seconds each)
2. **Quran Verses** - From ayat_quran table (10 seconds each)
3. **Hadiths** - From hadits table (10 seconds each)
4. **Pengajian** - From pengajian table (8 seconds each)

The display cycles through all content continuously during the day.

## Data Modification

### To Update Kas Masjid Data:
In Supabase:
1. Go to Table Editor > kas_masjid
2. Click the row to edit balance, income_month, expense_month, and trend_direction
3. Changes are reflected on the display within seconds

### To Add New Quran Verses:
1. Go to Table Editor > ayat_quran
2. Click "Insert new row"
3. Fill in: surah, surah_number, ayah, arabic, translation, transliteration
4. The verse will appear in the rotation within the update interval

### To Add New Hadiths:
1. Go to Table Editor > hadits
2. Click "Insert new row"
3. Fill in: narrator, arabic, translation, source, category
4. The hadith will appear in the rotation

### To Add New Pengajian (Teaching Schedule):
1. Go to Table Editor > pengajian
2. Click "Insert new row"
3. Fill in: judul, pembicara, jam, hari, lokasi, deskripsi
4. The schedule will appear in the ticker

## Troubleshooting

### Content Not Loading
- Check network connectivity
- Verify Supabase URL and API key in SupabaseConfig.kt
- Check Supabase status page
- Look at app logs (logcat) for error messages

### Tables Not Visible
- Ensure tables are created in Supabase
- Check that RLS policies allow SELECT access
- Verify table names match exactly (lowercase)

### Data Not Updating
- Refresh the app (close and reopen)
- Check that data was correctly inserted in Supabase
- Verify no syntax errors in the data

## API Endpoints Used

The app uses Supabase REST API v1:
- `GET /rest/v1/kas_masjid` - Fetch treasury data
- `GET /rest/v1/ayat_quran` - Fetch Quranic verses
- `GET /rest/v1/hadits` - Fetch Hadiths
- `GET /rest/v1/pengajian` - Fetch teaching schedules

## Dependencies

The app includes the following dependencies for Supabase integration:
- Retrofit 2.9.0 - HTTP client
- Gson 2.10.1 - JSON serialization
- OkHttp 4.11.0 - HTTP client
- Coroutines - Async operations

## Performance Notes

- Data is fetched once when the app starts
- Caching is done in-memory
- For large datasets, consider adding pagination parameters
- The running text display updates smoothly without blocking the UI
