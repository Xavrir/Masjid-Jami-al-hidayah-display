export type PrayerStatus = 'passed' | 'current' | 'upcoming';

export interface Prayer {
  name: string;
  adhanTime: string;
  iqamahTime: string;
  status: PrayerStatus;
  countdown?: string;
  /**
   * Optional display window length (minutes) counted from adhan.
   * Controls how long the "Sedang Berlangsung" view stays active.
   */
  windowMinutes?: number;
}

export interface KasTransaction {
  id: string;
  date: string;
  description: string;
  amount: number;
  type: 'income' | 'expense';
}

export interface KasData {
  balance: number;
  incomeMonth: number;
  expenseMonth: number;
  trendDirection: 'up' | 'down' | 'flat';
  recentTransactions: KasTransaction[];
  trendData: number[];
}

export interface MasjidConfig {
  name: string;
  location: string;
  tagline?: string;
  coordinates: {
    latitude: number;
    longitude: number;
  };
  calculationMethod: string;
}

