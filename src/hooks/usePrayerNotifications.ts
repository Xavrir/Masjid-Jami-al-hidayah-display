import { useState, useEffect, useRef } from 'react';
import { Prayer } from '../types';
import { soundNotificationService } from '../services/soundNotification';
import { format } from 'date-fns';

export type AlertType = 'adhan' | 'iqamah' | null;

interface PrayerAlert {
  type: AlertType;
  prayer: Prayer | null;
  timestamp: Date | null;
}

interface UsePrayerNotificationsReturn {
  currentAlert: PrayerAlert;
  isAdhanAlert: boolean;
  isIqamahAlert: boolean;
}

export const ADHAN_ALERT_DURATION_MS = 10000;
export const IQAMAH_ALERT_DURATION_MS = 15000;

export const usePrayerNotifications = (
  prayers: Prayer[],
  currentTime: Date
): UsePrayerNotificationsReturn => {
  const [currentAlert, setCurrentAlert] = useState<PrayerAlert>({
    type: null,
    prayer: null,
    timestamp: null,
  });

  const lastAdhanAlert = useRef<string>('');
  const lastIqamahAlert = useRef<string>('');
  const dismissTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    const currentTimeStr = format(currentTime, 'HH:mm');
    const currentDateStr = format(currentTime, 'yyyy-MM-dd');

    for (const prayer of prayers) {
      const prayerName = prayer.name.toLowerCase();
      if (
        prayerName === 'shuruq' ||
        prayerName === 'syuruq' ||
        prayerName === 'sunrise'
      ) {
        continue;
      }
      if (currentTimeStr === prayer.adhanTime) {
        const alertKey = `${currentDateStr}-${prayer.name}-${currentTimeStr}`;

        if (lastAdhanAlert.current !== alertKey) {
          lastAdhanAlert.current = alertKey;

          soundNotificationService.playAdhanAlert();

          setCurrentAlert({
            type: 'adhan',
            prayer: prayer,
            timestamp: new Date(),
          });

          if (dismissTimeout.current) {
            clearTimeout(dismissTimeout.current);
          }
          dismissTimeout.current = setTimeout(() => {
            setCurrentAlert({
              type: null,
              prayer: null,
              timestamp: null,
            });
          }, ADHAN_ALERT_DURATION_MS);

          if (__DEV__) {
            console.log(`Adhan alert triggered for ${prayer.name}`);
          }

          break;
        }
      }

      if (currentTimeStr === prayer.iqamahTime) {
        const alertKey = `${currentDateStr}-${prayer.name}-${currentTimeStr}`;

        if (lastIqamahAlert.current !== alertKey) {
          lastIqamahAlert.current = alertKey;

          soundNotificationService.playIqamahAlert();

          setCurrentAlert({
            type: 'iqamah',
            prayer: prayer,
            timestamp: new Date(),
          });

          if (dismissTimeout.current) {
            clearTimeout(dismissTimeout.current);
          }
          dismissTimeout.current = setTimeout(() => {
            setCurrentAlert({
              type: null,
              prayer: null,
              timestamp: null,
            });
          }, IQAMAH_ALERT_DURATION_MS);

          if (__DEV__) {
            console.log(`Iqamah alert triggered for ${prayer.name}`);
          }

          break;
        }
      }
    }
  }, [currentTime, prayers]);

  useEffect(() => {
    return () => {
      if (dismissTimeout.current) {
        clearTimeout(dismissTimeout.current);
      }
    };
  }, []);

  return {
    currentAlert,
    isAdhanAlert: currentAlert.type === 'adhan',
    isIqamahAlert: currentAlert.type === 'iqamah',
  };
};
