import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { Prayer } from '../types';

interface NextPrayerCardProps {
  prayer: Prayer | null;
}

export const NextPrayerCard: React.FC<NextPrayerCardProps> = ({ prayer }) => {
  if (!prayer) {
    return (
      <View style={styles.container}>
        <Text style={styles.noDataText}>Tidak ada jadwal salat berikutnya</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.chip}>
        <Text style={styles.chipText}>{prayer.name}</Text>
      </View>

      <View style={styles.contentRow}>
        <View style={styles.leftSection}>
          <Text style={styles.label}>Berikutnya:</Text>
        </View>

        <View style={styles.centerSection}>
          <Text style={styles.timeLabel}>Adzan</Text>
          <Text style={styles.timeValue}>{prayer.adhanTime}</Text>
        </View>

        <View style={styles.rightSection}>
          <Text style={styles.countdownLabel}>Dalam</Text>
          <Text style={styles.countdownValue}>{prayer.countdown || '--:--'}</Text>
        </View>
      </View>

      <View style={styles.iqamahRow}>
        <Text style={styles.iqamahLabel}>Iqamah: </Text>
        <Text style={styles.iqamahTime}>{prayer.iqamahTime}</Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surfaceElevated,
    borderRadius: radii.large,
    padding: spacing.xl,
    borderWidth: 1,
    borderColor: colors.accentPrimarySoft,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 18 },
    shadowOpacity: 0.75,
    shadowRadius: 48,
    elevation: 24,
  },
  chip: {
    alignSelf: 'flex-start',
    backgroundColor: colors.accentPrimarySoft,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
    marginBottom: spacing.md,
  },
  chipText: {
    ...typography.bodyM,
    color: colors.accentPrimary,
    fontWeight: '600',
    textTransform: 'uppercase',
  },
  contentRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  leftSection: {
    flex: 1,
  },
  centerSection: {
    flex: 1,
    alignItems: 'center',
  },
  rightSection: {
    flex: 1,
    alignItems: 'flex-end',
  },
  label: {
    ...typography.bodyM,
    color: colors.textSecondary,
  },
  timeLabel: {
    ...typography.bodyS,
    color: colors.textMuted,
    marginBottom: spacing.xs,
  },
  timeValue: {
    ...typography.numericLarge,
    color: colors.textPrimary,
  },
  countdownLabel: {
    ...typography.bodyS,
    color: colors.textMuted,
    marginBottom: spacing.xs,
  },
  countdownValue: {
    ...typography.numericMedium,
    color: colors.accentSecondary,
  },
  iqamahRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: spacing.lg,
    borderTopWidth: 1,
    borderTopColor: colors.divider,
  },
  iqamahLabel: {
    ...typography.bodyM,
    color: colors.textSecondary,
  },
  iqamahTime: {
    ...typography.numericSmall,
    color: colors.textPrimary,
  },
  noDataText: {
    ...typography.bodyM,
    color: colors.textMuted,
    textAlign: 'center',
  },
});
