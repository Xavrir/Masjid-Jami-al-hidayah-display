import React, { useEffect, useRef, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Animated,
  Easing,
  ScrollView,
} from 'react-native';
import { colors } from '../theme/colors';
import { spacing, radii } from '../theme/spacing';

interface AnnouncementTickerProps {
  announcements: string[];
  speed?: 'slow' | 'normal';
}

export const AnnouncementTicker: React.FC<AnnouncementTickerProps> = ({
  announcements,
  speed = 'slow',
}) => {
  const scrollAnim = useRef(new Animated.Value(0)).current;
  const [textWidth, setTextWidth] = useState(0);

  const combinedText =
    announcements.length > 0 ? announcements.join(' • ') : '';

  useEffect(() => {
    if (textWidth === 0 || !combinedText) {
      return;
    }

    const pixelsPerSecond = speed === 'slow' ? 50 : 100;
    const duration = (textWidth / pixelsPerSecond) * 1000;

    scrollAnim.setValue(0);

    const animation = Animated.loop(
      Animated.timing(scrollAnim, {
        toValue: -textWidth,
        duration: duration,
        easing: Easing.linear,
        useNativeDriver: true,
      })
    );

    animation.start();

    return () => {
      animation.stop();
    };
  }, [textWidth, speed, scrollAnim, combinedText]);

  return (
    <View style={styles.container}>
      <View style={styles.textContainer}>
        <ScrollView
          horizontal
          scrollEnabled={false}
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={{ flexGrow: 1, alignItems: 'center' }}>
          <Animated.View
            style={[
              styles.scrollingTextContainer,
              {
                transform: [{ translateX: scrollAnim }],
              },
            ]}>
            <View
              style={{ flexDirection: 'row', alignItems: 'center' }}
              onLayout={e => {
                const width = e.nativeEvent.layout.width;
                if (width > 0 && Math.abs(width - textWidth) > 1) {
                  setTextWidth(width);
                }
              }}>
              <Text style={styles.text}>{combinedText}</Text>
              <View style={{ width: 100 }} />
            </View>

            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <Text style={styles.text}>{combinedText}</Text>
              <View style={{ width: 100 }} />
            </View>
          </Animated.View>
        </ScrollView>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    height: 56,
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
    fontSize: 24,
    color: colors.textPrimary,
    fontWeight: '500',
    letterSpacing: 0.3,
  },
});
