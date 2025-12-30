import { useEffect, useState } from 'react';
import { KasSummary } from './KasSummary';
import { supabase } from '../lib/supabase';
import { KasData } from '../types';

export default function KasSummaryContainer() {
  const [kasData, setKasData] = useState<KasData>({
    balance: 0,
    incomeMonth: 0,
    expenseMonth: 0,
    trendDirection: 'flat',
    recentTransactions: [],
  trendData: [],
  });

  const fetchKas = async () => {
    const { data, error } = await supabase
      .from('kas_masjid')
      .select('total')
      .order('id', { ascending: false })
      .limit(1)
      .maybeSingle();

    if (error || !data) return;

    setKasData({
  balance: Number(data.total),
  incomeMonth: 0,
  expenseMonth: 0,
  trendDirection: 'flat',
  recentTransactions: [],
  trendData: [],
});

  };

  useEffect(() => {
    fetchKas();
  }, []);

  return <KasSummary kasData={kasData} />;
}
