import { useState } from 'react';
import { clinicApi } from '../api/clinicApi.js';
import Alert from '../components/Alert.jsx';

const EMPTY_ROW = { name: '', dose: '', duration: '' };

export default function NewPrescription({ patient, onNavigate }) {
  const [medications, setMedications] = useState([{ ...EMPTY_ROW }]);
  const [notes, setNotes] = useState('');
  const [error, setError] = useState('');
  const [created, setCreated] = useState(null);
  const [loading, setLoading] = useState(false);

  const update = (index, field, value) =>
    setMedications((list) => list.map((m, i) => (i === index ? { ...m, [field]: value } : m)));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      setCreated(await clinicApi.generatePrescription({ patientId: patient.id, medications, notes }));
      setMedications([{ ...EMPTY_ROW }]);
      setNotes('');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <header className="page-header">
        <h1>Nueva prescripción</h1>
        <p>
          Simula la fórmula que emite el médico. Antes de guardarla, el sistema la compara con las alergias del paciente
          {patient.allergies?.length ? ` (${patient.allergies.join(', ')})` : ''}.
        </p>
      </header>

      <Alert onClose={() => setError('')}>{error}</Alert>
      {created && (
        <Alert type="success" onClose={() => setCreated(null)}>
          Prescripción #{created.id} generada con {created.medications.length} medicamento(s).{' '}
          <button type="button" className="link-button" onClick={() => onNavigate('record')}>Ver en la historia clínica</button>
        </Alert>
      )}

      <form onSubmit={handleSubmit} className="panel-box">
        {medications.map((m, i) => (
          <fieldset key={i} className="medication-row">
            <legend>Medicamento {i + 1}</legend>
            <label>Nombre<input value={m.name} onChange={(e) => update(i, 'name', e.target.value)} placeholder="Acetaminofén" required /></label>
            <label>Dosis<input value={m.dose} onChange={(e) => update(i, 'dose', e.target.value)} placeholder="500 mg cada 8 horas" required /></label>
            <label>Duración<input value={m.duration} onChange={(e) => update(i, 'duration', e.target.value)} placeholder="5 días" required /></label>
            {medications.length > 1 && (
              <button
                type="button"
                className="link-button remove"
                onClick={() => setMedications((list) => list.filter((_, idx) => idx !== i))}
              >
                Quitar
              </button>
            )}
          </fieldset>
        ))}
        <button type="button" className="button-secondary" onClick={() => setMedications((list) => [...list, { ...EMPTY_ROW }])}>
          Añadir medicamento
        </button>
        <label>
          Observaciones
          <textarea rows={3} value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Tomar después de las comidas" />
        </label>
        <button className="button" type="submit" disabled={loading}>
          {loading ? 'Validando…' : 'Generar prescripción'}
        </button>
      </form>
    </div>
  );
}
