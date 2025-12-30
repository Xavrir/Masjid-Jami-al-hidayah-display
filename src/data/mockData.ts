import { KasData, MasjidConfig, KasTransaction } from '../types';

export const mockMasjidConfig: MasjidConfig = {
  name: "Masjid Jami' Al-Hidayah",
  location: 'Jl. Tanah Merdeka II No.8, Rambutan, Ciracas, Jakarta Timur 13830',
  tagline: 'Memakmurkan Masjid, Mencerahkan Umat',
  coordinates: {
    latitude: -6.3140892,
    longitude: 106.8776666,
  },
  calculationMethod: 'Kemenag RI',
};

const mockKasTransactions: KasTransaction[] = [
  {
    id: '1',
    date: '2025-11-26',
    description: 'Infaq Jumat',
    amount: 2500000,
    type: 'income',
  },
  {
    id: '2',
    date: '2025-11-25',
    description: 'Listrik Bulan November',
    amount: -850000,
    type: 'expense',
  },
  {
    id: '3',
    date: '2025-11-24',
    description: 'Donasi Umum',
    amount: 1500000,
    type: 'income',
  },
  {
    id: '4',
    date: '2025-11-23',
    description: 'Kebersihan dan Pemeliharaan',
    amount: -500000,
    type: 'expense',
  },
  {
    id: '5',
    date: '2025-11-22',
    description: 'Infaq Jumat',
    amount: 2800000,
    type: 'income',
  },
  {
    id: '6',
    date: '2025-11-21',
    description: 'Air PDAM',
    amount: -320000,
    type: 'expense',
  },
];

export const mockKasData: KasData = {
  balance: 45250000,
  incomeMonth: 28500000,
  expenseMonth: 12750000,
  trendDirection: 'up',
  recentTransactions: mockKasTransactions,
  trendData: [
    42000000, 42500000, 43000000, 42800000, 43200000, 43500000, 44000000,
    43800000, 44200000, 44500000, 44800000, 45000000, 45250000,
  ],
};

export const mockAnnouncements: string[] = [
  'Mohon menonaktifkan atau membisukan ponsel sebelum salat dimulai.',
  "Kajian rutin setiap Ahad ba'da Maghrib bersama Ustadz Ahmad.",
  'Infaq pembangunan gedung baru telah mencapai 75% dari target.',
  'Pendaftaran TPA dibuka untuk tahun ajaran baru, hubungi panitia di ruang sekretariat.',
];
