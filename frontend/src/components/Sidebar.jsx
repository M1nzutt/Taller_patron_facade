const MENU = [
  { id: 'dashboard', label: 'Inicio' },
  { id: 'schedule', label: 'Agendar cita' },
  { id: 'record', label: 'Historia clínica' },
  { id: 'labs', label: 'Exámenes de laboratorio' },
  { id: 'prescription', label: 'Nueva prescripción' },
];

export default function Sidebar({ patient, view, onNavigate, onLogout }) {
  return (
    <aside className="sidebar">
      <div className="brand">
        <span className="brand-mark" aria-hidden="true">+</span>
        <div>
          <strong>Clínica San Rafael</strong>
          <small>Portal del paciente</small>
        </div>
      </div>

      <nav aria-label="Secciones">
        {MENU.map((item) => (
          <button
            key={item.id}
            type="button"
            className={`nav-item ${view === item.id ? 'active' : ''}`}
            aria-current={view === item.id ? 'page' : undefined}
            onClick={() => onNavigate(item.id)}
          >
            {item.label}
          </button>
        ))}
      </nav>

      <div className="sidebar-user">
        <span>{patient.firstName} {patient.lastName}</span>
        <small>Documento {patient.documentNumber}</small>
        <button type="button" className="link-button" onClick={onLogout}>Cerrar sesión</button>
      </div>
    </aside>
  );
}
