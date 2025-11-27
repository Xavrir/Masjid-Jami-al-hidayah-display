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
    borderRadius: radii.large,
    padding: spacing.xl,
    borderWidth: 1,
    borderColor: colors.accentPrimarySoft,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.55,
    shadowRadius: 32,
    elevation: 12,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  icon: {
    fontSize: 28,
    marginRight: spacing.md,
  },
  headerText: {
    flex: 1,
  },
  title: {
    ...typography.titleS,
    color: colors.accentPrimary,
    fontWeight: '600',
    marginBottom: spacing.xs,
  },
  category: {
    ...typography.caption,
    color: colors.textMuted,
    textTransform: 'uppercase',
  },
  content: {
    marginBottom: spacing.lg,
  },
  arabicText: {
    fontSize: 22,
    lineHeight: 38,
    color: colors.textPrimary,
    textAlign: 'right',
    fontWeight: '500',
    marginBottom: spacing.lg,
  },
  divider: {
    height: 1,
    backgroundColor: colors.divider,
    marginVertical: spacing.md,
  },
  translationText: {
    ...typography.bodyL,
    color: colors.textSecondary,
    lineHeight: 28,
  },
  footer: {
    borderTopWidth: 1,
    borderTopColor: colors.divider,
    paddingTop: spacing.md,
  },
  sourceContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  narrator: {
    ...typography.bodyS,
    color: colors.textMuted,
    flex: 1,
  },
  source: {
    ...typography.bodyS,
    color: colors.accentPrimary,
    fontWeight: '600',
  },
});
