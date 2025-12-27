import React, { useState, useEffect } from 'react';
import { Pressable, SafeAreaView, StyleSheet, Text } from 'react-native';
import { MainDashboard } from './screens/MainDashboardEnhanced';
import { PrayerInProgress } from './screens/PrayerInProgress';
import { KasDetailOverlay } from './components/KasDetailOverlay';
import {
  mockMasjidConfig,
  mockKasData,
  mockAnnouncements,
} from './data/mockData';
import { Prayer } from './types';
import { colors } from './theme/colors';
import { typography } from './theme/typography';
import {
  calculatePrayerTimesForJakarta,
  isWithinPrayerWindow,
  getNextPrayer,
} from './utils/prayerTimesAdhan';

type Screen = 'dashboard' | 'prayer-in-progress';

const App: React.FC = () => {
  const [currentScreen, setCurrentScreen] = useState<Screen>('dashboard');
  const [currentPrayer, setCurrentPrayer] = useState<Prayer | null>(null);
  const [forcePrayerDebug, setForcePrayerDebug] = useState(false);
  const [appClock, setAppClock] = useState(new Date());
  const [kasOverlayVisible, setKasOverlayVisible] = useState(false);

  // TV Remote control handler
  // Note: TVEventHandler is deprecated in newer React Native versions
  // For production, implement proper TV navigation using react-native-tvos
  useEffect(() => {
    // Placeholder for TV event handling
    // Can be implemented with react-native-tvos or custom solution
    if (__DEV__) {
      console.log('TV event handler would be initialized here');
    }
  }, [currentScreen]);

  // Shared clock for gating overlays
  useEffect(() => {
    const timer = setInterval(() => setAppClock(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const handlePrayerStart = (prayer: Prayer) => {
    const now = new Date();
    if (!isWithinPrayerWindow(prayer, now)) {
      return;
    }
    setForcePrayerDebug(false);
    setCurrentPrayer(prayer);
    setCurrentScreen('prayer-in-progress');
  };

  const handlePrayerComplete = () => {
    setForcePrayerDebug(false);
    setCurrentScreen('dashboard');
    setCurrentPrayer(null);
  };

  // Auto-hide overlay when keluar dari jendela adzan (live mode)
  useEffect(() => {
    if (
      !currentPrayer ||
      currentScreen !== 'prayer-in-progress' ||
      forcePrayerDebug
    ) {
      return;
    }

    if (!isWithinPrayerWindow(currentPrayer, appClock)) {
      handlePrayerComplete();
    }
  }, [appClock, currentPrayer, currentScreen, forcePrayerDebug]);

  const triggerDebugPrayerOverlay = () => {
    const prayersToday = calculatePrayerTimesForJakarta(new Date());
    const next = getNextPrayer(prayersToday) || prayersToday[0];
    if (!next) return;

    setForcePrayerDebug(true);
    setCurrentPrayer({
      ...next,
      status: 'current',
    });
    setCurrentScreen('prayer-in-progress');
  };

  return (
    <SafeAreaView style={styles.container}>
      {currentScreen === 'dashboard' && (
        <MainDashboard
          masjidConfig={mockMasjidConfig}
          kasData={mockKasData}
          announcements={mockAnnouncements}
          onPrayerStart={handlePrayerStart}
        />
      )}

      {currentScreen === 'prayer-in-progress' && currentPrayer && (
        <PrayerInProgress
          prayer={currentPrayer}
          onComplete={handlePrayerComplete}
          masjidName={mockMasjidConfig.name}
          masjidLocation={mockMasjidConfig.location}
          forceDebug={forcePrayerDebug}
        />
      )}

      <KasDetailOverlay
        visible={kasOverlayVisible}
        kasData={mockKasData}
        onClose={() => setKasOverlayVisible(false)}
      />

      {currentScreen === 'dashboard' && (
        <Pressable
          style={styles.debugButton}
          onPress={triggerDebugPrayerOverlay}>
          <Text style={styles.debugText}>Force layar salat</Text>
        </Pressable>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  debugButton: {
    position: 'absolute',
    right: 18,
    bottom: 18,
    backgroundColor: colors.surfaceElevated,
    borderRadius: 999,
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: colors.borderSubtle,
    opacity: 0.82,
  },
  debugText: {
    ...typography.caption,
    color: colors.textSecondary,
    letterSpacing: 0.6,
  },
});

export default App;
