import { format } from 'date-fns';
import { id } from 'date-fns/locale';

/**
 * Format Gregorian date for display
 */
export const formatGregorianDate = (date: Date): string => {
  return format(date, 'EEEE, d MMMM yyyy', { locale: id });
};

/**
 * Format time with seconds
 */
export const formatTimeWithSeconds = (date: Date): string => {
  return format(date, 'HH:mm:ss');
};
