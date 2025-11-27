import { Prayer, PrayerStatus } from '../types';
import { format, addMinutes, differenceInMinutes, isBefore, isAfter } from 'date-fns';

/**
 * Calculate prayer times based on coordinates
 * This is a simplified implementation. For production, use a library like adhan-js
 */
export const calculatePrayerTimes = (
  date: Date,
  latitude: number,
  longitude: number
): Prayer[] => {
  // Simplified prayer times - in production, use proper astronomical calculations
  // This example uses Jakarta, Indonesia approximate times
  const baseTimes = {
    Subuh: { adhan: '04:45', iqamah: '05:00' },
    Dzuhur: { adhan: '12:05', iqamah: '12:20' },
    Ashar: { adhan: '15:15', iqamah: '15:30' },
    Maghrib: { adhan: '18:10', iqamah: '18:15' },
    Isya: { adhan: '19:25', iqamah: '19:40' },
  };

  const prayers: Prayer[] = Object.entries(baseTimes).map(([name, times]) => ({
    name,
    adhanTime: times.adhan,
    iqamahTime: times.iqamah,
    status: 'upcoming' as PrayerStatus,
  }));

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

    // Determine end time (15 minutes after iqamah or next prayer's adhan)
    const nextPrayer = prayers[index + 1];
    let endMinutes: number;

    if (nextPrayer) {
      const [nextAdhanHour, nextAdhanMin] = nextPrayer.adhanTime.split(':').map(Number);
      endMinutes = nextAdhanHour * 60 + nextAdhanMin;
    } else {
      endMinutes = iqamahMinutes + 15;
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
 * Get Hijri date (simplified - in production use proper Hijri calendar library)
 */
export const getHijriDate = (date: Date): string => {
  // Simplified approximation
  // In production, use a proper Hijri calendar library like moment-hijri or date-fns-jalali
  const gregorianYear = date.getFullYear();
  const hijriYear = Math.floor((gregorianYear - 622) * 1.030684);

  const months = [
    'Muharram', 'Safar', 'Rabi\' al-awwal', 'Rabi\' al-thani',
    'Jumada al-awwal', 'Jumada al-thani', 'Rajab', 'Sha\'ban',
    'Ramadan', 'Shawwal', 'Dhu al-Qi\'dah', 'Dhu al-Hijjah'
  ];

  const monthIndex = date.getMonth();

  return `${date.getDate()} ${months[monthIndex]} ${hijriYear} H`;
};

/**
 * Check if current date is in Ramadan
 */
export const isRamadan = (date: Date): boolean => {
  const hijriDate = getHijriDate(date);
  return hijriDate.includes('Ramadan');
};
