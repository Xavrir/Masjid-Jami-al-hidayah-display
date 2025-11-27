import { Prayer, PrayerStatus } from '../types';
import { format, differenceInMinutes } from 'date-fns';

/**
 * Calculate prayer times using adhan library for accurate astronomical calculations
 * Based on Kemenag RI calculation method
 */

// Note: This requires the 'adhan' package to be installed
// We'll use a polyfill approach that works in React Native

interface PrayerTime {
  name: string;
  time: Date;
}

/**
 * Calculate prayer times for Jakarta Timur, Indonesia
 * Using Kemenag RI parameters
 */
export const calculatePrayerTimesForJakarta = (date: Date): Prayer[] => {
  const latitude = -6.3140892;
  const longitude = 106.8776666;

  try {
    // Dynamic import for adhan (if available)
    const { Coordinates, CalculationMethod, PrayerTimes: AdhanPrayerTimes } = require('adhan');

    const coordinates = new Coordinates(latitude, longitude);
    const params = CalculationMethod.Other();

    // Kemenag RI parameters
    params.fajrAngle = 20.0;
    params.ishaAngle = 18.0;
    params.method = 'Kemenag';

    const prayerTimes = new AdhanPrayerTimes(coordinates, date, params);

    const prayers: Prayer[] = [
      {
        name: 'Subuh',
        adhanTime: format(prayerTimes.fajr, 'HH:mm'),
        iqamahTime: format(new Date(prayerTimes.fajr.getTime() + 15 * 60000), 'HH:mm'),
        status: 'upcoming' as PrayerStatus,
      },
      {
        name: 'Dzuhur',
        adhanTime: format(prayerTimes.dhuhr, 'HH:mm'),
        iqamahTime: format(new Date(prayerTimes.dhuhr.getTime() + 15 * 60000), 'HH:mm'),
        status: 'upcoming' as PrayerStatus,
      },
      {
        name: 'Ashar',
        adhanTime: format(prayerTimes.asr, 'HH:mm'),
        iqamahTime: format(new Date(prayerTimes.asr.getTime() + 15 * 60000), 'HH:mm'),
        status: 'upcoming' as PrayerStatus,
      },
      {
        name: 'Maghrib',
        adhanTime: format(prayerTimes.maghrib, 'HH:mm'),
        iqamahTime: format(new Date(prayerTimes.maghrib.getTime() + 5 * 60000), 'HH:mm'),
        status: 'upcoming' as PrayerStatus,
      },
      {
        name: 'Isya',
        adhanTime: format(prayerTimes.isha, 'HH:mm'),
        iqamahTime: format(new Date(prayerTimes.isha.getTime() + 15 * 60000), 'HH:mm'),
        status: 'upcoming' as PrayerStatus,
      },
    ];

    return updatePrayerStatuses(prayers, date);
  } catch (error) {
    console.warn('Adhan library not available, using fallback times', error);
    return getFallbackPrayerTimes(date);
  }
};

/**
 * Fallback prayer times if adhan library is not available
 * Approximate times for Jakarta Timur
 */
const getFallbackPrayerTimes = (date: Date): Prayer[] => {
  const prayers: Prayer[] = [
    {
      name: 'Subuh',
      adhanTime: '04:25',
      iqamahTime: '04:40',
      status: 'upcoming' as PrayerStatus,
    },
    {
      name: 'Dzuhur',
      adhanTime: '11:55',
      iqamahTime: '12:10',
      status: 'upcoming' as PrayerStatus,
    },
    {
      name: 'Ashar',
      adhanTime: '15:10',
      iqamahTime: '15:25',
      status: 'upcoming' as PrayerStatus,
    },
    {
      name: 'Maghrib',
      adhanTime: '18:00',
      iqamahTime: '18:05',
      status: 'upcoming' as PrayerStatus,
    },
    {
      name: 'Isya',
      adhanTime: '19:10',
      iqamahTime: '19:25',
      status: 'upcoming' as PrayerStatus,
    },
  ];

  return updatePrayerStatuses(prayers, date);
};

