import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Modal,
} from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { KasData, KasTransaction } from '../types';
import { formatCurrency } from '../utils/currency';

interface KasDetailOverlayProps {
  visible: boolean;
  kasData: KasData;
  onClose: () => void;
}

export const KasDetailOverlay: React.FC<KasDetailOverlayProps> = ({
  visible,
  kasData,
  onClose,
}) => {
  const getBalanceColor = () => {
    if (kasData.balance > 0) return colors.kasPositive;
    if (kasData.balance < 0) return colors.kasNegative;
    return colors.kasNeutral;
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
    <Modal
      visible={visible}
      transparent
      animationType="slide"
      onRequestClose={onClose}>
      <View style={styles.backdrop}>
        <View style={styles.container}>
          {/* Header */}
          <View style={styles.header}>
            <View style={styles.headerLeft}>
              <Text style={styles.headerTitle}>Ringkasan Kas Masjid</Text>
            </View>
            <TouchableOpacity onPress={onClose} style={styles.closeButton}>
              <Text style={styles.closeButtonText}>X</Text>
            </TouchableOpacity>
          </View>

          <ScrollView
            style={styles.scrollView}
            showsVerticalScrollIndicator={false}>
            {/* Balance Highlight */}
            <View style={styles.balanceCard}>
              <Text style={styles.balanceLabel}>Saldo Saat Ini</Text>
              <Text style={[styles.balanceValue, { color: getBalanceColor() }]}>
                {formatCurrency(kasData.balance)}
              </Text>
              <View style={styles.trendRow}>
                <Text style={styles.trendIcon}>{getTrendIcon()}</Text>
                <Text style={styles.trendText}>
                  {kasData.trendDirection === 'up' &&
                    'Meningkat dari bulan lalu'}
                  {kasData.trendDirection === 'down' &&
                    'Menurun dari bulan lalu'}
                  {kasData.trendDirection === 'flat' && 'Stabil'}
                </Text>
              </View>
            </View>

            {/* Income / Expense Row */}
            <View style={styles.statsRow}>
              <View style={[styles.statCard, styles.incomeCard]}>
                <Text style={styles.statLabel}>Total Pemasukan Bulan Ini</Text>
                <Text style={[styles.statValue, { color: colors.kasPositive }]}>
                  {formatCurrency(kasData.incomeMonth)}
                </Text>
              </View>

              <View style={[styles.statCard, styles.expenseCard]}>
                <Text style={styles.statLabel}>
                  Total Pengeluaran Bulan Ini
                </Text>
                <Text style={[styles.statValue, { color: colors.kasNegative }]}>
                  {formatCurrency(kasData.expenseMonth)}
                </Text>
              </View>
            </View>

            {/* Sparkline Section */}
            <View style={styles.sparklineCard}>
              <Text style={styles.sectionTitle}>Trend 30 Hari Terakhir</Text>
              <View style={styles.sparklineContainer}>
                <Text style={styles.sparklinePlaceholder}>
                  Grafik trend kas ditampilkan di sini
                </Text>
              </View>
            </View>

            {/* Recent Transactions */}
            <View style={styles.transactionsCard}>
              <Text style={styles.sectionTitle}>Transaksi Terbaru</Text>

              {kasData.recentTransactions.map(transaction => (
                <View key={transaction.id} style={styles.transactionRow}>
                  <View style={styles.transactionLeft}>
                    <Text style={styles.transactionDate}>
                      {transaction.date}
                    </Text>
                    <Text style={styles.transactionDescription}>
                      {transaction.description}
                    </Text>
                  </View>

                  <Text
                    style={[
                      styles.transactionAmount,
                      {
                        color:
                          transaction.type === 'income'
                            ? colors.kasPositive
                            : colors.kasNegative,
                      },
                    ]}>
                    {transaction.type === 'income' ? '+' : '-'}
                    {formatCurrency(Math.abs(transaction.amount))}
                  </Text>
                </View>
              ))}
            </View>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.65)',
    justifyContent: 'center',
    alignItems: 'flex-end',
  },
  container: {
    width: '40%',
    height: '100%',
    backgroundColor: colors.surfaceElevated,
    shadowColor: '#000',
    shadowOffset: { width: -8, height: 0 },
    shadowOpacity: 0.5,
    shadowRadius: 24,
    elevation: 24,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: spacing.xl,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  headerIcon: {
    fontSize: 32,
    marginRight: spacing.md,
  },
  headerTitle: {
    ...typography.titleM,
    color: colors.textPrimary,
  },
  closeButton: {
    width: 40,
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: radii.small,
    backgroundColor: colors.surfaceDefault,
  },
  closeButtonText: {
    ...typography.titleM,
    color: colors.textSecondary,
  },
  scrollView: {
    flex: 1,
    padding: spacing.xl,
  },
  balanceCard: {
    backgroundColor: colors.surfaceGlass,
    borderRadius: radii.medium,
    padding: spacing.xl,
    borderWidth: 1,
    borderColor: colors.accentSecondarySoft,
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  balanceLabel: {
    ...typography.bodyS,
    color: colors.textMuted,
    marginBottom: spacing.sm,
  },
  balanceValue: {
    ...typography.numericLarge,
    fontWeight: '700',
    marginBottom: spacing.md,
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
    gap: spacing.lg,
    marginBottom: spacing.lg,
  },
  statCard: {
    flex: 1,
    backgroundColor: colors.surfaceDefault,
    borderRadius: radii.medium,
    padding: spacing.lg,
    borderWidth: 2,
  },
  incomeCard: {
    borderColor: colors.kasPositive,
  },
  expenseCard: {
    borderColor: colors.kasNegative,
  },
  statLabel: {
    ...typography.bodyS,
    color: colors.textMuted,
    marginBottom: spacing.md,
  },
  statValue: {
    ...typography.numericMedium,
    fontWeight: '600',
  },
  sparklineCard: {
    backgroundColor: colors.surfaceDefault,
    borderRadius: radii.medium,
    padding: spacing.lg,
    marginBottom: spacing.lg,
  },
  sectionTitle: {
    ...typography.titleS,
    color: colors.textPrimary,
    marginBottom: spacing.md,
  },
  sparklineContainer: {
    height: 80,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.surfaceElevated,
    borderRadius: radii.small,
  },
  sparklinePlaceholder: {
    ...typography.bodyS,
    color: colors.textMuted,
  },
  transactionsCard: {
    backgroundColor: colors.surfaceDefault,
    borderRadius: radii.medium,
    padding: spacing.lg,
  },
  transactionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  transactionLeft: {
    flex: 1,
  },
  transactionDate: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: spacing.xs,
  },
  transactionDescription: {
    ...typography.bodyM,
    color: colors.textPrimary,
  },
  transactionAmount: {
    ...typography.numericSmall,
    fontWeight: '600',
  },
});
