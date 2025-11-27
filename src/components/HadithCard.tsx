import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Animated } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { Hadith, getRandomHadith } from '../data/islamicContent';
import { durations } from '../theme/motion';

interface HadithCardProps {
  autoRotate?: boolean;
  rotationInterval?: number; // in milliseconds
}

export const HadithCard: React.FC<HadithCardProps> = ({
  autoRotate = true,
  rotationInterval = 45000, // 45 seconds
}) => {
  const [hadith, setHadith] = useState<Hadith>(getRandomHadith());
  const fadeAnim = new Animated.Value(1);

  useEffect(() => {
    if (!autoRotate) return;

    const interval = setInterval(() => {
      // Fade out
      Animated.timing(fadeAnim, {
        toValue: 0,
        duration: durations.medium,
        useNativeDriver: true,
      }).start(() => {
        // Change hadith
        setHadith(getRandomHadith());

        // Fade in
        Animated.timing(fadeAnim, {
          toValue: 1,
          duration: durations.medium,
          useNativeDriver: true,
        }).start();
      });
    }, rotationInterval);

    return () => clearInterval(interval);
  }, [autoRotate, rotationInterval]);

  return (
    <Animated.View style={[styles.container, { opacity: fadeAnim }]}>
      <View style={styles.header}>
        <Text style={styles.icon}>📜</Text>
        <View style={styles.headerText}>
          <Text style={styles.title}>Hadits Pilihan</Text>
          <Text style={styles.category}>{hadith.category}</Text>
        </View>
      </View>

      <View style={styles.content}>
        <Text style={styles.arabicText}>{hadith.arabic}</Text>

        <View style={styles.divider} />

        <Text style={styles.translationText}>{hadith.translation}</Text>
      </View>

      <View style={styles.footer}>
        <View style={styles.sourceContainer}>
          <Text style={styles.narrator}>Dari: {hadith.narrator}</Text>
          <Text style={styles.source}>{hadith.source}</Text>
        </View>
      </View>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surfaceGlass,
    borderRadius: radii.medium,
    padding: spacing.lg,
    borderWidth: 1,
    borderColor: colors.accentPrimarySoft,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4,
    shadowRadius: 24,
    elevation: 8,
    maxHeight: 220,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.md,
  },
  icon: {
    fontSize: 22,
    marginRight: spacing.sm,
  },
  headerText: {
    flex: 1,
  },
  title: {
    ...typography.bodyM,
    color: colors.accentPrimary,
    fontWeight: '600',
    marginBottom: 2,
    fontSize: 14,
  },
  category: {
    ...typography.caption,
    color: colors.textMuted,
    textTransform: 'uppercase',
    fontSize: 10,
  },
  content: {
    marginBottom: spacing.md,
  },
  arabicText: {
    fontSize: 19,
    lineHeight: 32,
    color: colors.textPrimary,
    textAlign: 'right',
    fontWeight: '500',
    marginBottom: spacing.md,
  },
  divider: {
    height: 1,
    backgroundColor: colors.divider,
    marginVertical: spacing.sm,
  },
  translationText: {
    ...typography.bodyM,
    color: colors.textSecondary,
    lineHeight: 24,
    fontSize: 14,
  },
  footer: {
    borderTopWidth: 1,
    borderTopColor: colors.divider,
    paddingTop: spacing.sm,
  },
  sourceContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  narrator: {
    ...typography.caption,
    color: colors.textMuted,
    flex: 1,
    fontSize: 11,
  },
  source: {
    ...typography.caption,
    color: colors.accentPrimary,
    fontWeight: '600',
    fontSize: 11,
  },
});
