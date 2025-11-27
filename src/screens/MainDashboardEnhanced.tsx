import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  StatusBar,
  ScrollView,
  Dimensions,
  ImageBackground,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii, safeAreaMargins } from '../theme/spacing';
import { PrayerRow } from '../components/PrayerRow';
import { NextPrayerCard } from '../components/NextPrayerCard';
import { KasSummary } from '../components/KasSummary';
import { AnnouncementTicker } from '../components/AnnouncementTicker';
import { QuranVerseCard } from '../components/QuranVerseCard';
import { HadithCard } from '../components/HadithCard';
import { IslamicStudiesCard } from '../components/IslamicStudiesCard';
import {
  calculatePrayerTimesForJakarta,
  updatePrayerStatuses,
  getNextPrayer,
  getCurrentPrayer,
  getHijriDate,
  isRamadan as checkIsRamadan,
} from '../utils/prayerTimesAdhan';
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
    const initialPrayers = calculatePrayerTimesForJakarta(new Date());
    setPrayers(initialPrayers);
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

    const next = getNextPrayer(updatedPrayers);
    const current = getCurrentPrayer(updatedPrayers);

    setNextPrayer(next);

    if (current && current.name !== currentPrayer?.name) {
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

      <ImageBackground
        source={require('../assets/images/kaaba-background.jpg')}
        style={styles.backgroundImage}
        resizeMode="cover"
      >
        <LinearGradient
          colors={['rgba(5, 15, 24, 0.92)', 'rgba(5, 15, 24, 0.95)']}
          style={styles.gradient}
        >
        {/* Header */}
        <View style={styles.header}>
          <View style={styles.headerLeft}>
            <View style={styles.accentBar} />
            <View>
              <Text style={styles.masjidName}>{masjidConfig.name.toUpperCase()}</Text>
              <Text style={styles.tagline}>{masjidConfig.tagline}</Text>
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
              <Text style={styles.badgeText}>Jakarta Timur</Text>
            </View>
            <View style={styles.badge}>
              <View style={[styles.badgeDot, styles.badgeDotOnline]} />
              <Text style={styles.badgeText}>Online</Text>
            </View>
            {isRamadanPeriod && (
              <View style={[styles.badge, styles.ramadanBadge]}>
                <View style={styles.badgeDot} />
                <Text style={styles.badgeText}>Ramadan Kareem</Text>
              </View>
            )}
          </View>
        </View>

        {/* Main Content - 3 Column Layout */}
        <ScrollView
          style={styles.scrollContainer}
          showsVerticalScrollIndicator={false}
        >
          <View style={styles.coreContent}>
            {/* Left Column - Prayer Times */}
            <View style={styles.leftColumn}>
              <View style={styles.card}>
                <View style={styles.cardHeader}>
                  <View style={styles.cardTitleContainer}>
                    <View style={styles.cardIconDiamond} />
                    <Text style={styles.cardTitle}>Jadwal Salat Hari Ini</Text>
                  </View>
                  <View style={styles.chip}>
                    <Text style={styles.chipText}>WIB</Text>
                  </View>
                </View>

                <View style={styles.tableHeader}>
                  <Text style={[styles.tableHeaderText, { flex: 0.2 }]}>Salat</Text>
                  <Text style={[styles.tableHeaderText, { flex: 0.18, textAlign: 'center' }]}>Adzan</Text>
                  <Text style={[styles.tableHeaderText, { flex: 0.18, textAlign: 'center' }]}>Iqamah</Text>
                  <Text style={[styles.tableHeaderText, { flex: 0.22, textAlign: 'center' }]}>Status</Text>
                  <Text style={[styles.tableHeaderText, { flex: 0.22, textAlign: 'right' }]}>Waktu</Text>
                </View>

                {prayers.map((prayer) => (
                  <PrayerRow key={prayer.name} prayer={prayer} />
                ))}
              </View>

              <View style={styles.spacer} />

              {/* Next Prayer Card */}
              <NextPrayerCard prayer={nextPrayer} />
            </View>

            {/* Middle Column - Islamic Content */}
            <View style={styles.middleColumn}>
              {/* Quran Verse */}
              <QuranVerseCard autoRotate rotationInterval={40000} />

              <View style={styles.spacer} />

              {/* Hadith */}
              <HadithCard autoRotate rotationInterval={50000} />
            </View>

            {/* Right Column - Studies & Kas */}
            <View style={styles.rightColumn}>
              {/* Islamic Studies */}
              <IslamicStudiesCard showTodayOnly={false} />

              <View style={styles.spacer} />

              {/* Kas Summary */}
              <KasSummary kasData={kasData} variant="compact_with_sparkline" />
            </View>
          </View>
        </ScrollView>

        {/* Ticker */}
        <View style={styles.tickerContainer}>
          <AnnouncementTicker announcements={announcements} speed="slow" />
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
    paddingTop: safeAreaMargins.top,
    paddingBottom: safeAreaMargins.bottom,
  },
  scrollContainer: {
    flex: 1,
  },
  header: {
    height: 120,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: spacing.xxl,
    marginBottom: spacing.lg,
  },
  headerLeft: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
  accentBar: {
    width: 4,
    height: 56,
    backgroundColor: colors.accentPrimary,
    marginRight: spacing.lg,
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
    borderRadius: radii.medium,
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
  badgeDotOnline: {
    backgroundColor: '#4ade80',
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
  coreContent: {
    flexDirection: 'row',
    gap: spacing.lg,
    paddingHorizontal: spacing.xxl,
    paddingBottom: spacing.xl,
  },
  leftColumn: {
    flex: 0.32,
  },
  middleColumn: {
    flex: 0.34,
  },
  rightColumn: {
    flex: 0.34,
  },
  card: {
    backgroundColor: colors.surfaceGlass,
    borderRadius: radii.large,
    padding: spacing.xl,
    borderWidth: 1,
    borderColor: colors.accentPrimarySoft,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.55,
    shadowRadius: 32,
    elevation: 12,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  cardTitleContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  cardIconDiamond: {
    width: 12,
    height: 12,
    backgroundColor: colors.accentPrimary,
    transform: [{ rotate: '45deg' }],
    marginRight: spacing.md,
  },
  cardTitle: {
    ...typography.titleM,
    color: colors.textPrimary,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  chip: {
    backgroundColor: colors.surfaceElevated,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
  },
  chipText: {
    ...typography.caption,
    color: colors.textMuted,
  },
  tableHeader: {
    flexDirection: 'row',
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
    marginBottom: spacing.sm,
  },
  tableHeaderText: {
    ...typography.bodyS,
    color: colors.textMuted,
    textTransform: 'uppercase',
  },
  spacer: {
    height: spacing.lg,
  },
  tickerContainer: {
    marginTop: spacing.lg,
    paddingHorizontal: spacing.xxl,
  },
});
