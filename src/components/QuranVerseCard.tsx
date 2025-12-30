import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, Animated } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';
import { durations } from '../theme/motion';
import { AyatQuran } from '../types/AyatQuran';

interface QuranVerseCardProps {
  ayat: AyatQuran;
}

export const QuranVerseCard: React.FC<QuranVerseCardProps> = ({ ayat }) => {
  const fadeAnim = useRef(new Animated.Value(0)).current;

  // Fade in setiap ayat berubah
  useEffect(() => {
    Animated.timing(fadeAnim, {
      toValue: 1,
      duration: durations.medium,
      useNativeDriver: true,
    }).start();
  }, [ayat]);

  return (
    <Animated.View style={[styles.container, { opacity: fadeAnim }]}>
      <View style={styles.header}>
        <View style={styles.headerText}>
          <Text style={styles.surahName} numberOfLines={1} ellipsizeMode="tail">
            QS. {ayat.surah} ({ayat.surahNumber}): {ayat.ayah}
          </Text>
        </View>
      </View>

      <View style={styles.content}>
        <Text style={styles.arabicText} numberOfLines={3} ellipsizeMode="tail">
          {ayat.arabic}
        </Text>

        {ayat.transliteration && (
          <Text
            style={styles.transliterationText}
            numberOfLines={2}
            ellipsizeMode="tail"
          >
            {ayat.transliteration}
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
    paddingVertical: spacing.xl,
    paddingHorizontal: spacing.xxl,
    borderWidth: 1,
    borderColor: colors.accentSecondarySoft,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4,
    shadowRadius: 24,
    elevation: 8,
    flex: 1,
    overflow: 'hidden',
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
    marginTop: spacing.xs,
    marginBottom: spacing.md,
  },
  arabicText: {
    fontSize: 18,
    lineHeight: 32,
    color: colors.textPrimary,
    textAlign: 'right',
    fontWeight: '500',
    marginBottom: spacing.sm,
  },
  transliterationText: {
    ...typography.caption,
    color: colors.accentSecondary,
    fontStyle: 'italic',
    fontSize: 10,
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
