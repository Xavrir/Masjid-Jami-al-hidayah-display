import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { KasData } from '../types';

interface KasSummaryProps {
  kasData: KasData;
  variant?: 'compact_with_sparkline' | 'simple';
}

export const KasSummary: React.FC<KasSummaryProps> = ({
  kasData,
  variant = 'compact_with_sparkline'
}) => {
  const getBalanceColor = () => {
    if (kasData.balance > 0) return colors.kasPositive;
    if (kasData.balance < 0) return colors.kasNegative;
    return colors.kasNeutral;
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const getTrendIcon = () => {
    switch (kasData.trendDirection) {
      case 'up':
        return '↑';
      case 'down':
        return '↓';
      default:
        return '→';
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Kas Masjid</Text>

      <View style={styles.balanceSection}>
        <Text style={styles.balanceLabel}>Saldo Saat Ini</Text>
        <Text style={[styles.balanceValue, { color: getBalanceColor() }]}>
          {formatCurrency(kasData.balance)}
        </Text>
        <View style={styles.trendRow}>
          <Text style={styles.trendIcon}>{getTrendIcon()}</Text>
          <Text style={styles.trendText}>
            {kasData.trendDirection === 'up' && 'Meningkat'}
            {kasData.trendDirection === 'down' && 'Menurun'}
            {kasData.trendDirection === 'flat' && 'Stabil'}
          </Text>
        </View>
      </View>

      <View style={styles.statsRow}>
        <View style={styles.statItem}>
          <Text style={styles.statLabel}>Pemasukan Bulan Ini</Text>
          <Text style={[styles.statValue, { color: colors.kasPositive }]}>
            {formatCurrency(kasData.incomeMonth)}
          </Text>
        </View>

        <View style={styles.dividerVertical} />

        <View style={styles.statItem}>
          <Text style={styles.statLabel}>Pengeluaran Bulan Ini</Text>
          <Text style={[styles.statValue, { color: colors.kasNegative }]}>
            {formatCurrency(kasData.expenseMonth)}
          </Text>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surfaceGlass,
    borderRadius: radii.medium,
    padding: spacing.lg,
    borderWidth: 1,
    borderColor: colors.borderSubtle,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4,
    shadowRadius: 24,
    elevation: 8,
    maxHeight: 180,
  },
  title: {
    ...typography.bodyM,
    color: colors.textPrimary,
    marginBottom: spacing.md,
    fontWeight: '600',
    fontSize: 16,
  },
  balanceSection: {
    alignItems: 'center',
    marginBottom: spacing.md,
    paddingBottom: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  balanceLabel: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: spacing.xs,
    fontSize: 11,
  },
  balanceValue: {
    fontSize: 28,
    fontWeight: '700',
    marginBottom: spacing.xs,
  },
  trendRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  trendIcon: {
    fontSize: 16,
    color: colors.accentSecondary,
    marginRight: spacing.xs,
  },
  trendText: {
    ...typography.caption,
    color: colors.textSecondary,
    fontSize: 11,
  },
  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  statItem: {
    flex: 1,
    alignItems: 'center',
  },
  statLabel: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: spacing.xs,
    textAlign: 'center',
    fontSize: 10,
  },
  statValue: {
    fontSize: 16,
    fontWeight: '600',
  },
  dividerVertical: {
    width: 1,
    backgroundColor: colors.divider,
    marginHorizontal: spacing.md,
  },
});
