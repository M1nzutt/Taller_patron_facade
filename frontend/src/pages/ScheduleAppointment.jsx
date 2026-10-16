import { useEffect, useState } from 'react';
import { clinicApi } from '../api/clinicApi.js';
import Alert from '../components/Alert.jsx';
import { todayISO } from '../utils/format.js';

export default function ScheduleAppointment({ patient, onNavigate }) {
  const [specialties, setSpecialties] = useState([]);
  const [specialty, setSpecialty] = useState('');
  const [date, setDate] = useState(todayISO(1));
  const [doctors, setDoctors] = useState([]);
  const [selection, setSelection] = useState(null); // { doctor, slot }
  const [error, setError] = useState('');
  const [confirmation, setConfirmation] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    clinicApi.getSpecialties().then(setSpecialties).catch((e) => setError(e.message));
  }, []);

  useEffect(() => {
    if (!specialty || !date) {
      setDoctors([]);
      return;
    }
    setSelection(null);
    setError('');
    clinicApi.getDoctors(specialty, date).then(setDoctors).catch((e) => setError(e.message));
  }, [specialty, date]);

  const isSunday = date && new Date(`${date}T00:00:00`).getDay() === 0;

  const confirm = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await clinicApi.scheduleAppointment({
        patientId: patient.id,
        specialty,
        doctorId: selection.doctor.id,
        date: `${date}T${selection.slot}`,
      });
      setConfirmation(response);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (confirmation) {
    return (
      <div className="page">
        <header className="page-header">
          <h1>Cita agendada</h1>
        </header>
        <div className="confirmation">
          <p>{confirmation.reminder}</p>
          <div className="actions">
            <button type="button" className="button" onClick={() => onNavigate('dashboard')}>Ver mis citas</button>
            <button
              type="button"
              className="button-secondary"
              onClick={() => { setConfirmation(null); setSelection(null); setSpecialty(''); }}
            >
              Agendar otra cita
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>Agendar cita</h1>
        <p>Elige la especialidad y el día; luego toca un horario libre del médico que prefieras.</p>
      </header>

      <Alert onClose={() => setError('')}>{error}</Alert>

      <div className="step">
        <h2><span className="step-number">1</span> Especialidad</h2>
        <div className="options">
          {specialties.map((s) => (
            <button
              key={s}
              type="button"
              className={`option ${specialty === s ? 'selected' : ''}`}
              aria-pressed={specialty === s}
              onClick={() => setSpecialty(s)}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      <div className="step">
        <h2><span className="step-number">2</span> Día</h2>
        <input type="date" className="date-input" min={todayISO()} value={date} onChange={(e) => setDate(e.target.value)} />
        {isSunday && <p className="hint">La clínica no atiende los domingos. Elige otro día.</p>}
      </div>

      {specialty && !isSunday && (
        <div className="step">
          <h2><span className="step-number">3</span> Médico y horario</h2>
          <div className="doctors">
            {doctors.map((d) => (
              <div key={d.id} className="doctor">
                <strong>{d.name}</strong>
                {d.availableSlots.length === 0 ? (
                  <p className="hint">Sin horarios libres este día.</p>
                ) : (
                  <div className="slots">
                    {d.availableSlots.map((slot) => {
                      const active = selection?.doctor.id === d.id && selection?.slot === slot;
                      return (
                        <button
                          key={slot}
                          type="button"
                          className={`slot ${active ? 'selected' : ''}`}
                          aria-pressed={active}
                          onClick={() => setSelection({ doctor: d, slot })}
                        >
                          {slot}
                        </button>
                      );
                    })}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {selection && (
        <div className="confirm-bar">
          <span>
            {specialty} con <b>{selection.doctor.name}</b>, {date} a las {selection.slot}
          </span>
          <button type="button" className="button" onClick={confirm} disabled={loading}>
            {loading ? 'Agendando…' : 'Confirmar cita'}
          </button>
        </div>
      )}
    </div>
  );
}
