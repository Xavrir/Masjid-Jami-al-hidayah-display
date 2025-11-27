import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  StatusBar,
  Animated,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii, safeAreaMargins } from '../theme/spacing';
import { formatTimeWithSeconds } from '../utils/dateTime';
import { Prayer } from '../types';
import { durations } from '../theme/motion';

interface PrayerInProgressProps {
  prayer: Prayer;
  onComplete?: () => void;
}

export const PrayerInProgress: React.FC<PrayerInProgressProps> = ({
  prayer,
  onComplete,
}) => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const glowAnim = new Animated.Value(0);

  // Update time every second
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  // Pulse animation for the icon
  useEffect(() => {
    Animated.loop(
      Animated.sequence([
        Animated.timing(glowAnim, {
          toValue: 1,
          duration: durations.slow,
          useNativeDriver: true,
        }),
        Animated.timing(glowAnim, {
          toValue: 0,
          duration: durations.slow,
          useNativeDriver: true,
        }),
      ])
    ).start();
  }, []);

  const glowOpacity = glowAnim.interpolate({
    inputRange: [0, 1],
    outputRange: [0.3, 0.8],
  });

  return (
    <View style={styles.container}>
      <StatusBar hidden />

      <LinearGradient
        colors={[colors.background, '#050F18']}
        style={styles.gradient}
        start={{ x: 0.5, y: 0 }}
        end={{ x: 0.5, y: 1 }}
      >
        {/* Background ornament */}
        <View style={styles.ornamentContainer}>
          <Text style={styles.ornament}>🕌</Text>
        </View>

        {/* Center Message Card */}
        <View style={styles.centerContainer}>
          <Animated.View style={[styles.card, { opacity: glowOpacity }]}>
            <View style={styles.iconContainer}>
              <Text style={styles.icon}>🕌</Text>
            </View>

            <Text style={styles.subtitle}>Sedang Berlangsung:</Text>

            <View style={styles.titleContainer}>
              <Text style={styles.title}>Salat {prayer.name}</Text>
            </View>

            <Text style={styles.message}>
              Mohon menjaga ketenangan dan kekhusyukan.
            </Text>

            <View style={styles.divider} />

            <View style={styles.countdownSection}>
              <Text style={styles.countdownLabel}>Perkiraan selesai</Text>
              <Text style={styles.countdownTime}>{prayer.countdown || '--:--'}</Text>
            </View>
          </Animated.View>
        </View>

        {/* Bottom Info */}
        <View style={styles.bottomInfo}>
          <View style={styles.bottomLeft}>
            <View style={styles.timelineContainer}>
              {['Subuh', 'Dzuhur', 'Ashar', 'Maghrib', 'Isya'].map((name) => (
                <View
                  key={name}
                  style={[
                    styles.timelineDot,
                    name === prayer.name && styles.timelineDotActive,
                  ]}
                />
              ))}
            </View>
          </View>

          <View style={styles.bottomCenter}>
            <Text style={styles.currentTimeLarge}>
              {formatTimeWithSeconds(currentTime)}
            </Text>
          </View>

          <View style={styles.bottomRight}>
            <View style={styles.infoChip}>
              <Text style={styles.infoChipIcon}>🔔</Text>
              <View>
                <Text style={styles.infoChipLabel}>Adzan</Text>
                <Text style={styles.infoChipValue}>{prayer.adhanTime}</Text>
              </View>
            </View>

            <View style={[styles.infoChip, { marginLeft: spacing.lg }]}>
              <Text style={styles.infoChipIcon}>⏰</Text>
              <View>
                <Text style={styles.infoChipLabel}>Iqamah</Text>
                <Text style={styles.infoChipValue}>{prayer.iqamahTime}</Text>
              </View>
            </View>
          </View>
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
    justifyContent: 'space-between',
  },
  ornamentContainer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    alignItems: 'center',
    opacity: 0.15,
  },
  ornament: {
    fontSize: 400,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: safeAreaMargins.left * 2,
  },
  card: {
    backgroundColor: colors.surfaceGlass,
    borderRadius: radii.large,
    paddingVertical: spacing.sectionGap,
    paddingHorizontal: spacing.sectionGap,
    borderWidth: 1.2,
    borderColor: colors.accentPrimary,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 18 },
    shadowOpacity: 0.75,
    shadowRadius: 48,
    elevation: 24,
    minWidth: 600,
    alignItems: 'center',
  },
  iconContainer: {
    marginBottom: spacing.xl,
  },
  icon: {
    fontSize: 64,
  },
  subtitle: {
    ...typography.bodyL,
    color: colors.textSecondary,
    marginBottom: spacing.md,
  },
  titleContainer: {
    marginBottom: spacing.xl,
  },
  title: {
    ...typography.headlineXL,
    color: colors.accentPrimary,
    textAlign: 'center',
    fontWeight: '700',
  },
  message: {
    ...typography.bodyL,
    color: colors.textSecondary,
    textAlign: 'center',
    opacity: 0.8,
    marginBottom: spacing.xl,
  },
  divider: {
    width: '100%',
    height: 1,
    backgroundColor: colors.divider,
    marginBottom: spacing.xl,
  },
  countdownSection: {
    alignItems: 'center',
  },
  countdownLabel: {
    ...typography.bodyS,
    color: colors.textMuted,
    marginBottom: spacing.sm,
  },
  countdownTime: {
    ...typography.numericMedium,
    color: colors.accentSecondary,
  },
  bottomInfo: {
    height: 120,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: safeAreaMargins.left + spacing.xl,
    paddingBottom: safeAreaMargins.bottom,
  },
  bottomLeft: {
    flex: 1,
  },
  timelineContainer: {
    flexDirection: 'row',
    gap: spacing.md,
  },
  timelineDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: colors.surfaceElevated,
    borderWidth: 2,
    borderColor: colors.divider,
  },
  timelineDotActive: {
    backgroundColor: colors.accentPrimary,
    borderColor: colors.accentPrimary,
    shadowColor: colors.accentPrimary,
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.6,
    shadowRadius: 8,
    elevation: 8,
  },
  bottomCenter: {
    flex: 1,
    alignItems: 'center',
  },
  currentTimeLarge: {
    ...typography.headlineL,
    color: colors.textPrimary,
  },
  bottomRight: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'flex-end',
  },
  infoChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surfaceElevated,
    borderRadius: radii.medium,
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.lg,
  },
  infoChipIcon: {
    fontSize: 24,
    marginRight: spacing.md,
  },
  infoChipLabel: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: spacing.xs,
  },
  infoChipValue: {
    ...typography.numericSmall,
    color: colors.textPrimary,
  },
});
