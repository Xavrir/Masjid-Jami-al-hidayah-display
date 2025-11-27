import { TextStyle, Platform } from 'react-native';

// Professional font families - using system defaults with fallbacks
// For production, replace with custom fonts using react-native-vector-icons or custom font files
const fontFamilies = {
  primary: Platform.select({
    ios: 'SF Pro Display',
    android: 'Roboto',
    default: 'System',
  }),
  numeric: Platform.select({
    ios: 'SF Pro Display',
    android: 'Roboto',
    default: 'System',
  }),
};

export const typography = {
  displayXL: {
    fontFamily: fontFamilies.primary,
    fontSize: 72,
    lineHeight: 80,
    fontWeight: '700' as TextStyle['fontWeight'],
    letterSpacing: -0.5,
  },
  displayL: {
    fontFamily: fontFamilies.numeric,
    fontSize: 56,
    lineHeight: 64,
    fontWeight: '700' as TextStyle['fontWeight'],
    letterSpacing: -0.5,
  },
  headlineXL: {
    fontFamily: fontFamilies.primary,
    fontSize: 40,
    lineHeight: 48,
    fontWeight: '700' as TextStyle['fontWeight'],
    letterSpacing: -0.2,
  },
  headlineL: {
    fontFamily: fontFamilies.primary,
    fontSize: 32,
    lineHeight: 40,
    fontWeight: '600' as TextStyle['fontWeight'],
    letterSpacing: -0.2,
  },
  headlineM: {
    fontFamily: fontFamilies.primary,
    fontSize: 24,
    lineHeight: 32,
    fontWeight: '600' as TextStyle['fontWeight'],
    letterSpacing: 0,
  },
  titleM: {
    fontFamily: fontFamilies.primary,
    fontSize: 20,
    lineHeight: 28,
    fontWeight: '600' as TextStyle['fontWeight'],
    letterSpacing: 0.3,
  },
  titleS: {
    fontFamily: fontFamilies.primary,
    fontSize: 18,
    lineHeight: 24,
    fontWeight: '600' as TextStyle['fontWeight'],
    letterSpacing: 0.3,
  },
  bodyL: {
    fontFamily: fontFamilies.primary,
    fontSize: 18,
    lineHeight: 26,
    fontWeight: '400' as TextStyle['fontWeight'],
    letterSpacing: 0.2,
  },
  bodyM: {
    fontFamily: fontFamilies.primary,
    fontSize: 16,
    lineHeight: 24,
    fontWeight: '400' as TextStyle['fontWeight'],
    letterSpacing: 0.2,
  },
  bodyS: {
    fontFamily: fontFamilies.primary,
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '400' as TextStyle['fontWeight'],
    letterSpacing: 0.2,
  },
  caption: {
    fontFamily: fontFamilies.primary,
    fontSize: 12,
    lineHeight: 16,
    fontWeight: '500' as TextStyle['fontWeight'],
    letterSpacing: 0.5,
    textTransform: 'uppercase' as TextStyle['textTransform'],
  },
  numericLarge: {
    fontFamily: fontFamilies.numeric,
    fontSize: 48,
    lineHeight: 56,
    fontWeight: '700' as TextStyle['fontWeight'],
    letterSpacing: -0.5,
  },
  numericMedium: {
    fontFamily: fontFamilies.numeric,
    fontSize: 32,
    lineHeight: 40,
    fontWeight: '700' as TextStyle['fontWeight'],
    letterSpacing: -0.3,
  },
  numericSmall: {
    fontFamily: fontFamilies.numeric,
    fontSize: 18,
    lineHeight: 24,
    fontWeight: '600' as TextStyle['fontWeight'],
    letterSpacing: 0,
  },
};
