import { useState } from 'react';
import Sidebar from './components/Sidebar.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import ScheduleAppointment from './pages/ScheduleAppointment.jsx';
import MedicalRecord from './pages/MedicalRecord.jsx';
import LabTests from './pages/LabTests.jsx';
import NewPrescription from './pages/NewPrescription.jsx';

const SESSION_KEY = 'clinic.patient';

function readSession() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY));
  } catch {
    return null;
  }
}

export default function App() {
  const [patient, setPatient] = useState(readSession);
  const [view, setView] = useState('dashboard');
  const [authScreen, setAuthScreen] = useState('login');

  const startSession = (data) => {
    setPatient(data);
    setView('dashboard');
    try {
      localStorage.setItem(SESSION_KEY, JSON.stringify(data));
    } catch {
      /* no storage available: the session lasts while the tab is open */
    }
  };

  const endSession = () => {
    setPatient(null);
    setAuthScreen('login');
    try {
      localStorage.removeItem(SESSION_KEY);
    } catch {
      /* ignore */
    }
  };

  if (!patient) {
    return authScreen === 'login'
      ? <Login onLogin={startSession} goToRegister={() => setAuthScreen('register')} />
      : <Register onRegistered={startSession} goToLogin={() => setAuthScreen('login')} />;
  }

  const pageProps = { patient, onNavigate: setView };

  return (
    <div className="app">
      <Sidebar patient={patient} view={view} onNavigate={setView} onLogout={endSession} />
      <main className="content">
        {view === 'dashboard' && <Dashboard {...pageProps} />}
        {view === 'schedule' && <ScheduleAppointment {...pageProps} />}
        {view === 'record' && <MedicalRecord {...pageProps} />}
        {view === 'labs' && <LabTests {...pageProps} />}
        {view === 'prescription' && <NewPrescription {...pageProps} />}
      </main>
    </div>
  );
}
