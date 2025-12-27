export interface QuranVerse {
  id: string;
  surah: string;
  surahNumber: number;
  ayah: number;
  arabic: string;
  translation: string;
  transliteration?: string;
}

export interface Hadith {
  id: string;
  narrator: string;
  arabic: string;
  translation: string;
  source: string;
  category: string;
}

export const quranVerses: QuranVerse[] = [
  {
    id: '1',
    surah: 'Al-Baqarah',
    surahNumber: 2,
    ayah: 186,
    arabic:
      'وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ',
    translation:
      'Dan apabila hamba-hamba-Ku bertanya kepadamu (Muhammad) tentang Aku, maka sesungguhnya Aku dekat. Aku mengabulkan permohonan orang yang berdoa apabila dia berdoa kepada-Ku.',
    transliteration:
      "Wa idza sa-alaka 'ibaadii 'annii fa-innii qariib, ujiibu da'watad-daa'i idza da'aan",
  },
  {
    id: '2',
    surah: "Ali 'Imran",
    surahNumber: 3,
    ayah: 159,
    arabic: 'فَاعْفُ عَنْهُمْ وَاسْتَغْفِرْ لَهُمْ وَشَاوِرْهُمْ فِي الْأَمْرِ',
    translation:
      'Maka maafkanlah mereka dan mohonkanlah ampunan untuk mereka, dan bermusyawarahlah dengan mereka dalam urusan itu.',
    transliteration: "Fa'fu 'anhum wastaghfir lahum wa syawirhum fil-amr",
  },
  {
    id: '3',
    surah: 'An-Nisa',
    surahNumber: 4,
    ayah: 86,
    arabic:
      'وَإِذَا حُيِّيتُم بِتَحِيَّةٍ فَحَيُّوا بِأَحْسَنَ مِنْهَا أَوْ رُدُّوهَا',
    translation:
      'Apabila kamu diberi salam, maka balaslah dengan yang lebih baik, atau balaslah (dengan salam yang serupa).',
    transliteration:
      'Wa idza huyyiitum bi-tahiyyatin fa-hayyuu bi-ahsana minha aw rudduuhaa',
  },
  {
    id: '4',
    surah: 'Al-Hujurat',
    surahNumber: 49,
    ayah: 13,
    arabic: 'إِنَّ أَكْرَمَكُمْ عِندَ اللَّهِ أَتْقَاكُمْ',
    translation:
      'Sesungguhnya yang paling mulia di antara kamu di sisi Allah ialah orang yang paling bertakwa.',
    transliteration: "Inna akramakum 'indallahi atqaakum",
  },
  {
    id: '5',
    surah: 'Al-Mujadilah',
    surahNumber: 58,
    ayah: 11,
    arabic:
      'يَرْفَعِ اللَّهُ الَّذِينَ آمَنُوا مِنكُمْ وَالَّذِينَ أُوتُوا الْعِلْمَ دَرَجَاتٍ',
    translation:
      'Allah akan mengangkat derajat orang-orang yang beriman di antaramu dan orang-orang yang diberi ilmu beberapa derajat.',
    transliteration:
      "Yarfa'illahul-ladziina aamanuu minkum wal-ladziina uutul-'ilma darajaat",
  },
  {
    id: '6',
    surah: 'Al-Insyirah',
    surahNumber: 94,
    ayah: 5,
    arabic: 'فَإِنَّ مَعَ الْعُسْرِ يُسْرًا',
    translation: 'Maka sesungguhnya bersama kesulitan ada kemudahan.',
    transliteration: "Fa-inna ma'al-'usri yusraa",
  },
  {
    id: '7',
    surah: 'Al-Isra',
    surahNumber: 17,
    ayah: 23,
    arabic:
      'وَقَضَىٰ رَبُّكَ أَلَّا تَعْبُدُوا إِلَّا إِيَّاهُ وَبِالْوَالِدَيْنِ إِحْسَانًا',
    translation:
      'Dan Tuhanmu telah memerintahkan agar kamu jangan menyembah selain Dia dan hendaklah berbuat baik kepada ibu bapak.',
    transliteration:
      "Wa qadaa rabbuka allaa ta'buduu illaa iyyaahu wa bil-waalidayni ihsaanaa",
  },
  {
    id: '8',
    surah: 'Luqman',
    surahNumber: 31,
    ayah: 14,
    arabic: 'وَوَصَّيْنَا الْإِنسَانَ بِوَالِدَيْهِ',
    translation:
      'Dan Kami perintahkan kepada manusia (agar berbuat baik) kepada kedua orang tuanya.',
    transliteration: 'Wa wassaynal-insaana bi-waalidayh',
  },
];

