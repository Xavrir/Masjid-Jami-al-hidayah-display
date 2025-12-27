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
        <View style={styles.headerText}>
          <Text style={styles.surahName} numberOfLines={1} ellipsizeMode="tail">
            QS. {verse.surah} ({verse.surahNumber}): {verse.ayah}
          </Text>
        </View>
      </View>

      <View style={styles.content}>
        <Text style={styles.arabicText} numberOfLines={3} ellipsizeMode="tail">
          {verse.arabic}
        </Text>

        <View style={styles.divider} />

        <Text
          style={styles.translationText}
          numberOfLines={4}
          ellipsizeMode="tail">
          {verse.translation}
        </Text>

        {verse.transliteration && (
          <Text
            style={styles.transliterationText}
            numberOfLines={2}
            ellipsizeMode="tail">
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
    borderRadius: radii.medium,
    padding: spacing.lg,
    borderWidth: 1,
    borderColor: colors.accentSecondarySoft,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4,
    shadowRadius: 24,
    elevation: 8,
    flex: 1,
    minHeight: 0,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.md,
  },

  headerText: {
    flex: 1,
  },
  surahName: {
    ...typography.bodyM,
    color: colors.accentSecondary,
    fontWeight: '600',
    fontSize: 14,
  },
  content: {
    flex: 1,
    minHeight: 0,
    marginBottom: spacing.md,
  },
  arabicText: {
    fontSize: 20,
    lineHeight: 34,
    color: colors.textPrimary,
    textAlign: 'right',
    fontWeight: '500',
    marginBottom: spacing.md,
    includeFontPadding: false,
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
    marginBottom: spacing.sm,
    fontSize: 14,
    includeFontPadding: false,
  },
  transliterationText: {
    ...typography.caption,
    color: colors.textMuted,
    fontStyle: 'italic',
    lineHeight: 18,
    fontSize: 11,
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
    width: 40,
    height: 2,
    backgroundColor: colors.accentSecondary,
    borderRadius: radii.pill,
  },
});
