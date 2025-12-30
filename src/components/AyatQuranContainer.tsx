import { useEffect, useState } from 'react';
import { supabase } from '../lib/supabase';
import { QuranVerseCard } from './QuranVerseCard';

export default function AyatQuranContainer() {
  const [ayat, setAyat] = useState<any>(null);

  const fetchAyat = async () => {
    const { data, error } = await supabase
      .from('ayat_quran')
      .select('*')
      .eq('aktif', true)
      .order('updated_at', { ascending: false })
      .limit(1)
      .maybeSingle();

    if (error || !data) return;

    setAyat(data);
  };

  useEffect(() => {
    fetchAyat();
  }, []);

  if (!ayat) return null;

  return <QuranVerseCard ayat={ayat} />;
}
