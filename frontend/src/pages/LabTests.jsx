import { useState } from 'react';
import { clinicApi } from '../api/clinicApi.js';
import Alert from '../components/Alert.jsx';
import LabTestCard from '../components/LabTestCard.jsx';

const AVAILABLE_TESTS = [
  { id: 'COMPLETE_BLOOD_COUNT', name: 'Hemograma', detail: 'Hemoglobina, hematocrito, leucocitos y plaquetas' },
  { id: 'BLOOD_GLUCOSE', name: 'Glicemia', detail: 'Glucosa en ayunas' },
  { id: 'LIPID_PROFILE', name: 'Perfil lipídico', detail: 'Colesterol total, LDL, HDL y triglicéridos' },
];

export default function LabTests({ patient }) {
  const [selected, setSelected] = useState([]);
  const [results, setResults] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const toggle = (id) =>
    setSelected((current) => (current.includes(id) ? current.filter((x) => x !== id) : [...current, id]));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      setResults(await clinicApi.requestLabTests({ patientId: patient.id, tests: selected }));
      setSelected([]);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <header className="page-header">
        <h1>Exámenes de laboratorio</h1>
        <p>Selecciona los exámenes. Los resultados son simulados y se comparan con los valores de referencia.</p>
      </header>

      <Alert onClose={() => setError('')}>{error}</Alert>

      <form onSubmit={handleSubmit} className="panel-box">
        <fieldset className="checks">
          <legend>Exámenes disponibles</legend>
          {AVAILABLE_TESTS.map((t) => (
            <label key={t.id} className={`check ${selected.includes(t.id) ? 'selected' : ''}`}>
              <input type="checkbox" checked={selected.includes(t.id)} onChange={() => toggle(t.id)} />
              <span>
                <strong>{t.name}</strong>
                <small>{t.detail}</small>
              </span>
            </label>
          ))}
        </fieldset>
        <button className="button" type="submit" disabled={loading || selected.length === 0}>
          {loading ? 'Procesando…' : `Solicitar ${selected.length || ''} ${selected.length === 1 ? 'examen' : 'exámenes'}`}
        </button>
      </form>

      {results.length > 0 && (
        <section className="panel-box">
          <h2>Resultados</h2>
          {results.map((t) => <LabTestCard key={t.id} test={t} />)}
        </section>
      )}
    </div>
  );
}
