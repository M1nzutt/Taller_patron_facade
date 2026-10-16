const LOCALE = 'es-CO';

// "2026-10-16" is parsed as UTC and would show the previous day in Colombia; force local time.
function toDate(value) {
  if (!value) return null;
  return typeof value === 'string' && value.length === 10 ? new Date(`${value}T00:00:00`) : new Date(value);
}

export function formatDate(value) {
  const d = toDate(value);
  return d ? d.toLocaleDateString(LOCALE, { day: 'numeric', month: 'short', year: 'numeric' }) : '';
}

export function formatDateTime(value) {
  const d = toDate(value);
  if (!d) return '';
  const day = d.toLocaleDateString(LOCALE, { weekday: 'long', day: 'numeric', month: 'long' });
  const time = d.toLocaleTimeString(LOCALE, { hour: '2-digit', minute: '2-digit', hour12: true });
  return `${day.charAt(0).toUpperCase()}${day.slice(1)}, ${time}`;
}

export function formatNumber(value) {
  return Number(value).toLocaleString(LOCALE, { maximumFractionDigits: 1 });
}

export function formatRange(min, max) {
  if (min != null && max != null) return `${formatNumber(min)} – ${formatNumber(max)}`;
  if (max != null) return `menor a ${formatNumber(max)}`;
  if (min != null) return `mayor a ${formatNumber(min)}`;
  return '—';
}

/** Returns "NORMAL", "HIGH" or "LOW" according to the reference range. */
export function getValueStatus({ value, min, max }) {
  if (max != null && value > max) return 'HIGH';
  if (min != null && value < min) return 'LOW';
  return 'NORMAL';
}

export function todayISO(extraDays = 0) {
  const d = new Date();
  d.setDate(d.getDate() + extraDays);
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${month}-${day}`;
}
