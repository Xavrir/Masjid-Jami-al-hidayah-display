import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  StatusBar,
  Dimensions,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii, safeAreaMargins } from '../theme/spacing';
import { PrayerRow } from '../components/PrayerRow';
import { NextPrayerCard } from '../components/NextPrayerCard';
import { KasSummary } from '../components/KasSummary';
import { AnnouncementTicker } from '../components/AnnouncementTicker';
import {
  calculatePrayerTimes,
  updatePrayerStatuses,
  getNextPrayer,
  getCurrentPrayer,
  getHijriDate,
  isRamadan as checkIsRamadan,
} from '../utils/prayerTimes';
import { formatGregorianDate, formatTimeWithSeconds } from '../utils/dateTime';
import { Prayer, KasData, MasjidConfig } from '../types';

const { width, height } = Dimensions.get('window');

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
  const [nextPrayer, setNextPrayer] = useState<Prayer | null>(null);
  const [currentPrayer, setCurrentPrayer] = useState<Prayer | null>(null);

  // Initialize prayer times
  useEffect(() => {
    const initialPrayers = calculatePrayerTimes(
      new Date(),
      masjidConfig.coordinates.latitude,
      masjidConfig.coordinates.longitude
    );
    setPrayers(initialPrayers);
  }, [masjidConfig.coordinates]);

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

    const next = getNextPrayer(updatedPrayers);
    const current = getCurrentPrayer(updatedPrayers);

    setNextPrayer(next);

    if (current && current !== currentPrayer) {
      setCurrentPrayer(current);
      onPrayerStart?.(current);
    } else if (!current) {
      setCurrentPrayer(null);
    }
  }, [currentTime]);

  const isRamadanPeriod = checkIsRamadan(currentTime);

  return (
    <View style={styles.container}>
      <StatusBar hidden />

      <LinearGradient
        colors={[colors.backgroundGradientTop, colors.backgroundGradientBottom]}
        style={styles.gradient}
      >
        {/* Header with Clock and Integrated Prayer Times */}
        <View style={styles.headerSection}>
          <View style={styles.headerTop}>
            <View style={styles.headerLeft}>
              <View style={styles.accentBar} />
              <View>
                <Text style={styles.masjidName}>{masjidConfig.name.toUpperCase()}</Text>
                <Text style={styles.location}>{masjidConfig.location}</Text>
              </View>
            </View>

            <View style={styles.headerCenter}>
              <Text style={styles.currentTime}>{formatTimeWithSeconds(currentTime)}</Text>
              <Text style={styles.gregorianDate}>{formatGregorianDate(currentTime)}</Text>
              <Text style={styles.hijriDate}>{getHijriDate(currentTime)}</Text>
            </View>

            <View style={styles.headerRight}>
              <View style={styles.badge}>
                <View style={styles.badgeDot} />
                <Text style={styles.badgeText}>{masjidConfig.calculationMethod}</Text>
              </View>
              <View style={styles.badge}>
                <View style={[styles.badgeDot, { backgroundColor: '#22c55e' }]} />
                <Text style={styles.badgeText}>ONLINE</Text>
              </View>
            </View>
          </View>

          {/* Compact Prayer Schedule - Below Clock */}
          <View style={styles.prayerTimesCompact}>
            {prayers.map((prayer, index) => (
              <View key={prayer.name} style={[
                styles.prayerTimeItem,
                prayer.status === 'current' && styles.prayerTimeItemActive,
                prayer.status === 'upcoming' && styles.prayerTimeItemNext,
              ]}>
                <Text style={[
                  styles.prayerTimeName,
                  prayer.status === 'current' && styles.prayerTimeNameActive,
                ]}>{prayer.name}</Text>
                <Text style={[
                  styles.prayerTimeValue,
                  prayer.status === 'current' && styles.prayerTimeValueActive,
                ]}>{prayer.adhanTime}</Text>
                <Text style={styles.prayerIqamah}>{prayer.iqamahTime}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* Core Content */}
        <View style={styles.coreContent}>
          {/* Next Prayer Card */}
          <View style={styles.leftColumn}>
            <NextPrayerCard prayer={nextPrayer} />
          </View>

          {/* Right Column - Info Cards */}
          <View style={styles.rightColumn}>
            <KasSummary kasData={kasData} variant="compact_with_sparkline" />
          </View>
        </View>

        {/* Ticker */}
        <View style={styles.tickerContainer}>
          <AnnouncementTicker announcements={announcements} speed="slow" />
        </View>
      </LinearGradient>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
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
    marginBottom: spacing.md,
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
  location: {
    ...typography.bodyS,
    color: colors.textSecondary,
    opacity: 0.8,
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
    backgroundColor: colors.surfaceGlass,
    borderRadius: radii.small,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    marginBottom: spacing.sm,
    borderWidth: 1,
    borderColor: colors.divider,
  },
  badgeDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: colors.accentPrimary,
    marginRight: spacing.sm,
  },
  badgeText: {
    ...typography.bodyS,
    color: colors.textSecondary,
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
    backgroundColor: colors.surfaceGlass,
    borderRadius: radii.small,
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.sm,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: colors.divider,
    minHeight: 72,
  },
  prayerTimeItemActive: {
    backgroundColor: 'rgba(212, 175, 55, 0.2)',
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
    flexDirection: 'row',
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
    marginBottom: spacing.md,
    maxHeight: 200,
  },
  leftColumn: {
    flex: 0.5,
  },
  rightColumn: {
    flex: 0.5,
  },
  tickerContainer: {
    paddingHorizontal: spacing.lg,
  },
});
