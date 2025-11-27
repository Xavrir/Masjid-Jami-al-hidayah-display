import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing } from '../theme/spacing';
import { Prayer } from '../types';

interface PrayerRowProps {
  prayer: Prayer;
}

export const PrayerRow: React.FC<PrayerRowProps> = ({ prayer }) => {
  const getRowStyle = () => {
    switch (prayer.status) {
      case 'current':
        return styles.current;
      case 'upcoming':
        return styles.upcoming;
      case 'passed':
        return styles.passed;
      default:
        return {};
    }
  };

  const getTextStyle = () => {
    return prayer.status === 'passed' ? styles.passedText : {};
  };

  return (
    <View style={[styles.container, getRowStyle()]}>
      <View style={styles.nameColumn}>
        <Text style={[styles.nameText, getTextStyle()]}>{prayer.name}</Text>
      </View>

      <View style={styles.timeColumn}>
        <Text style={[styles.timeText, getTextStyle()]}>{prayer.adhanTime}</Text>
      </View>

      <View style={styles.timeColumn}>
        <Text style={[styles.timeText, getTextStyle()]}>{prayer.iqamahTime}</Text>
      </View>

      <View style={styles.statusColumn}>
        <Text style={[styles.statusText, getTextStyle()]}>
          {prayer.status === 'current' && 'Sedang Berlangsung'}
          {prayer.status === 'upcoming' && 'Akan Datang'}
          {prayer.status === 'passed' && 'Selesai'}
        </Text>
      </View>

      <View style={styles.countdownColumn}>
        {prayer.countdown && (
          <Text style={[styles.countdownText, getTextStyle()]}>
            {prayer.countdown}
          </Text>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    height: 64,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.lg,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  nameColumn: {
    flex: 0.2,
    justifyContent: 'center',
  },
  timeColumn: {
    flex: 0.18,
    justifyContent: 'center',
    alignItems: 'center',
  },
  statusColumn: {
    flex: 0.22,
    justifyContent: 'center',
    alignItems: 'center',
  },
  countdownColumn: {
    flex: 0.22,
    justifyContent: 'center',
    alignItems: 'flex-end',
  },
  nameText: {
    ...typography.bodyL,
    color: colors.textPrimary,
  },
  timeText: {
    ...typography.numericSmall,
    color: colors.textSecondary,
  },
  statusText: {
    ...typography.caption,
    color: colors.textMuted,
    textTransform: 'uppercase',
  },
  countdownText: {
    ...typography.numericSmall,
    color: colors.accentSecondary,
  },
  current: {
    backgroundColor: colors.accentPrimarySoft,
    borderLeftWidth: 4,
    borderLeftColor: colors.prayerCurrent,
    shadowColor: colors.accentPrimary,
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 8,
  },
  upcoming: {
    borderLeftWidth: 3,
    borderLeftColor: colors.prayerUpcoming,
  },
  passed: {
    opacity: 0.7,
  },
  passedText: {
    color: colors.prayerPassed,
  },
});
