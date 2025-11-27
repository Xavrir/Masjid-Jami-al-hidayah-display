import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Animated } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { QuranVerse, getRandomQuranVerse } from '../data/islamicContent';
import { durations } from '../theme/motion';

interface QuranVerseCardProps {
  autoRotate?: boolean;
  rotationInterval?: number; // in milliseconds
}

export const QuranVerseCard: React.FC<QuranVerseCardProps> = ({
  autoRotate = true,
  rotationInterval = 30000, // 30 seconds
}) => {
  const [verse, setVerse] = useState<QuranVerse>(getRandomQuranVerse());
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
        // Change verse
        setVerse(getRandomQuranVerse());

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
        <Text style={styles.icon}>📖</Text>
        <View style={styles.headerText}>
          <Text style={styles.surahName}>
            QS. {verse.surah} ({verse.surahNumber}): {verse.ayah}
          </Text>
        </View>
      </View>

      <View style={styles.content}>
        <Text style={styles.arabicText}>{verse.arabic}</Text>

        <View style={styles.divider} />

        <Text style={styles.translationText}>{verse.translation}</Text>

        {verse.transliteration && (
          <Text style={styles.transliterationText}>
            <Text style={styles.transliterationLabel}>Transliterasi: </Text>
            {verse.transliteration}
          </Text>
        )}
      </View>

      <View style={styles.footer}>
        <View style={styles.decorativeLine} />
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
    borderColor: colors.accentSecondarySoft,
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
  surahName: {
    ...typography.titleS,
    color: colors.accentSecondary,
    fontWeight: '600',
  },
  content: {
    marginBottom: spacing.lg,
  },
  arabicText: {
    fontSize: 24,
    lineHeight: 40,
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
    marginBottom: spacing.md,
  },
  transliterationText: {
    ...typography.bodyS,
    color: colors.textMuted,
    fontStyle: 'italic',
    lineHeight: 22,
  },
  transliterationLabel: {
    color: colors.accentSecondary,
    fontStyle: 'normal',
    fontWeight: '600',
  },
  footer: {
    alignItems: 'center',
  },
  decorativeLine: {
    width: 60,
    height: 3,
    backgroundColor: colors.accentSecondary,
    borderRadius: radii.pill,
  },
});
