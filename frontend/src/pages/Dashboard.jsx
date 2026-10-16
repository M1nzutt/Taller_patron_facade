import { useState } from 'react';
import { clinicApi } from '../api/clinicApi.js';
import Alert from '../components/Alert.jsx';
import LabTestCard from '../components/LabTestCard.jsx';
import useMedicalRecord from '../hooks/useMedicalRecord.js';
import { formatDateTime } from '../utils/format.js';

export default function Dashboard({ patient, onNavigate }) {
  const { record, error, loading, reload } = useMedicalRecord(patient.id);
  const [notice, setNotice] = useState('');
  const [actionError, setActionError] = useState('');

  const cancel = async (appointment) => {
    if (!window.confirm(`¿Cancelar la cita de ${appointment.specialty} con ${appointment.doctorName}?`)) return;
    setActionError('');
    try {
      const response = await clinicApi.cancelAppointment(appointment.id, patient.id);
      setNotice(response.reminder);
      reload();
    } catch (err) {
      setActionError(err.message);
    }
  };

  if (loading && !record) return <p className="loading">Cargando tu información…</p>;
  if (error) return <Alert>{error}</Alert>;
  if (!record) return null;

  const { summary, upcomingAppointments, labTests } = record;
  const { allergies } = record.patient;

  return (
    <div className="page">
      <header className="page-header">
        <h1>Hola, {patient.firstName}</h1>
        <p>
          {summary.upcomingAppointments === 0
            ? 'No tienes citas programadas.'
            : `Tienes ${summary.upcomingAppointments} ${summary.upcomingAppointments === 1 ? 'cita programada' : 'citas programadas'}.`}{' '}
          {summary.outOfRangeResults > 0 &&
            `${summary.outOfRangeResults} ${summary.outOfRangeResults === 1 ? 'resultado está' : 'resultados están'} fuera del rango normal.`}
        </p>
      </header>

      <Alert type="success" onClose={() => setNotice('')}>{notice}</Alert>
      <Alert onClose={() => setActionError('')}>{actionError}</Alert>

      <div className="allergies">
        <span>Alergias registradas:</span>
        {allergies.length ? allergies.map((a) => <span key={a} className="allergy-chip">{a}</span>) : <span>ninguna</span>}
      </div>

      <section className="panel-box">
        <div className="panel-box-header">
          <h2>Próximas citas</h2>
          <button type="button" className="button" onClick={() => onNavigate('schedule')}>Agendar cita</button>
        </div>
        {upcomingAppointments.length === 0 ? (
          <p className="empty">Aún no tienes citas. Agenda una con cualquiera de nuestras especialidades.</p>
        ) : (
          <ul className="appointment-list">
            {upcomingAppointments.map((a) => (
              <li key={a.id} className="appointment">
                <div className="appointment-date">{formatDateTime(a.dateTime)}</div>
                <div className="appointment-detail">
                  <strong>{a.specialty}</strong>
                  <span>{a.doctorName}</span>
                </div>
                <button type="button" className="button-secondary" onClick={() => cancel(a)}>Cancelar</button>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="panel-box">
        <div className="panel-box-header">
          <h2>Últimos resultados</h2>
          <button type="button" className="button-secondary" onClick={() => onNavigate('labs')}>Solicitar exámenes</button>
        </div>
        {labTests.length === 0 ? (
          <p className="empty">No tienes resultados de laboratorio todavía.</p>
        ) : (
          labTests.slice(0, 2).map((t) => <LabTestCard key={t.id} test={t} />)
        )}
      </section>
    </div>
  );
}
