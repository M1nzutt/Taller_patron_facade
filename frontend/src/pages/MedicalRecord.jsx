import { useState } from 'react';
import Alert from '../components/Alert.jsx';
import LabTestCard from '../components/LabTestCard.jsx';
import useMedicalRecord from '../hooks/useMedicalRecord.js';
import { formatDate, formatDateTime } from '../utils/format.js';

const TABS = [
  { id: 'consultations', label: 'Consultas' },
  { id: 'prescriptions', label: 'Prescripciones' },
  { id: 'labTests', label: 'Laboratorios' },
  { id: 'pastAppointments', label: 'Citas anteriores' },
];

const APPOINTMENT_STATUS = { SCHEDULED: 'Programada', CANCELLED: 'Cancelada', ATTENDED: 'Atendida' };

export default function MedicalRecord({ patient }) {
  const { record, error, loading } = useMedicalRecord(patient.id);
  const [tab, setTab] = useState('consultations');

  if (loading && !record) return <p className="loading">Cargando historia clínica…</p>;
  if (error) return <Alert>{error}</Alert>;
  if (!record) return null;

  const counts = Object.fromEntries(TABS.map((t) => [t.id, record[t.id].length]));

  return (
    <div className="page">
      <header className="page-header">
        <h1>Historia clínica</h1>
        <p>
          {record.patient.firstName} {record.patient.lastName} · Documento {record.patient.documentNumber}
          {record.patient.allergies.length > 0 && ` · Alergias: ${record.patient.allergies.join(', ')}`}
        </p>
      </header>

      <div className="tabs" role="tablist">
        {TABS.map((t) => (
          <button
            key={t.id}
            type="button"
            role="tab"
            aria-selected={tab === t.id}
            className={`tab ${tab === t.id ? 'active' : ''}`}
            onClick={() => setTab(t.id)}
          >
            {t.label} <span className="count">{counts[t.id]}</span>
          </button>
        ))}
      </div>

      <div role="tabpanel" className="tab-panel">
        {tab === 'consultations' && (
          counts.consultations === 0 ? <p className="empty">Sin consultas registradas.</p> : (
            <ol className="timeline">
              {record.consultations.map((c) => (
                <li key={c.id}>
                  <time>{formatDate(c.date)}</time>
                  <div>
                    <strong>{c.diagnosis}</strong>
                    <span>{c.specialty} · {c.doctor}</span>
                    {c.notes && <p>{c.notes}</p>}
                  </div>
                </li>
              ))}
            </ol>
          )
        )}

        {tab === 'prescriptions' && (
          counts.prescriptions === 0 ? <p className="empty">Sin prescripciones.</p> : (
            record.prescriptions.map((p) => (
              <article key={p.id} className="prescription">
                <header>
                  <h3>Prescripción #{p.id}</h3>
                  <time>{formatDate(p.date)}</time>
                </header>
                <div className="table-scroll">
                  <table className="simple-table">
                    <thead><tr><th scope="col">Medicamento</th><th scope="col">Dosis</th><th scope="col">Duración</th></tr></thead>
                    <tbody>
                      {p.medications.map((m, i) => (
                        <tr key={i}><td>{m.name}</td><td>{m.dose}</td><td>{m.duration}</td></tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                {p.notes && <p className="hint">{p.notes}</p>}
              </article>
            ))
          )
        )}

        {tab === 'labTests' && (
          counts.labTests === 0 ? <p className="empty">Sin exámenes de laboratorio.</p> :
            record.labTests.map((t) => <LabTestCard key={t.id} test={t} />)
        )}

        {tab === 'pastAppointments' && (
          counts.pastAppointments === 0 ? <p className="empty">Sin citas anteriores.</p> : (
            <ul className="appointment-list">
              {record.pastAppointments.map((a) => (
                <li key={a.id} className="appointment">
                  <div className="appointment-date">{formatDateTime(a.dateTime)}</div>
                  <div className="appointment-detail">
                    <strong>{a.specialty}</strong>
                    <span>{a.doctorName}</span>
                  </div>
                  <span className={`appointment-status status-${a.status.toLowerCase()}`}>{APPOINTMENT_STATUS[a.status]}</span>
                </li>
              ))}
            </ul>
          )
        )}
      </div>
    </div>
  );
}
