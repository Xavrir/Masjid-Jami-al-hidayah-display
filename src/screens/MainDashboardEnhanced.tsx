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
import { spacing, radii } from '../theme/spacing';
import { NextPrayerCard } from '../components/NextPrayerCard';
import { KasSummary } from '../components/KasSummary';
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

  // Determine which prayers to display in compact view
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
<<<<<<< HEAD
              </View>

              <View style={styles.headerRight}>
=======
>>>>>>> 165ea55 (feat: Enhance Main Dashboard layout and add new typography styles)
                <Text style={styles.hijriDate}>
                  {getHijriDate(currentTime)}
                </Text>
              </View>
<<<<<<< HEAD
=======

              <View style={styles.headerRight}>
                {isRamadanPeriod && (
                  <View style={[styles.badge, styles.ramadanBadge]}>
                    <View style={styles.badgeDot} />
                    <Text style={styles.badgeText}>Ramadan Kareem</Text>
                  </View>
                )}
              </View>
>>>>>>> 165ea55 (feat: Enhance Main Dashboard layout and add new typography styles)
            </View>

            {/* Compact Prayer Schedule - Below Clock */}
            <View style={styles.prayerTimesCompact}>
              {displayPrayers.map((prayer, index) => {
<<<<<<< HEAD
                // For tomorrow's prayers, highlight the first one (Subuh)
                const isHighlighted = isNextPrayerTomorrow && index === 0;
=======
                const isTheNextPrayer = nextPrayer?.name === prayer.name;
>>>>>>> 165ea55 (feat: Enhance Main Dashboard layout and add new typography styles)
                return (
                  <View
                    key={prayer.name}
                    style={[
                      styles.prayerTimeItem,
                      prayer.status === 'current' &&
                        styles.prayerTimeItemActive,
<<<<<<< HEAD
                      (prayer.status === 'upcoming' || isHighlighted) &&
                        styles.prayerTimeItemNext,
=======
                      isTheNextPrayer && styles.prayerTimeItemNext,
>>>>>>> 165ea55 (feat: Enhance Main Dashboard layout and add new typography styles)
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

          {/* Main Content - Scrollable 2 Column Layout */}
          <ScrollView
            style={styles.scrollContainer}
            showsVerticalScrollIndicator={false}>
            <View style={styles.coreContent}>
              {/* Left Column - Next Prayer & Quran */}
              <View style={styles.leftColumn}>
                <NextPrayerCard
                  prayer={nextPrayer}
                  isTomorrow={isNextPrayerTomorrow}
                />
                <View style={styles.spacer} />
                <QuranVerseCard autoRotate rotationInterval={40000} />
              </View>

              {/* Right Column - Hadith & Kas */}
              <View style={styles.rightColumn}>
                <HadithCard autoRotate rotationInterval={50000} />
                <View style={styles.spacer} />
                <KasSummary
                  kasData={kasData}
                  variant="compact_with_sparkline"
                />
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
    paddingTop: spacing.lg,
    paddingBottom: spacing.lg,
    paddingHorizontal: spacing.xxl,
  },
  // Header Section - Integrated with Prayer Times
  headerSection: {
    marginBottom: spacing.lg,
  },
  scrollContainer: {
    flex: 1,
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
    flexDirection: 'row',
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xl,
  },
  leftColumn: {
    flex: 0.5,
  },
  rightColumn: {
    flex: 0.5,
  },
  spacer: {
    height: spacing.md,
  },
  tickerContainer: {
    paddingHorizontal: spacing.lg,
  },
});
