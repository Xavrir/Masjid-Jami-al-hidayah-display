import React, { useState, useEffect } from 'react';
import { SafeAreaView, StyleSheet } from 'react-native';
import { MainDashboard } from './screens/MainDashboardEnhanced';
import { PrayerInProgress } from './screens/PrayerInProgress';
import { KasDetailOverlay } from './components/KasDetailOverlay';
import { mockMasjidConfig, mockKasData, mockAnnouncements } from './data/mockData';
import { Prayer } from './types';
import { colors } from './theme/colors';

type Screen = 'dashboard' | 'prayer-in-progress';

const App: React.FC = () => {
  const [currentScreen, setCurrentScreen] = useState<Screen>('dashboard');
  const [currentPrayer, setCurrentPrayer] = useState<Prayer | null>(null);
  const [kasOverlayVisible, setKasOverlayVisible] = useState(false);

  // TV Remote control handler
  // Note: TVEventHandler is deprecated in newer React Native versions
  // For production, implement proper TV navigation using react-native-tvos
  useEffect(() => {
    // Placeholder for TV event handling
    // Can be implemented with react-native-tvos or custom solution
    console.log('TV event handler would be initialized here');
  }, [currentScreen]);

  const handlePrayerStart = (prayer: Prayer) => {
    setCurrentPrayer(prayer);
    setCurrentScreen('prayer-in-progress');
  };

  const handlePrayerComplete = () => {
    setCurrentScreen('dashboard');
    setCurrentPrayer(null);
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
        />
      )}

      <KasDetailOverlay
        visible={kasOverlayVisible}
        kasData={mockKasData}
        onClose={() => setKasOverlayVisible(false)}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
});

export default App;
