import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { IslamicStudy, getTodayStudies, getUpcomingStudies } from '../data/islamicContent';

interface IslamicStudiesCardProps {
  showTodayOnly?: boolean;
}

export const IslamicStudiesCard: React.FC<IslamicStudiesCardProps> = ({
  showTodayOnly = false,
}) => {
  const studies = showTodayOnly ? getTodayStudies() : getUpcomingStudies();

  const getCategoryIcon = (category: IslamicStudy['category']): string => {
    switch (category) {
      case 'kajian':
        return '🎓';
      case 'tahfidz':
        return '📿';
      case 'tpa':
        return '📚';
      case 'halaqah':
        return '👥';
      case 'daurah':
        return '🌟';
      default:
        return '📖';
    }
  };

  const getCategoryColor = (category: IslamicStudy['category']): string => {
    switch (category) {
      case 'kajian':
        return colors.accentPrimary;
      case 'tahfidz':
        return colors.accentSecondary;
      case 'tpa':
        return '#3498DB';
      case 'halaqah':
        return '#9B59B6';
      case 'daurah':
        return '#E67E22';
      default:
        return colors.textSecondary;
    }
  };

  if (studies.length === 0) {
    return (
      <View style={styles.container}>
        <View style={styles.header}>
          <Text style={styles.headerIcon}>📅</Text>
          <Text style={styles.headerTitle}>
            {showTodayOnly ? 'Kajian Hari Ini' : 'Info Pengajian'}
          </Text>
        </View>
        <Text style={styles.noDataText}>
          Tidak ada kajian/pengajian yang terjadwal hari ini.
        </Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerIcon}>📅</Text>
        <Text style={styles.headerTitle}>
          {showTodayOnly ? 'Kajian Hari Ini' : 'Info Pengajian'}
        </Text>
      </View>

      <ScrollView
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}
        nestedScrollEnabled={true}
      >
        {studies.map((study, index) => (
          <View
            key={study.id}
            style={[
              styles.studyItem,
              index === studies.length - 1 && styles.lastStudyItem,
            ]}
          >
            <View style={styles.studyHeader}>
              <Text style={styles.categoryIcon}>{getCategoryIcon(study.category)}</Text>
              <View style={styles.studyHeaderText}>
                <Text style={styles.studyTitle}>{study.title}</Text>
                <View
                  style={[
                    styles.categoryBadge,
                    { backgroundColor: getCategoryColor(study.category) + '33' },
                  ]}
                >
                  <Text
                    style={[
                      styles.categoryText,
                      { color: getCategoryColor(study.category) },
                    ]}
                  >
                    {study.category.toUpperCase()}
                  </Text>
                </View>
              </View>
            </View>

            <View style={styles.studyDetails}>
              <View style={styles.detailRow}>
                <Text style={styles.detailIcon}>👤</Text>
                <Text style={styles.detailText}>{study.instructor}</Text>
              </View>

              <View style={styles.detailRow}>
                <Text style={styles.detailIcon}>🕒</Text>
                <Text style={styles.detailText}>{study.schedule}</Text>
              </View>

              <View style={styles.detailRow}>
                <Text style={styles.detailIcon}>📍</Text>
                <Text style={styles.detailText}>{study.location}</Text>
              </View>

              {study.description && (
                <Text style={styles.description}>{study.description}</Text>
              )}
            </View>
          </View>
        ))}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surfaceGlass,
    borderRadius: radii.large,
    padding: spacing.xl,
    borderWidth: 1,
    borderColor: colors.borderSubtle,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.55,
    shadowRadius: 32,
    elevation: 12,
    maxHeight: 600,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.lg,
    paddingBottom: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  headerIcon: {
    fontSize: 28,
    marginRight: spacing.md,
  },
  headerTitle: {
    ...typography.titleM,
    color: colors.textPrimary,
    fontWeight: '600',
  },
  scrollView: {
    flex: 1,
  },
  studyItem: {
    paddingBottom: spacing.lg,
    marginBottom: spacing.lg,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  lastStudyItem: {
    borderBottomWidth: 0,
    marginBottom: 0,
  },
  studyHeader: {
    flexDirection: 'row',
    marginBottom: spacing.md,
  },
  categoryIcon: {
    fontSize: 24,
    marginRight: spacing.md,
  },
  studyHeaderText: {
    flex: 1,
  },
  studyTitle: {
    ...typography.titleS,
    color: colors.textPrimary,
    marginBottom: spacing.xs,
  },
  categoryBadge: {
    alignSelf: 'flex-start',
    borderRadius: radii.small,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
  },
  categoryText: {
    ...typography.caption,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  studyDetails: {
    paddingLeft: 36,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  detailIcon: {
    fontSize: 16,
    marginRight: spacing.sm,
    width: 20,
  },
  detailText: {
    ...typography.bodyM,
    color: colors.textSecondary,
    flex: 1,
  },
  description: {
    ...typography.bodyS,
    color: colors.textMuted,
    marginTop: spacing.sm,
    fontStyle: 'italic',
    lineHeight: 20,
  },
  noDataText: {
    ...typography.bodyM,
    color: colors.textMuted,
    textAlign: 'center',
    paddingVertical: spacing.xl,
  },
});