export const hadiths: Hadith[] = [
  {
    id: '1',
    narrator: 'Abu Hurairah RA',
    arabic: 'خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ',
    translation:
      "Sebaik-baik kalian adalah yang mempelajari Al-Qur'an dan mengajarkannya.",
    source: 'HR. Bukhari',
    category: 'Keutamaan Ilmu',
  },
  {
    id: '2',
    narrator: 'Abu Hurairah RA',
    arabic:
      'مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ',
    translation:
      'Barangsiapa menempuh jalan untuk mencari ilmu, maka Allah akan memudahkan baginya jalan menuju surga.',
    source: 'HR. Muslim',
    category: 'Keutamaan Ilmu',
  },
  {
    id: '3',
    narrator: 'Anas bin Malik RA',
    arabic:
      'لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ',
    translation:
      'Tidaklah beriman salah seorang dari kalian sampai ia mencintai untuk saudaranya apa yang ia cintai untuk dirinya sendiri.',
    source: 'HR. Bukhari & Muslim',
    category: 'Akhlak',
  },
  {
    id: '4',
    narrator: 'Abu Hurairah RA',
    arabic:
      'الْمُؤْمِنُ الْقَوِيُّ خَيْرٌ وَأَحَبُّ إِلَى اللَّهِ مِنَ الْمُؤْمِنِ الضَّعِيفِ',
    translation:
      'Mukmin yang kuat lebih baik dan lebih dicintai Allah daripada mukmin yang lemah.',
    source: 'HR. Muslim',
    category: 'Motivasi',
  },
  {
    id: '5',
    narrator: 'Abdullah bin Amr RA',
    arabic:
      'الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ ارْحَمُوا مَنْ فِي الْأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ',
    translation:
      'Orang-orang yang pengasih akan dikasihi oleh Yang Maha Pengasih. Sayangilah siapa saja yang ada di bumi, niscaya Dia yang di langit akan menyayangimu.',
    source: 'HR. Abu Daud & Tirmidzi',
    category: 'Akhlak',
  },
  {
    id: '6',
    narrator: 'Abu Hurairah RA',
    arabic:
      'إِنَّ اللَّهَ تَعَالَى قَالَ مَنْ عَادَى لِي وَلِيًّا فَقَدْ آذَنْتُهُ بِالْحَرْبِ',
    translation:
      "Sesungguhnya Allah Ta'ala berfirman: Barangsiapa memusuhi wali-Ku, maka Aku umumkan perang kepadanya.",
    source: 'HR. Bukhari',
    category: 'Peringatan',
  },
  {
    id: '7',
    narrator: "Abu Musa Al-Asy'ari RA",
    arabic: 'الْمُؤْمِنُ لِلْمُؤْمِنِ كَالْبُنْيَانِ يَشُدُّ بَعْضُهُ بَعْضًا',
    translation:
      'Mukmin dengan mukmin lainnya seperti sebuah bangunan, saling menguatkan satu sama lain.',
    source: 'HR. Bukhari & Muslim',
    category: 'Ukhuwah',
  },
  {
    id: '8',
    narrator: 'Abu Hurairah RA',
    arabic:
      'تُنْكَحُ الْمَرْأَةُ لِأَرْبَعٍ لِمَالِهَا وَلِحَسَبِهَا وَجَمَالِهَا وَلِدِينِهَا فَاظْفَرْ بِذَاتِ الدِّينِ تَرِبَتْ يَدَاكَ',
    translation:
      'Wanita dinikahi karena empat hal: karena hartanya, keturunannya, kecantikannya, dan agamanya. Maka pilihlah yang beragama, niscaya kamu akan beruntung.',
    source: 'HR. Bukhari & Muslim',
    category: 'Kehidupan',
  },
  {
    id: '9',
    narrator: 'Umar bin Khattab RA',
    arabic: 'إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ',
    translation: 'Sesungguhnya setiap amalan tergantung pada niatnya.',
    source: 'HR. Bukhari & Muslim',
    category: 'Niat & Ikhlas',
  },
  {
    id: '10',
    narrator: 'Abu Hurairah RA',
    arabic:
      'مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الْآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ',
    translation:
      'Barangsiapa yang beriman kepada Allah dan hari akhir, hendaklah ia berkata baik atau diam.',
    source: 'HR. Bukhari & Muslim',
    category: 'Akhlak',
  },
];

// Helper function to get random Islamic content
export const getRandomQuranVerse = (): QuranVerse => {
  return quranVerses[Math.floor(Math.random() * quranVerses.length)];
};

export const getRandomHadith = (): Hadith => {
  return hadiths[Math.floor(Math.random() * hadiths.length)];
};