/**
 * Update prayer statuses based on current time
 */
export const updatePrayerStatuses = (prayers: Prayer[], currentTime: Date): Prayer[] => {
  const currentMinutes = currentTime.getHours() * 60 + currentTime.getMinutes();

  let foundCurrent = false;

  return prayers.map((prayer, index) => {
    const [adhanHour, adhanMin] = prayer.adhanTime.split(':').map(Number);
    const [iqamahHour, iqamahMin] = prayer.iqamahTime.split(':').map(Number);

    const adhanMinutes = adhanHour * 60 + adhanMin;
    const iqamahMinutes = iqamahHour * 60 + iqamahMin;

    // Determine end time (next prayer's adhan or 30 minutes after iqamah)
    const nextPrayer = prayers[index + 1];
    let endMinutes: number;

    if (nextPrayer) {
      const [nextAdhanHour, nextAdhanMin] = nextPrayer.adhanTime.split(':').map(Number);
      endMinutes = nextAdhanHour * 60 + nextAdhanMin;
    } else {
      endMinutes = iqamahMinutes + 30;
    }

    let status: PrayerStatus;
    let countdown: string | undefined;

    if (currentMinutes >= adhanMinutes && currentMinutes < endMinutes && !foundCurrent) {
      status = 'current';
      foundCurrent = true;
      const remainingMinutes = endMinutes - currentMinutes;
      countdown = formatCountdown(remainingMinutes);
    } else if (currentMinutes < adhanMinutes) {
      status = 'upcoming';
      const remainingMinutes = adhanMinutes - currentMinutes;
      countdown = formatCountdown(remainingMinutes);
    } else {
      status = 'passed';
    }

    return {
      ...prayer,
      status,
      countdown,
    };
  });
};

/**
 * Get the next upcoming prayer
 */
export const getNextPrayer = (prayers: Prayer[]): Prayer | null => {
  const upcoming = prayers.find(p => p.status === 'upcoming');
  return upcoming || null;
};

/**
 * Get the current prayer in progress
 */
export const getCurrentPrayer = (prayers: Prayer[]): Prayer | null => {
  const current = prayers.find(p => p.status === 'current');
  return current || null;
};

/**
 * Format countdown time
 */
export const formatCountdown = (minutes: number): string => {
  if (minutes < 0) return '--:--';

  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;

  if (hours > 0) {
    return `${hours}j ${mins}m`;
  }
  return `${mins}m`;
};

/**
 * Format time for display
 */
export const formatTime = (date: Date): string => {
  return format(date, 'HH:mm:ss');
};

/**
 * Get Hijri date using simplified conversion
 * For production, use a proper Hijri calendar library
 */
export const getHijriDate = (date: Date): string => {
  // Simplified Hijri conversion
  const gregorianYear = date.getFullYear();
  const gregorianMonth = date.getMonth() + 1;
  const gregorianDay = date.getDate();

  // Approximate conversion (not accurate, use proper library for production)
  const hijriYear = Math.floor((gregorianYear - 622) * 1.030684);

  const hijriMonths = [
    'Muharram', 'Safar', 'Rabi\'ul Awwal', 'Rabi\'ul Akhir',
    'Jumadil Awwal', 'Jumadil Akhir', 'Rajab', 'Sya\'ban',
    'Ramadhan', 'Syawwal', 'Dzulqa\'dah', 'Dzulhijjah'
  ];

  // Approximate month (for display purposes)
  const hijriMonth = hijriMonths[gregorianMonth - 1];
  const hijriDay = gregorianDay; // Simplified

  return `${hijriDay} ${hijriMonth} ${hijriYear} H`;
};

/**
 * Check if current date is in Ramadan
 * This is a simplified check, use proper Islamic calendar library for accuracy
 */
export const isRamadan = (date: Date): boolean => {
  const hijriDate = getHijriDate(date);
  return hijriDate.includes('Ramadhan');
};
