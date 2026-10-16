import RangeBar from './RangeBar.jsx';
import { formatDateTime, formatNumber, formatRange, getValueStatus } from '../utils/format.js';

const STATUS_LABEL = { NORMAL: 'Normal', HIGH: 'Alto', LOW: 'Bajo' };

export default function LabTestCard({ test }) {
  const outOfRange = test.parameters.filter((p) => !p.withinRange).length;

  return (
    <article className="lab-card">
      <header className="lab-card-header">
        <div>
          <h3>{test.type}</h3>
          <small>{formatDateTime(test.date)}</small>
        </div>
        <span className={`range-summary ${outOfRange ? 'has-alerts' : ''}`}>
          {outOfRange ? `${outOfRange} fuera de rango` : 'Todo en rango normal'}
        </span>
      </header>

      <div className="table-scroll">
        <table className="results-table">
          <thead>
            <tr>
              <th scope="col">Parámetro</th>
              <th scope="col">Resultado</th>
              <th scope="col">Referencia</th>
              <th scope="col" className="bar-column">Posición</th>
              <th scope="col">Estado</th>
            </tr>
          </thead>
          <tbody>
            {test.parameters.map((p) => {
              const status = getValueStatus(p);
              return (
                <tr key={p.name}>
                  <td>{p.name}</td>
                  <td className="value">
                    {formatNumber(p.value)} <small>{p.unit}</small>
                  </td>
                  <td className="reference">{formatRange(p.min, p.max)}</td>
                  <td className="bar-column">
                    <RangeBar value={p.value} min={p.min} max={p.max} status={status} />
                  </td>
                  <td>
                    <span className={`status status-${status.toLowerCase()}`}>{STATUS_LABEL[status]}</span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </article>
  );
}
