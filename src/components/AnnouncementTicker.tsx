import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, Animated, Dimensions } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing, radii } from '../theme/spacing';

interface AnnouncementTickerProps {
  announcements: string[];
  speed?: 'slow' | 'normal';
}

export const AnnouncementTicker: React.FC<AnnouncementTickerProps> = ({
  announcements,
  speed = 'slow'
}) => {
  const scrollAnim = useRef(new Animated.Value(0)).current;
  const screenWidth = Dimensions.get('window').width;

  useEffect(() => {
    const duration = speed === 'slow' ? 60000 : 40000;

    const animate = () => {
      scrollAnim.setValue(0);
      Animated.loop(
        Animated.timing(scrollAnim, {
          toValue: -screenWidth * 2,
          duration,
          useNativeDriver: true,
        })
      ).start();
    };

    animate();
  }, [announcements, speed, scrollAnim, screenWidth]);

  const combinedText = announcements.join('  •  ');

  return (
    <View style={styles.container}>
      <View style={styles.textContainer}>
        <Animated.View
          style={[
            styles.scrollingTextContainer,
            {
              transform: [{ translateX: scrollAnim }],
            },
          ]}
        >
          <Text style={styles.text}>{combinedText}  •  {combinedText}</Text>
        </Animated.View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    height: 48,
    backgroundColor: 'rgba(21, 32, 43, 0.85)',
    borderRadius: radii.small,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.lg,
    borderWidth: 1,
    borderColor: 'rgba(212, 175, 55, 0.25)',
    overflow: 'hidden',
  },
  textContainer: {
    flex: 1,
    overflow: 'hidden',
  },
  scrollingTextContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  text: {
    ...typography.bodyM,
    color: colors.textPrimary,
    fontWeight: '500',
    letterSpacing: 0.3,
    lineHeight: 24,
  },
});
