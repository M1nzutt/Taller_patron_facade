// Visual ruler: green band = normal range, dot = the patient's value.
export default function RangeBar({ value, min, max, status }) {
  const low = min ?? (max != null ? max * 0.5 : 0);
  const high = max ?? (min != null ? min * 2 : 1);
  const margin = (high - low) * 0.4;
  const start = Math.max(0, low - margin);
  const end = high + margin;
  const percent = (v) => Math.min(100, Math.max(0, ((v - start) / (end - start)) * 100));

  const bandStart = min != null ? percent(min) : 0;
  const bandEnd = max != null ? percent(max) : 100;

  return (
    <div className="range" aria-hidden="true">
      <div className="range-band" style={{ left: `${bandStart}%`, width: `${bandEnd - bandStart}%` }} />
      <div className={`range-dot dot-${status.toLowerCase()}`} style={{ left: `${percent(value)}%` }} />
    </div>
  );
}
