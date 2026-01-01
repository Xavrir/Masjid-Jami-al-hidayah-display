import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  ImageBackground,
  Pressable,
  StatusBar,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { radii, safeAreaMargins, spacing } from '../theme/spacing';
import { formatGregorianDate, formatTimeWithSeconds } from '../utils/dateTime';
import { Prayer } from '../types';
import {
  getHijriDate,
  getPrayerPhase,
  getPrayerWindowBounds,
} from '../utils/prayerTimesAdhan';

const AUTO_RETURN_MS = 60_000;
const BACKGROUND_IMAGE = require('../assets/images/kaaba-background.jpg');

interface PrayerInProgressProps {
  prayer: Prayer;
  onComplete?: () => void;
  masjidName?: string;
  masjidLocation?: string;
  forceDebug?: boolean;
}

const formatMsToClock = (ms: number): string => {
  if (ms <= 0) {
    return '00:00';
  }

  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) {
    return `${hours.toString().padStart(2, '0')}:${minutes
      .toString()
      .padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
};

export const PrayerInProgress: React.FC<PrayerInProgressProps> = ({
  prayer,
  onComplete,
  masjidName = 'Masjid',
  masjidLocation,
  forceDebug = false,
}) => {
  const [currentTime, setCurrentTime] = useState(new Date());

  const autoReturnDeadlineRef = useRef<Date | null>(null);
  const autoReturnTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const { start, end, iqamahDate, durationMinutes } = useMemo(
    () => getPrayerWindowBounds(prayer, currentTime),
    [prayer, currentTime]
  );

  useEffect(() => {
    const tick = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(tick);
  }, []);

  useEffect(() => {
    const deadline = new Date(Date.now() + AUTO_RETURN_MS);
    autoReturnDeadlineRef.current = deadline;

    autoReturnTimerRef.current = setTimeout(() => {
      onComplete?.();
    }, AUTO_RETURN_MS);

    return () => {
      if (autoReturnTimerRef.current) {
        clearTimeout(autoReturnTimerRef.current);
        autoReturnTimerRef.current = null;
      }
    };
  }, [prayer, onComplete]);

  const windowRemainingMs = Math.max(0, end.getTime() - currentTime.getTime());
  const iqamahRemainingMs = Math.max(
    0,
    iqamahDate.getTime() - currentTime.getTime()
  );
  const autoReturnRemainingMs = Math.max(
    0,
    (autoReturnDeadlineRef.current?.getTime() ?? Date.now()) -
      currentTime.getTime()
  );
  const phase = getPrayerPhase(prayer, currentTime);

  return (
    <View style={styles.container}>
      <StatusBar hidden />

      <ImageBackground
        source={BACKGROUND_IMAGE}
        blurRadius={18}
        style={styles.background}>
        <LinearGradient
          colors={['rgba(2, 7, 18, 0.84)', 'rgba(4, 13, 26, 0.94)']}
          style={styles.overlay}>
          <View style={styles.header}>
            <View style={styles.headerLeft}>
              <Text style={styles.masjidName}>{masjidName.toUpperCase()}</Text>
              {masjidLocation ? (
                <Text
                  style={styles.masjidLocation}
                  numberOfLines={2}
                  ellipsizeMode="tail">
                  {masjidLocation}
                </Text>
              ) : null}
            </View>

            <View style={styles.headerCenter}>
              <Text style={styles.currentTime}>
                {formatTimeWithSeconds(currentTime)}
              </Text>
              <Text style={styles.dateText}>
                {formatGregorianDate(currentTime)}
              </Text>
              <Text style={styles.hijriText}>{getHijriDate(currentTime)}</Text>
            </View>

            <View style={styles.headerRight}>
              <View style={styles.badge}>
                <Text style={styles.badgeText}>
                  {phase === 'adzan' ? 'Adzan' : 'Iqamah'}
                </Text>
              </View>
              <View style={[styles.badge, forceDebug && styles.badgeWarning]}>
                <Text style={styles.badgeText}>
                  {forceDebug ? 'Debug' : 'Live'}
                </Text>
              </View>
            </View>
          </View>

          <View style={styles.centerContent}>
            <View style={styles.card}>
              <View style={styles.cardHeader}>
                <View style={styles.statusPill}>
                  <Text style={styles.statusText}>
                    {phase === 'adzan' ? 'Adzan' : 'Iqamah'} • {prayer.name}
                  </Text>
                </View>
                <Text style={styles.windowText}>
                  Jendela {durationMinutes} menit
                </Text>
              </View>

              <Text style={styles.cardTitle}>Sedang berlangsung</Text>
              <Text style={styles.cardSubtitle}>
                Mohon menjaga ketenangan dan kekhusyukan jamaah.
              </Text>

              <View style={styles.countdownRow}>
                <View style={styles.countdownBlock}>
                  <Text style={styles.countdownLabel}>Sisa waktu</Text>
                  <Text style={styles.countdownValue}>
                    {formatMsToClock(windowRemainingMs)}
                  </Text>
                  <Text style={styles.countdownHint}>
                    Hingga akhir jendela adzan
                  </Text>
                </View>

                <View style={styles.countdownBlock}>
                  <Text style={styles.countdownLabel}>Menuju iqamah</Text>
                  <Text style={styles.countdownValue}>
                    {phase === 'iqamah'
                      ? '00:00'
                      : formatMsToClock(iqamahRemainingMs)}
                  </Text>
                  <Text style={styles.countdownHint}>
                    {phase === 'iqamah'
                      ? 'Iqamah berlangsung'
                      : 'Perkiraan ke iqamah'}
                  </Text>
                </View>
              </View>

              <View style={styles.chipRow}>
                <View
                  style={[styles.chip, phase === 'adzan' && styles.chipActive]}>
                  <Text style={styles.chipLabel}>Adzan</Text>
                  <Text style={styles.chipValue}>{prayer.adhanTime}</Text>
                </View>
                <View
                  style={[
                    styles.chip,
                    phase === 'iqamah' && styles.chipActive,
                  ]}>
                  <Text style={styles.chipLabel}>Iqamah</Text>
                  <Text style={styles.chipValue}>{prayer.iqamahTime}</Text>
                </View>
              </View>

              <View style={styles.footerRow}>
                <View style={styles.beepBadge}>
                  <Text style={styles.beepBadgeText}>
                    Notifikasi suara hanya saat adzan & iqamah
                  </Text>
                </View>
              </View>
            </View>
          </View>

          <View style={styles.bottomBar}>
            <View style={styles.bottomTimeBlock}>
              <Text style={styles.bottomLabel}>Mulai adzan</Text>
              <Text style={styles.bottomValue}>
                {start.toLocaleTimeString([], {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </Text>
            </View>
            <View style={styles.bottomTimeBlock}>
              <Text style={styles.bottomLabel}>Akhir jendela</Text>
              <Text style={styles.bottomValue}>
                {end.toLocaleTimeString([], {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </Text>
            </View>
            <View style={styles.bottomRight}>
              <Text style={styles.autoReturnText} numberOfLines={1}>
                Kembali ke beranda dalam{' '}
                {formatMsToClock(autoReturnRemainingMs)}
              </Text>
              <Pressable
                onPress={() => onComplete?.()}
                style={styles.skipButton}>
                <Text style={styles.skipText}>Kembali</Text>
              </Pressable>
            </View>
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
  background: {
    flex: 1,
  },
  overlay: {
    flex: 1,
    paddingTop: safeAreaMargins.top + spacing.lg,
    paddingHorizontal: spacing.xxl,
    paddingBottom: safeAreaMargins.bottom + spacing.lg,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: spacing.xxl,
  },
  headerLeft: {
    flex: 1,
    maxWidth: 520,
    paddingRight: spacing.xxl,
  },
  masjidName: {
    ...typography.headlineS,
    color: colors.textPrimary,
    letterSpacing: 2,
    marginBottom: spacing.xs,
  },
  masjidLocation: {
    ...typography.bodyS,
    color: colors.textSecondary,
    maxWidth: 520,
    lineHeight: 18,
  },
  headerCenter: {
    flex: 1,
    alignItems: 'center',
  },
  currentTime: {
    ...typography.displayM,
    color: colors.textPrimary,
  },
  dateText: {
    ...typography.bodyM,
    color: colors.textSecondary,
    marginTop: spacing.xs,
  },
  hijriText: {
    ...typography.bodyS,
    color: colors.accentPrimary,
    marginTop: spacing.xs,
  },
  headerRight: {
    flex: 1,
    alignItems: 'flex-end',
    gap: spacing.sm,
  },
  badge: {
    backgroundColor: colors.surfaceGlass,
    borderColor: colors.accentPrimarySoft,
    borderWidth: 1,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
  },
  badgeWarning: {
    backgroundColor: colors.badgeWarning,
  },
  badgeText: {
    ...typography.caption,
    color: colors.textPrimary,
    letterSpacing: 0.5,
  },
  centerContent: {
    flex: 1,
    justifyContent: 'center',
  },
  card: {
    backgroundColor: 'rgba(8, 16, 28, 0.75)',
    borderRadius: radii.large,
    padding: spacing.sectionGap,
    borderWidth: 1.2,
    borderColor: colors.accentPrimarySoft,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 18 },
    shadowOpacity: 0.65,
    shadowRadius: 42,
    elevation: 20,
    marginBottom: spacing.xxl,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.xl,
  },
  statusPill: {
    backgroundColor: colors.accentPrimarySoft,
    borderRadius: radii.pill,
    paddingVertical: spacing.xs,
    paddingHorizontal: spacing.md,
  },
  statusText: {
    ...typography.caption,
    color: colors.accentPrimary,
    letterSpacing: 0.5,
  },
  windowText: {
    ...typography.bodyS,
    color: colors.textSecondary,
  },
  cardTitle: {
    ...typography.headlineL,
    color: colors.textPrimary,
    marginTop: spacing.xs,
    marginBottom: spacing.sm,
  },
  cardSubtitle: {
    ...typography.bodyM,
    color: colors.textSecondary,
    marginBottom: spacing.xl,
  },
  countdownRow: {
    flexDirection: 'row',
    gap: spacing.lg,
    marginBottom: spacing.lg,
  },
  countdownBlock: {
    flex: 1,
    backgroundColor: colors.surfaceElevated,
    borderRadius: radii.medium,
    paddingVertical: spacing.lg,
    paddingHorizontal: spacing.xl,
    borderWidth: 1,
    borderColor: colors.borderSubtle,
  },
  countdownLabel: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: spacing.xs,
  },
  countdownValue: {
    ...typography.displayS,
    color: colors.accentPrimary,
    marginBottom: spacing.sm,
  },
  countdownHint: {
    ...typography.bodyS,
    color: colors.textSecondary,
  },
  chipRow: {
    flexDirection: 'row',
    gap: spacing.md,
    marginBottom: spacing.lg,
  },
  chip: {
    flex: 1,
    backgroundColor: colors.surfaceGlass,
    borderRadius: radii.medium,
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.lg,
    borderWidth: 1,
    borderColor: colors.borderSubtle,
  },
  chipActive: {
    borderColor: colors.accentPrimary,
    backgroundColor: colors.accentPrimarySoft,
  },
  chipLabel: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: spacing.xs,
  },
  chipValue: {
    ...typography.numericMedium,
    color: colors.textPrimary,
  },
  footerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  beepBadge: {
    backgroundColor: colors.surfaceElevated,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
    borderWidth: 1,
    borderColor: colors.accentPrimarySoft,
  },
  beepBadgeText: {
    ...typography.caption,
    color: colors.textSecondary,
    letterSpacing: 0.2,
  },
  bottomBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    borderTopWidth: 1,
    borderTopColor: colors.divider,
    paddingTop: spacing.lg,
  },
  bottomTimeBlock: {
    minWidth: 220,
  },
  bottomRight: {
    alignItems: 'flex-end',
    gap: spacing.sm,
    flex: 1,
  },
  autoReturnText: {
    ...typography.bodyS,
    color: colors.textSecondary,
  },
  bottomLabel: {
    ...typography.caption,
    color: colors.textMuted,
  },
  bottomValue: {
    ...typography.numericSmall,
    color: colors.textPrimary,
    marginTop: spacing.xs,
  },
  skipButton: {
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
    borderRadius: radii.medium,
    borderWidth: 1,
    borderColor: colors.borderSubtle,
    backgroundColor: colors.surfaceGlass,
  },
  skipText: {
    ...typography.bodyS,
    color: colors.textPrimary,
    letterSpacing: 0.5,
  },
});
