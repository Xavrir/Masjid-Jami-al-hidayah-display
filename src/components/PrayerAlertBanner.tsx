import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, Animated } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { Prayer } from '../types';

interface PrayerAlertBannerProps {
  type: 'adhan' | 'iqamah';
  prayer: Prayer;
}

export const PrayerAlertBanner: React.FC<PrayerAlertBannerProps> = ({
  type,
  prayer,
}) => {
  const slideAnim = useRef(new Animated.Value(-100)).current;
  const fadeAnim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(slideAnim, {
        toValue: 0,
        duration: 300,
        useNativeDriver: true,
      }),
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 300,
        useNativeDriver: true,
      }),
    ]).start();

    return () => {
      Animated.parallel([
        Animated.timing(slideAnim, {
          toValue: -100,
          duration: 300,
          useNativeDriver: true,
        }),
        Animated.timing(fadeAnim, {
          toValue: 0,
          duration: 300,
          useNativeDriver: true,
        }),
      ]).start();
    };
  }, [slideAnim, fadeAnim]);

  const isAdhan = type === 'adhan';

  return (
    <Animated.View
      style={[
        styles.container,
        {
          transform: [{ translateY: slideAnim }],
          opacity: fadeAnim,
        },
        isAdhan ? styles.adhanContainer : styles.iqamahContainer,
      ]}>
      <View style={styles.iconContainer}>
        <Text style={styles.icon}>{isAdhan ? '🕌' : '🚶'}</Text>
      </View>

      <View style={styles.textContainer}>
        <Text
          style={[
            styles.title,
            isAdhan ? styles.adhanTitle : styles.iqamahTitle,
          ]}>
          {isAdhan ? 'WAKTU ADZAN' : 'IQAMAH'}
        </Text>
        <Text
          style={[
            styles.subtitle,
            isAdhan ? styles.adhanSubtitle : styles.iqamahSubtitle,
          ]}>
          {isAdhan ? prayer.name.toUpperCase() : 'MOHON BERDIRI UNTUK SHALAT'}
        </Text>
      </View>

      <View
        style={[styles.pulse, isAdhan ? styles.adhanPulse : styles.iqamahPulse]}
      />
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.xl,
    marginHorizontal: spacing.xl,
    marginBottom: spacing.md,
    borderRadius: radii.medium,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  adhanContainer: {
    backgroundColor: 'rgba(22, 160, 133, 0.25)',
    borderWidth: 2,
    borderColor: colors.accentSecondary,
  },
  iqamahContainer: {
    backgroundColor: 'rgba(212, 175, 55, 0.3)',
    borderWidth: 3,
    borderColor: colors.accentPrimary,
  },
  iconContainer: {
    marginRight: spacing.md,
  },
  icon: {
    fontSize: 40,
  },
  textContainer: {
    flex: 1,
  },
  title: {
    ...typography.headlineS,
    fontWeight: '800',
    letterSpacing: 2,
    marginBottom: spacing.xs,
  },
  adhanTitle: {
    color: colors.accentSecondary,
  },
  iqamahTitle: {
    color: colors.accentPrimary,
    fontSize: 28,
  },
  subtitle: {
    ...typography.bodyM,
    fontWeight: '600',
  },
  adhanSubtitle: {
    color: colors.textPrimary,
  },
  iqamahSubtitle: {
    color: colors.textPrimary,
    fontSize: 18,
    letterSpacing: 1,
  },
  pulse: {
    width: 12,
    height: 12,
    borderRadius: 6,
    marginLeft: spacing.md,
  },
  adhanPulse: {
    backgroundColor: colors.accentSecondary,
  },
  iqamahPulse: {
    backgroundColor: colors.accentPrimary,
  },
});
