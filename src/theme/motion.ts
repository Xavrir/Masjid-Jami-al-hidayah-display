export const durations = {
  instant: 80,
  fast: 180,
  medium: 280,
  slow: 420,
  verySlow: 650,
};

export const easings = {
  standard: [0.4, 0.0, 0.2, 1] as const,
  emphasized: [0.2, 0.0, 0.0, 1] as const,
  decelerate: [0.0, 0.0, 0.2, 1] as const,
};
