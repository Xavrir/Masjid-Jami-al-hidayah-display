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
      <View style={styles.iconContainer}>
        <Text style={styles.icon}>ℹ️</Text>
      </View>

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
    height: 72,
    backgroundColor: colors.surfaceElevated,
    borderRadius: radii.pill,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.xxl,
    borderWidth: 1,
    borderColor: colors.divider,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.55,
    shadowRadius: 32,
    elevation: 12,
    overflow: 'hidden',
  },
  iconContainer: {
    marginRight: spacing.lg,
  },
  icon: {
    fontSize: 24,
  },
  textContainer: {
    flex: 1,
    overflow: 'hidden',
  },
  scrollingTextContainer: {
    flexDirection: 'row',
  },
  text: {
    ...typography.bodyM,
    color: colors.textPrimary,
  },
});
