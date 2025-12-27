import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  StatusBar,
  ImageBackground,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { NextPrayerCard } from '../components/NextPrayerCard';
import { AnnouncementTicker } from '../components/AnnouncementTicker';
import { QuranVerseCard } from '../components/QuranVerseCard';
import { HadithCard } from '../components/HadithCard';
import {
  calculatePrayerTimesForJakarta,
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
  const [currentPrayer, setCurrentPrayer] = useState<Prayer | null>(null);
  const [isNextPrayerTomorrow, setIsNextPrayerTomorrow] = useState(false);

  // Initialize prayer times
  useEffect(() => {
    const today = new Date();
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
    if (prayers.length === 0) return;

    const updatedPrayers = updatePrayerStatuses(prayers, currentTime);
    setPrayers(updatedPrayers);

    // Check if all prayers have passed, if so use tomorrow's prayers
    const allPassed = allPrayersPassed(updatedPrayers);
    const next = getNextPrayer(
      updatedPrayers,
      allPassed ? tomorrowPrayers : undefined
    );
    const current = getCurrentPrayer(updatedPrayers);

    setNextPrayer(next);
    setIsNextPrayerTomorrow(allPassed && next !== null);

    // If all prayers passed, show tomorrow's schedule in the compact view
    if (allPassed && tomorrowPrayers.length > 0) {
      // Update compact prayer times to show tomorrow's schedule
      // We'll keep today's prayers in the state but display tomorrow's in UI
    }

    if (current && current.name !== currentPrayer?.name) {
      setCurrentPrayer(current);
      onPrayerStart?.(current);
    } else if (!current) {
      setCurrentPrayer(null);
    }
  }, [currentTime, tomorrowPrayers]);

  const isRamadanPeriod = checkIsRamadan(currentTime);

  const announcementsWithKas = [
    ...announcements,
    `Kas Masjid - Saldo: ${formatCurrency(kasData.balance)} | Pemasukan Bulan Ini: ${formatCurrency(kasData.incomeMonth)} | Pengeluaran Bulan Ini: ${formatCurrency(kasData.expenseMonth)}`,
  ];

  const displayPrayers =
    allPrayersPassed(prayers) && tomorrowPrayers.length > 0
      ? tomorrowPrayers
      : prayers;

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
              {displayPrayers.map((prayer, index) => {
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
