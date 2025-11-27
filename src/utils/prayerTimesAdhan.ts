import { Prayer, PrayerStatus } from '../types';
import { format, differenceInMinutes } from 'date-fns';

const DEFAULT_PRAYER_WINDOW_MINUTES = 20;
const MIN_PRAYER_WINDOW_MINUTES = 5;
const MAX_PRAYER_WINDOW_MINUTES = 60;

const getWindowMinutes = (
  adhanDate: Date,
  iqamahDate: Date,
  override?: number
): number => {
  const derived = differenceInMinutes(iqamahDate, adhanDate);
  const base = override ?? (derived > 0 ? derived : DEFAULT_PRAYER_WINDOW_MINUTES);
  return Math.min(MAX_PRAYER_WINDOW_MINUTES, Math.max(MIN_PRAYER_WINDOW_MINUTES, base));
};

export const getDateFromTimeString = (time: string, reference: Date): Date => {
  const [hour, minute] = time.split(':').map(Number);
  const result = new Date(reference);
  result.setHours(hour);
  result.setMinutes(minute);
  result.setSeconds(0);
  result.setMilliseconds(0);
  return result;
};

export const getPrayerWindowBounds = (
  prayer: Prayer,
  referenceDate: Date = new Date()
): { start: Date; end: Date; iqamahDate: Date; durationMinutes: number } => {
  const start = getDateFromTimeString(prayer.adhanTime, referenceDate);
  const iqamahDate = getDateFromTimeString(prayer.iqamahTime, referenceDate);
  const durationMinutes = getWindowMinutes(start, iqamahDate, prayer.windowMinutes);
  const end = new Date(start.getTime() + durationMinutes * 60 * 1000);

  return { start, end, iqamahDate, durationMinutes };
};

export const isWithinPrayerWindow = (
  prayer: Prayer,
  currentTime: Date,
  referenceDate: Date = currentTime
): boolean => {
  const { start, end } = getPrayerWindowBounds(prayer, referenceDate);
  return currentTime >= start && currentTime < end;
};

export const getPrayerPhase = (
  prayer: Prayer,
  currentTime: Date,
  referenceDate: Date = currentTime
): 'adzan' | 'iqamah' => {
  const { iqamahDate } = getPrayerWindowBounds(prayer, referenceDate);
  return currentTime >= iqamahDate ? 'iqamah' : 'adzan';
};

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
    ].map((prayer) => {
      const { durationMinutes } = getPrayerWindowBounds(prayer, date);
      return {
        ...prayer,
        windowMinutes: durationMinutes,
      };
    });

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
  ].map((prayer) => {
    const start = getDateFromTimeString(prayer.adhanTime, date);
    const iqamahDate = getDateFromTimeString(prayer.iqamahTime, date);
    return {
      ...prayer,
      windowMinutes: getWindowMinutes(start, iqamahDate),
    };
  });

  return updatePrayerStatuses(prayers, date);
};

/**
 * Update prayer statuses based on current time
 */
export const updatePrayerStatuses = (prayers: Prayer[], currentTime: Date): Prayer[] => {
  let foundCurrent = false;

  return prayers.map((prayer) => {
    const { start, end, durationMinutes } = getPrayerWindowBounds(prayer, currentTime);

    let status: PrayerStatus;
    let countdown: string | undefined;

    if (!foundCurrent && currentTime >= start && currentTime < end) {
      status = 'current';
      foundCurrent = true;
      const remainingMinutes = Math.max(
        0,
        Math.ceil((end.getTime() - currentTime.getTime()) / (60 * 1000))
      );
      countdown = formatCountdown(remainingMinutes);
    } else if (currentTime < start) {
      status = 'upcoming';
      const remainingMinutes = Math.max(0, differenceInMinutes(start, currentTime));
      countdown = formatCountdown(remainingMinutes);
    } else {
      status = 'passed';
    }

    return {
      ...prayer,
      status,
      countdown,
      windowMinutes: durationMinutes,
    };
  });
};

/**
 * Get the next upcoming prayer
 * If all prayers have passed, fallback to tomorrow's prayers
 */
export const getNextPrayer = (prayers: Prayer[], tomorrowPrayers?: Prayer[]): Prayer | null => {
  const upcoming = prayers.find(p => p.status === 'upcoming');

  // If no upcoming prayer today and tomorrow's prayers are provided
  if (!upcoming && tomorrowPrayers && tomorrowPrayers.length > 0) {
    // Return first prayer of tomorrow (Subuh)
    return tomorrowPrayers[0];
  }

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
 * Check if all prayers for the day have passed
 */
export const allPrayersPassed = (prayers: Prayer[]): boolean => {
  return prayers.every(p => p.status === 'passed');
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
