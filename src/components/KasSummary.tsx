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
    padding: spacing.xl,
    borderWidth: 1,
    borderColor: colors.borderSubtle,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.55,
    shadowRadius: 32,
    elevation: 12,
  },
  title: {
    ...typography.titleM,
    color: colors.textPrimary,
    marginBottom: spacing.lg,
  },
  balanceSection: {
    alignItems: 'center',
    marginBottom: spacing.xl,
    paddingBottom: spacing.lg,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  balanceLabel: {
    ...typography.bodyS,
    color: colors.textMuted,
    marginBottom: spacing.sm,
  },
  balanceValue: {
    ...typography.displayL,
    fontWeight: '700',
    marginBottom: spacing.sm,
  },
  trendRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  trendIcon: {
    ...typography.titleM,
    color: colors.accentSecondary,
    marginRight: spacing.xs,
  },
  trendText: {
    ...typography.bodyS,
    color: colors.textSecondary,
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
    ...typography.bodyS,
    color: colors.textMuted,
    marginBottom: spacing.sm,
    textAlign: 'center',
  },
  statValue: {
    ...typography.numericSmall,
    fontWeight: '600',
  },
  dividerVertical: {
    width: 1,
    backgroundColor: colors.divider,
    marginHorizontal: spacing.lg,
  },
});
