import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  ImageBackground,
  Pressable,
  StatusBar,
  StyleSheet,
  Text,
  Vibration,
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
const BEEP_INTERVAL_MS = 1200;
const BACKGROUND_IMAGE =
  'https://images.unsplash.com/photo-1501446529957-6226bd447c46?auto=format&fit=crop&w=1800&q=80';

type SoundInstance = {
  play: (callback?: (success: boolean) => void) => void;
  stop: (callback?: () => void) => void;
  release: () => void;
};

type SoundConstructor = new (
  source: number | string,
  basePath: string,
  onError?: (error: unknown) => void
) => SoundInstance;

type SoundModule = SoundConstructor & {
  setCategory: (category: string) => void;
  MAIN_BUNDLE: string;
};

const isSoundModule = (value: unknown): value is SoundModule => {
  if (typeof value !== 'function') return false;
  const maybe = value as unknown as {
    setCategory?: unknown;
    MAIN_BUNDLE?: unknown;
  };
  return (
    typeof maybe.setCategory === 'function' &&
    typeof maybe.MAIN_BUNDLE === 'string'
  );
};

interface PrayerInProgressProps {
  prayer: Prayer;
  onComplete?: () => void;
  masjidName?: string;
  masjidLocation?: string;
  forceDebug?: boolean;
}

const formatMsToClock = (ms: number): string => {
  if (ms <= 0) return '00:00';

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
  const [soundReady, setSoundReady] = useState(false);

  const autoReturnDeadlineRef = useRef<Date | null>(null);
  const autoReturnTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const beepIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const soundRef = useRef<SoundInstance | null>(null);

  const { start, end, iqamahDate, durationMinutes } = useMemo(
    () => getPrayerWindowBounds(prayer, currentTime),
    [prayer, currentTime]
  );

  useEffect(() => {
    const tick = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(tick);
  }, []);

  useEffect(() => {
    let instance: SoundInstance | null = null;

    try {
      const required: unknown = require('react-native-sound');
      if (!isSoundModule(required)) {
        throw new Error('Invalid sound module');
      }

      required.setCategory('Playback');
      instance = new required(
        require('../assets/sounds/beep.wav'),
        required.MAIN_BUNDLE,
        (error: unknown) => {
          if (error) {
            if (__DEV__) {
              console.warn('Gagal memuat bunyi beep', error);
            }
          } else {
            setSoundReady(true);
          }
        }
      );
    } catch (error) {
      if (__DEV__) {
        console.warn('Modul suara tidak tersedia, fallback ke vibrasi', error);
      }
    }

    soundRef.current = instance;

    return () => {
      instance?.release();
    };
  }, []);

  const stopBeepLoop = () => {
    if (beepIntervalRef.current) {
      clearInterval(beepIntervalRef.current);
      beepIntervalRef.current = null;
    }
    soundRef.current?.stop?.();
  };

  const playBeep = () => {
    if (soundRef.current && soundReady) {
      soundRef.current.stop?.(() => {
        soundRef.current?.play?.();
      });
    } else {
      Vibration.vibrate(120);
    }
  };

  const startBeepLoop = () => {
    stopBeepLoop();
    playBeep();
    beepIntervalRef.current = setInterval(playBeep, BEEP_INTERVAL_MS);
  };

  useEffect(() => {
    startBeepLoop();
    const deadline = new Date(Date.now() + AUTO_RETURN_MS);
    autoReturnDeadlineRef.current = deadline;

    autoReturnTimerRef.current = setTimeout(() => {
      stopBeepLoop();
      onComplete?.();
    }, AUTO_RETURN_MS);

    return () => {
      stopBeepLoop();
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
        source={{ uri: BACKGROUND_IMAGE }}
        blurRadius={18}
        style={styles.background}>
        <LinearGradient
          colors={['rgba(2, 7, 18, 0.84)', 'rgba(4, 13, 26, 0.94)']}
          style={styles.overlay}>
          <View style={styles.header}>
            <View style={styles.headerLeft}>
              <Text style={styles.masjidName}>{masjidName.toUpperCase()}</Text>
              {masjidLocation ? (
                <Text style={styles.masjidLocation}>{masjidLocation}</Text>
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
                    Beep 60 detik {soundReady ? 'aktif' : 'vibrasi'}
                  </Text>
                </View>
                <Text style={styles.footerText}>
                  Kembali ke beranda dalam{' '}
                  {formatMsToClock(autoReturnRemainingMs)}
                </Text>
              </View>
            </View>
          </View>

          <View style={styles.bottomBar}>
            <View>
              <Text style={styles.bottomLabel}>Mulai adzan</Text>
              <Text style={styles.bottomValue}>
                {start.toLocaleTimeString([], {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </Text>
            </View>
            <View>
              <Text style={styles.bottomLabel}>Akhir jendela</Text>
              <Text style={styles.bottomValue}>
                {end.toLocaleTimeString([], {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </Text>
            </View>
            <Pressable onPress={() => onComplete?.()} style={styles.skipButton}>
              <Text style={styles.skipText}>Kembali</Text>
            </Pressable>
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
    maxWidth: 360,
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
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.lg,
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
  footerText: {
    ...typography.bodyS,
    color: colors.textSecondary,
  },
  bottomBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: spacing.xxl,
    borderTopWidth: 1,
    borderTopColor: colors.divider,
    paddingTop: spacing.lg,
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
