import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  ImageBackground,
  Linking,
  StatusBar,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { NextPrayerCard } from '../components/NextPrayerCard';
import { AnnouncementTicker } from '../components/AnnouncementTicker';
import { QuranVerseCard } from '../components/QuranVerseCard';
import { HadithCard } from '../components/HadithCard';
import { PrayerAlertBanner } from '../components/PrayerAlertBanner';
import { usePrayerNotifications } from '../hooks/usePrayerNotifications';
import {
  calculatePrayerTimesForJakarta,
  calculateShuruqTimeForJakarta,
  updatePrayerStatuses,
  getNextPrayer,
  getCurrentPrayer,
  getHijriDate,
  isRamadan as checkIsRamadan,
  allPrayersPassed,
} from '../utils/prayerTimesAdhan';
import { formatGregorianDate, formatTimeWithSeconds } from '../utils/dateTime';
import { formatCurrency } from '../utils/currency';
import { Prayer, KasData, MasjidConfig } from '../types';
import { soundNotificationService } from '../services/soundNotification';

interface MainDashboardProps {
  masjidConfig: MasjidConfig;
  kasData: KasData;
  announcements: string[];
  onPrayerStart?: (prayer: Prayer) => void;
}

export const MainDashboard: React.FC<MainDashboardProps> = ({
  masjidConfig,
  kasData,
  announcements,
  onPrayerStart,
}) => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [prayers, setPrayers] = useState<Prayer[]>([]);
  const [tomorrowPrayers, setTomorrowPrayers] = useState<Prayer[]>([]);
  const [nextPrayer, setNextPrayer] = useState<Prayer | null>(null);
  const [isNextPrayerTomorrow, setIsNextPrayerTomorrow] = useState(false);

  const { currentAlert } = usePrayerNotifications(prayers, currentTime);
  const currentPrayerNameRef = useRef<string | null>(null);

  const [debugAlert, setDebugAlert] = useState<{
    type: 'adhan' | 'iqamah' | null;
    prayer: Prayer | null;
  }>({
    type: null,
    prayer: null,
  });

  const dismissTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const lastTriggerRef = useRef<{ url: string; ts: number } | null>(null);
  const prayersForLookupRef = useRef<Prayer[]>([]);

  useEffect(() => {
    prayersForLookupRef.current = prayers;
  }, [prayers]);

  useEffect(() => {
    return () => {
      if (dismissTimeoutRef.current) {
        clearTimeout(dismissTimeoutRef.current);
        dismissTimeoutRef.current = null;
      }
      soundNotificationService.cleanup();
    };
  }, []);

  useEffect(() => {
    if (!__DEV__) {
      return;
    }

    const clearDebugAlert = () => {
      if (dismissTimeoutRef.current) {
        clearTimeout(dismissTimeoutRef.current);
        dismissTimeoutRef.current = null;
      }

      setDebugAlert({
        type: null,
        prayer: null,
      });
    };

    const safeDecode = (value: string) => {
      try {
        return decodeURIComponent(value);
      } catch (_error) {
        return value;
      }
    };

    const parseQuery = (query: string): Record<string, string> => {
      const params: Record<string, string> = {};
      if (!query) {
        return params;
      }

      for (const part of query.split('&')) {
        if (!part) {
          continue;
        }
        const [rawKey, rawValue = ''] = part.split('=');
        const key = safeDecode(rawKey);
        if (!key) {
          continue;
        }
        params[key] = safeDecode(rawValue);
      }

      return params;
    };

    const resolvePrayerName = (value: string) => {
      if (!value) {
        return '';
      }
      if (value === 'subuh' || value === 'fajr') {
        return 'subuh';
      }
      if (
        value === 'dzuhur' ||
        value === 'dzhuhur' ||
        value === 'dhuhr' ||
        value === 'zuhur'
      ) {
        return 'dzuhur';
      }
      if (value === 'ashar' || value === 'asr') {
        return 'ashar';
      }
      if (value === 'maghrib') {
        return 'maghrib';
      }
      if (value === 'isya' || value === 'isha') {
        return 'isya';
      }
      return value;
    };

    const handleBeepDeepLink = (url: string) => {
      const match = url.match(
        /^([a-zA-Z0-9+.-]+):\/\/([^/?#]+)(?:\/[^?#]*)?(?:\?([^#]*))?/
      );
      if (!match) {
        return;
      }

      const scheme = match[1];
      const host = match[2];
      if (scheme !== 'masjiddisplay' || host !== 'beep') {
        return;
      }

      const params = parseQuery(match[3] ?? '');
      const rawTypeParam = (params.type ?? '').toLowerCase();
      const target = (params.target ?? '').toLowerCase();
      const jsFlag = (params.js ?? '').toLowerCase();

      const isJsTarget =
        target === 'js' ||
        jsFlag === '1' ||
        jsFlag === 'true' ||
        rawTypeParam.startsWith('js_') ||
        rawTypeParam.startsWith('js-') ||
        rawTypeParam.endsWith('_js') ||
        rawTypeParam.endsWith('-js') ||
        rawTypeParam.includes('_js_') ||
        rawTypeParam.includes('-js-');

      if (!isJsTarget) {
        return;
      }

      const normalizedTypeParam = rawTypeParam
        .replace(/^js[_-]/, '')
        .replace(/[_-]js$/, '')
        .replace(/[_-]js[_-]/g, '_');

      const parts = normalizedTypeParam.split(/[_-]+/).filter(Boolean);
      const typePart = parts[0] ?? '';
      const type: 'adhan' | 'iqamah' =
        typePart === 'iqamah' ? 'iqamah' : 'adhan';

      const prayerFromType = parts.slice(1).join('_');
      const requestedPrayerRaw = String(
        params.prayer ?? params.p ?? prayerFromType ?? ''
      ).trim();
      const requestedPrayer = requestedPrayerRaw.toLowerCase();

      if (
        requestedPrayer === 'shuruq' ||
        requestedPrayer === 'syuruq' ||
        requestedPrayer === 'sunrise'
      ) {
        return;
      }

      const now = Date.now();
      if (
        lastTriggerRef.current &&
        lastTriggerRef.current.url === url &&
        now - lastTriggerRef.current.ts < 800
      ) {
        return;
      }
      lastTriggerRef.current = { url, ts: now };

      const normalizedPrayer = resolvePrayerName(requestedPrayer);
      const overridePrayer = normalizedPrayer
        ? (prayersForLookupRef.current.find(
            (p: Prayer) => p.name.toLowerCase() === normalizedPrayer
          ) ??
          prayersForLookupRef.current.find((p: Prayer) =>
            p.name.toLowerCase().includes(normalizedPrayer)
          ))
        : null;

      const prayerForBanner: Prayer = overridePrayer ?? {
        name: requestedPrayerRaw || 'Debug',
        adhanTime: '00:00',
        iqamahTime: '00:00',
        status: 'upcoming',
      };

      if (type === 'adhan') {
        soundNotificationService.playAdhanAlert();
      } else {
        soundNotificationService.playIqamahAlert();
      }

      setDebugAlert({
        type,
        prayer: prayerForBanner,
      });

      if (dismissTimeoutRef.current) {
        clearTimeout(dismissTimeoutRef.current);
      }

      const durationMs = type === 'adhan' ? 10_000 : 15_000;
      dismissTimeoutRef.current = setTimeout(() => {
        clearDebugAlert();
      }, durationMs);

      console.log(
        `[MasjidDisplay] JS deep-link alert triggered: type=${type}, prayer=${prayerForBanner.name}`
      );
    };

    const subscription = Linking.addEventListener(
      'url',
      (event: { url?: string }) => {
        if (event?.url) {
          handleBeepDeepLink(event.url);
        }
      }
    );

    Linking.getInitialURL()
      .then((initialUrl: string | null) => {
        if (initialUrl) {
          handleBeepDeepLink(initialUrl);
        }
      })
      .catch((error: unknown) => {
        if (__DEV__) {
          console.debug('[MasjidDisplay] getInitialURL failed', error);
        }
      });

    return () => {
      subscription.remove();
      clearDebugAlert();
    };
  }, []);

  // Initialize prayer times
  useEffect(() => {
    const now = new Date();
    const today = new Date(now);
    today.setHours(0, 0, 0, 0);

    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const initialPrayers = calculatePrayerTimesForJakarta(today);
    const initialTomorrowPrayers = calculatePrayerTimesForJakarta(tomorrow);

    setPrayers(initialPrayers);
    setTomorrowPrayers(initialTomorrowPrayers);
  }, []);

  // Update time every second
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  // Update prayer statuses when time changes
  useEffect(() => {
    setPrayers(previousPrayers => {
      if (previousPrayers.length === 0) {
        return previousPrayers;
      }

      return updatePrayerStatuses(previousPrayers, currentTime);
    });
  }, [currentTime]);

  const tomorrowPrayersLive = useMemo(() => {
    if (tomorrowPrayers.length === 0) {
      return tomorrowPrayers;
    }

    const tomorrowReference = new Date(currentTime);
    tomorrowReference.setDate(tomorrowReference.getDate() + 1);

    return updatePrayerStatuses(
      tomorrowPrayers,
      currentTime,
      tomorrowReference
    );
  }, [currentTime, tomorrowPrayers]);

  useEffect(() => {
    if (prayers.length === 0) {
      setNextPrayer(null);
      setIsNextPrayerTomorrow(false);
      currentPrayerNameRef.current = null;
      return;
    }

    const allPassedToday = allPrayersPassed(prayers);

    const next = getNextPrayer(
      prayers,
      allPassedToday ? tomorrowPrayersLive : undefined
    );

    const current = getCurrentPrayer(prayers);

    setNextPrayer(next);
    setIsNextPrayerTomorrow(allPassedToday && next !== null);

    if (current && currentPrayerNameRef.current !== current.name) {
      currentPrayerNameRef.current = current.name;
      onPrayerStart?.(current);
    } else if (!current) {
      currentPrayerNameRef.current = null;
    }
  }, [currentTime, onPrayerStart, prayers, tomorrowPrayersLive]);

  const isRamadanPeriod = checkIsRamadan(currentTime);

  const announcementsWithKas = [
    ...announcements,
    `Kas Masjid - Saldo: ${formatCurrency(kasData.balance)} | Pemasukan Bulan Ini: ${formatCurrency(kasData.incomeMonth)} | Pengeluaran Bulan Ini: ${formatCurrency(kasData.expenseMonth)}`,
  ];

  const isShowingTomorrowSchedule =
    allPrayersPassed(prayers) && tomorrowPrayers.length > 0;

  const displayPrayers = isShowingTomorrowSchedule
    ? tomorrowPrayersLive
    : prayers;

  const scheduleDayKey = `${currentTime.getFullYear()}-${String(
    currentTime.getMonth() + 1
  ).padStart(2, '0')}-${String(currentTime.getDate()).padStart(2, '0')}`;

  const shuruqTimeForSchedule = useMemo(() => {
    const [year, month, day] = scheduleDayKey.split('-').map(Number);
    const referenceDate = new Date(year, month - 1, day);
    referenceDate.setHours(0, 0, 0, 0);

    if (isShowingTomorrowSchedule) {
      referenceDate.setDate(referenceDate.getDate() + 1);
    }

    return calculateShuruqTimeForJakarta(referenceDate);
  }, [isShowingTomorrowSchedule, scheduleDayKey]);

  const displayPrayersCompact = (() => {
    if (displayPrayers.length === 0) {
      return displayPrayers;
    }

    const shuruqPrayer: Prayer = {
      name: 'Shuruq',
      adhanTime: shuruqTimeForSchedule,
      iqamahTime: '—',
      status: 'upcoming',
    };

    const subuhIndex = displayPrayers.findIndex(
      prayer => prayer.name.toLowerCase() === 'subuh'
    );

    if (subuhIndex === -1) {
      return [shuruqPrayer, ...displayPrayers];
    }

    return [
      ...displayPrayers.slice(0, subuhIndex + 1),
      shuruqPrayer,
      ...displayPrayers.slice(subuhIndex + 1),
    ];
  })();

  const alertToShow = debugAlert.type ? debugAlert : currentAlert;

  return (
    <View style={styles.container}>
      <StatusBar hidden />

      <ImageBackground
        source={require('../assets/images/kaaba-background.jpg')}
        style={styles.backgroundImage}
        resizeMode="cover">
        <LinearGradient
          colors={['rgba(5, 15, 24, 0.92)', 'rgba(5, 15, 24, 0.95)']}
          style={styles.gradient}>
          {alertToShow.prayer && alertToShow.type && (
            <PrayerAlertBanner
              type={alertToShow.type}
              prayer={alertToShow.prayer}
            />
          )}
          {/* Header with Clock and Integrated Prayer Times */}
          <View style={styles.headerSection}>
            <View style={styles.headerTop}>
              <View style={styles.headerLeft}>
                <View style={styles.accentBar} />
                <View>
                  <Text style={styles.masjidName}>
                    {masjidConfig.name.toUpperCase()}
                  </Text>
                  <Text style={styles.tagline}>{masjidConfig.tagline}</Text>
                </View>
              </View>

              <View style={styles.headerCenter}>
                <Text style={styles.currentTime}>
                  {formatTimeWithSeconds(currentTime)}
                </Text>
                <Text style={styles.gregorianDate}>
                  {formatGregorianDate(currentTime)}
                </Text>
                <Text style={styles.hijriDate}>
                  {getHijriDate(currentTime)}
                </Text>
              </View>

              <View style={styles.headerRight}>
                {isRamadanPeriod && (
                  <View style={[styles.badge, styles.ramadanBadge]}>
                    <View style={styles.badgeDot} />
                    <Text style={styles.badgeText}>Ramadan Kareem</Text>
                  </View>
                )}
              </View>
            </View>

            {/* Compact Prayer Schedule - Below Clock */}
            <View style={styles.prayerTimesCompact}>
              {displayPrayersCompact.map(prayer => {
                const isTheNextPrayer = nextPrayer?.name === prayer.name;
                return (
                  <View
                    key={prayer.name}
                    style={[
                      styles.prayerTimeItem,
                      prayer.status === 'current' &&
                        styles.prayerTimeItemActive,
                      isTheNextPrayer && styles.prayerTimeItemNext,
                    ]}>
                    <Text
                      style={[
                        styles.prayerTimeName,
                        prayer.status === 'current' &&
                          styles.prayerTimeNameActive,
                      ]}>
                      {prayer.name}
                    </Text>
                    <Text
                      style={[
                        styles.prayerTimeValue,
                        prayer.status === 'current' &&
                          styles.prayerTimeValueActive,
                      ]}>
                      {prayer.adhanTime}
                    </Text>
                    <Text style={styles.prayerIqamah}>{prayer.iqamahTime}</Text>
                  </View>
                );
              })}
            </View>
          </View>

          <View style={styles.coreContent}>
            <View style={styles.coreColumn}>
              <NextPrayerCard
                prayer={nextPrayer}
                isTomorrow={isNextPrayerTomorrow}
              />
            </View>
            <View style={styles.coreColumn}>
              <QuranVerseCard autoRotate rotationInterval={40000} />
            </View>
            <View style={styles.coreColumn}>
              <HadithCard autoRotate rotationInterval={50000} />
            </View>
          </View>

          <View style={styles.tickerContainer}>
            <AnnouncementTicker
              announcements={announcementsWithKas}
              speed="slow"
            />
          </View>
        </LinearGradient>
      </ImageBackground>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  backgroundImage: {
    flex: 1,
  },
  gradient: {
    flex: 1,
    paddingTop: spacing.lg,
    paddingBottom: spacing.lg,
    paddingHorizontal: spacing.xxl,
  },
  // Header Section - Integrated with Prayer Times
  headerSection: {
    marginBottom: spacing.lg,
  },

  headerTop: {
    height: 80,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: spacing.lg,
    marginBottom: spacing.xl,
  },
  headerLeft: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
  accentBar: {
    width: 4,
    height: 40,
    backgroundColor: colors.accentPrimary,
    marginRight: spacing.md,
  },
  masjidName: {
    ...typography.headlineM,
    color: colors.textPrimary,
    letterSpacing: 2,
    marginBottom: spacing.xs,
  },
  tagline: {
    ...typography.bodyS,
    color: colors.textSecondary,
    opacity: 0.9,
    fontStyle: 'italic',
  },
  headerCenter: {
    flex: 1,
    alignItems: 'center',
  },
  currentTime: {
    ...typography.displayL,
    color: colors.textPrimary,
    marginBottom: spacing.xs,
  },
  gregorianDate: {
    ...typography.bodyM,
    color: colors.textSecondary,
    marginBottom: spacing.xs,
  },
  hijriDate: {
    ...typography.bodyS,
    color: colors.accentPrimary,
  },
  headerRight: {
    flex: 1,
    alignItems: 'flex-end',
  },
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(21, 32, 43, 0.7)',
    borderRadius: radii.small,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    marginBottom: spacing.sm,
    borderWidth: 1,
    borderColor: 'rgba(212, 175, 55, 0.2)',
  },
  badgeDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: colors.accentPrimary,
    marginRight: spacing.sm,
  },
  ramadanBadge: {
    backgroundColor: 'rgba(212, 175, 55, 0.15)',
    borderColor: colors.accentPrimary,
  },
  badgeText: {
    ...typography.caption,
    color: colors.textSecondary,
    fontWeight: '600',
  },
  // Compact Prayer Times - Below Clock
  prayerTimesCompact: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: spacing.lg,
    gap: spacing.sm,
  },
  prayerTimeItem: {
    flex: 1,
    backgroundColor: 'rgba(21, 32, 43, 0.6)',
    borderRadius: radii.small,
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.sm,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(212, 175, 55, 0.15)',
    minHeight: 72,
  },
  prayerTimeItemActive: {
    backgroundColor: 'rgba(212, 175, 55, 0.25)',
    borderColor: colors.accentPrimary,
    borderWidth: 2,
  },
  prayerTimeItemNext: {
    borderColor: colors.accentPrimary,
    borderWidth: 1,
  },
  prayerTimeName: {
    ...typography.bodyS,
    color: colors.textSecondary,
    marginBottom: spacing.xs,
    fontWeight: '600',
    fontSize: 13,
  },
  prayerTimeNameActive: {
    color: colors.accentPrimary,
  },
  prayerTimeValue: {
    fontSize: 20,
    fontWeight: '700',
    color: colors.textPrimary,
    marginBottom: 2,
  },
  prayerTimeValueActive: {
    color: colors.accentPrimary,
  },
  prayerIqamah: {
    ...typography.caption,
    color: colors.textMuted,
    fontSize: 11,
  },
  coreContent: {
    flex: 1,
    minHeight: 0,
    flexDirection: 'row',
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xxl,
    alignItems: 'stretch',
  },
  coreColumn: {
    flex: 1,
    minHeight: 0,
  },
  tickerContainer: {
    paddingHorizontal: spacing.lg,
  },
});
