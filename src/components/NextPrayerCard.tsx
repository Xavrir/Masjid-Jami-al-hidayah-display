import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { Prayer } from '../types';

interface NextPrayerCardProps {
  prayer: Prayer | null;
  isTomorrow?: boolean;
}

export const NextPrayerCard: React.FC<NextPrayerCardProps> = ({
  prayer,
  isTomorrow = false,
}) => {
  if (!prayer) {
    return (
      <View style={styles.container}>
        <Text style={styles.noDataText}>Tidak ada jadwal salat berikutnya</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.chipRow}>
        <View style={styles.chip}>
          <Text style={styles.chipText}>{prayer.name}</Text>
        </View>
        {isTomorrow && (
          <View style={styles.tomorrowBadge}>
            <Text style={styles.tomorrowBadgeText}>Besok</Text>
          </View>
        )}
      </View>

      <View style={styles.contentRow}>
        <View style={styles.timeSection}>
          <Text style={styles.label}>Berikutnya</Text>
          <Text style={styles.timeLabel}>Adzan</Text>
          <Text style={styles.timeValue} numberOfLines={1}>
            {prayer.adhanTime}
          </Text>
        </View>

        <View style={styles.rightSection}>
          <Text style={styles.countdownLabel}>Dalam</Text>
          <Text style={styles.countdownValue} numberOfLines={1}>
            {prayer.countdown || '--:--'}
          </Text>
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
    borderRadius: radii.medium,
    padding: spacing.lg,
    borderWidth: 1,
    borderColor: colors.accentPrimarySoft,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4,
    shadowRadius: 24,
    elevation: 8,
    flex: 1,
    minHeight: 0,
  },
  chipRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    marginBottom: spacing.sm,
  },
  chip: {
    backgroundColor: colors.accentPrimarySoft,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
  },
  chipText: {
    ...typography.bodyS,
    color: colors.accentPrimary,
    fontWeight: '600',
    textTransform: 'uppercase',
    fontSize: 12,
  },
  tomorrowBadge: {
    backgroundColor: colors.accentSecondarySoft,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderWidth: 1,
    borderColor: colors.accentSecondary,
  },
  tomorrowBadgeText: {
    ...typography.caption,
    color: colors.accentSecondary,
    fontWeight: '700',
    fontSize: 10,
    textTransform: 'uppercase',
  },
  contentRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: spacing.md,
  },
  timeSection: {
    flex: 1,
    alignItems: 'flex-start',
  },
  rightSection: {
    flex: 1,
    alignItems: 'flex-end',
  },
  label: {
    ...typography.bodyS,
    color: colors.textSecondary,
    fontSize: 13,
  },
  timeLabel: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: 2,
    fontSize: 11,
  },
  timeValue: {
    fontSize: 32,
    fontWeight: '700',
    color: colors.textPrimary,
    includeFontPadding: false,
  },
  countdownLabel: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: 2,
    fontSize: 11,
  },
  countdownValue: {
    fontSize: 28,
    fontWeight: '700',
    color: colors.accentSecondary,
    includeFontPadding: false,
  },
  iqamahRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: spacing.md,
    borderTopWidth: 1,
    borderTopColor: colors.divider,
  },
  iqamahLabel: {
    ...typography.bodyS,
    color: colors.textSecondary,
    fontSize: 13,
  },
  iqamahTime: {
    fontSize: 20,
    fontWeight: '600',
    color: colors.textPrimary,
  },
  noDataText: {
    ...typography.bodyS,
    color: colors.textMuted,
    textAlign: 'center',
  },
});
