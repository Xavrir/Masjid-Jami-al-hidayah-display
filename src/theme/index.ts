import { colors, ramadanColors } from './colors';
import { typography } from './typography';
import { spacing, radii, safeAreaMargins } from './spacing';
import { durations, easings } from './motion';

export { colors, ramadanColors };
export { typography };
export { spacing, radii, safeAreaMargins };
export { durations, easings };

export const theme = {
  colors,
  typography,
  spacing,
  radii,
  safeAreaMargins,
  durations,
  easings,
};

export type Theme = typeof theme;
