# 🚀 Quick Start Guide - Masjid Display

## Instalasi Cepat (5 Menit)

### 1. Install Dependencies
```bash
cd "c:\Users\rukiaja\Downloads\Rifqi masjid"
npm install
```
**Status:** ✅ DONE (943 packages installed)

### 2. Start Metro Bundler
```bash
npm start
```
**Expected Output:**
```
Welcome to Metro v0.80.12
Fast - Scalable - Integrated
```

### 3. Run on Android TV
**Option A - Emulator:**
```bash
npm run android
```

**Option B - Physical Device:**
```bash
adb connect <IP_TV>:5555
npm run android
```

---

## 📍 Lokasi Masjid

**Masjid Jami' Al-Hidayah**
Jl. Tanah Merdeka II No.8, Rambutan, Ciracas
Jakarta Timur 13830

Koordinat: -6.3140892, 106.8776666

---

## ✨ Fitur Utama

### 1. Jadwal Salat ⏰
- 5 waktu salat dengan adzan & iqamah
- Perhitungan akurat (Kemenag RI + library adhan)
- Countdown real-time
- Status visual (selesai/berlangsung/akan datang)

### 2. Konten Islami 📖
- **8 Ayat Al-Qur'an** (rotasi 40 detik)
- **10 Hadits Shahih** (rotasi 50 detik)
- Teks Arab + terjemahan Indonesia
- Animasi smooth

### 3. Info Pengajian 📅
- 8 program kajian rutin
- TPA, Tahfidz, Halaqah, Daurah
- Jadwal lengkap + pengajar

### 4. Kas Masjid 💰
- Saldo real-time
- Pemasukan/pengeluaran
- Transaksi terbaru
- Detail overlay (tekan Menu)

### 5. Pengumuman 📢
- Ticker berjalan
- Loop otomatis
- Mudah dibaca

---

## 🎮 Kontrol Remote TV

| Tombol | Fungsi |
|--------|--------|
| Menu | Toggle Kas Detail |
| Back | Tutup overlay / Keluar |
| (Future) D-pad | Navigasi settings |

---

## 🎨 Tampilan

```
┌─────────────────────────────────────────────────────────┐
│  🕌 MASJID JAMI' AL-HIDAYAH  │  14:25:30  │  📍 Status│
│  Tagline Masjid              │  Tanggal    │  📶 Online│
├──────────────┬──────────────────────┬──────────────────┤
│              │                      │                  │
│  JADWAL      │   📖 AYAT QUR'AN    │  📅 INFO KAJIAN │
│  SALAT       │                      │                  │
│              │   Teks Arab...       │  • Kajian Tafsir│
│  Subuh  ✓   │   Terjemahan...      │  • TPA Anak    │
│  Dzuhur ✓   │                      │  • Tahfidz      │
│  Ashar  🟡  │   ─────────────      │  • Halaqah      │
│  Maghrib 🔵 │                      │                  │
│  Isya   🔵  │   📜 HADITS          │  ──────────────│
│              │                      │                  │
│  ──────────  │   Teks Arab...       │  💰 KAS MASJID │
│              │   Terjemahan...      │                  │
│  SALAT       │   Perawi & Sumber    │  Rp 45.250.000 │
│  BERIKUTNYA  │                      │  ↑ Meningkat    │
│              │                      │                  │
│  Ashar       │                      │  Pemasukan: +  │
│  15:10 WIB   │                      │  Pengeluaran: -│
│  Dalam 35m   │                      │                  │
└──────────────┴──────────────────────┴──────────────────┘
│  📢 Pengumuman berjalan... Info 1 • Info 2 • Info 3... │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 Kustomisasi Cepat

### Update Pengumuman:
File: `src/data/mockData.ts`
```typescript
export const mockAnnouncements: string[] = [
  'Pengumuman 1',
  'Pengumuman 2',
  // tambahkan di sini
];
```

### Tambah Ayat Qur'an:
File: `src/data/islamicContent.ts`
```typescript
export const quranVerses: QuranVerse[] = [
  {
    id: '9',
    surah: 'Al-Fatihah',
    surahNumber: 1,
    ayah: 5,
    arabic: 'إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ',
    translation: 'Hanya kepada-Mu kami menyembah...',
  },
  // tambahkan ayat baru
];
```

### Tambah Kajian:
File: `src/data/islamicContent.ts`
```typescript
export const islamicStudies: IslamicStudy[] = [
  {
    id: '9',
    title: 'Kajian Baru',
    instructor: 'Ustadz...',
    schedule: 'Senin, 19:00 WIB',
    location: 'Ruang Utama',
    description: 'Deskripsi...',
    recurring: 'weekly',
    category: 'kajian',
  },
  // tambahkan kajian baru
];
```

### Update Kas:
File: `src/data/mockData.ts`
```typescript
export const mockKasData: KasData = {
  balance: 50000000, // ubah saldo
  incomeMonth: 30000000, // ubah pemasukan
  expenseMonth: 15000000, // ubah pengeluaran
  // ...
};
```

---

## 🔧 Troubleshooting

### Metro tidak start?
```bash
npx react-native start --reset-cache
```

### Aplikasi tidak muncul di TV?
```bash
# Cek koneksi adb
adb devices

# Reconnect
adb connect <IP_TV>:5555
```

### Build error?
```bash
# Clean & rebuild
cd android
./gradlew clean
cd ..
npm run android
```

### Waktu salat tidak sesuai?
Cek file: `src/utils/prayerTimesAdhan.ts`
- Pastikan koordinat benar
- Periksa fallback times

---

## 📚 Dokumentasi Lengkap

- **README.md** - Dokumentasi utama
- **TEST_GUIDE.md** - Panduan testing lengkap
- **FEATURE_SUMMARY.md** - Ringkasan fitur detail
- **QUICK_START.md** - Panduan ini

---

## ✅ Checklist Deployment

- [x] Install dependencies
- [x] Test Metro bundler
- [x] Konfigurasi lokasi masjid
- [x] Update pengumuman
- [x] Verifikasi waktu salat
- [x] Test di emulator
- [ ] Test di Android TV fisik
- [ ] Deploy ke TV masjid
- [ ] Training pengurus masjid
- [ ] Monitoring 24 jam pertama

---

## 🎯 Tips Penggunaan

1. **Biarkan aplikasi running 24/7** - Dirancang untuk continuous operation
2. **Update konten berkala** - Edit file data setiap minggu/bulan
3. **Monitor waktu salat** - Verifikasi akurasi setiap bulan
4. **Backup data kas** - Simpan transaksi secara terpisah
5. **Update pengumuman** - Sesuaikan dengan kegiatan masjid

---

## 📞 Support

Untuk pertanyaan atau issues:
1. Cek dokumentasi di folder project
2. Review TEST_GUIDE.md untuk troubleshooting
3. Catat error messages dari console
4. Screenshot masalah yang terjadi

---

**Siap digunakan! Bismillah 🕌✨**

Semoga bermanfaat untuk kemakmuran Masjid Jami' Al-Hidayah
Jakarta Timur

---

*Generated with ❤️ for Islamic Community*
