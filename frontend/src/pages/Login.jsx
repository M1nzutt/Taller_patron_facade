import { useState } from 'react';
import { clinicApi } from '../api/clinicApi.js';
import Alert from '../components/Alert.jsx';

export default function Login({ onLogin, goToRegister }) {
  const [documentNumber, setDocumentNumber] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      onLogin(await clinicApi.login(documentNumber.trim(), password));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth">
      <section className="auth-intro">
        <span className="brand-mark large" aria-hidden="true">+</span>
        <h1>Clínica San Rafael</h1>
        <p>Agenda tus citas, consulta tu historia clínica y revisa tus resultados de laboratorio desde un solo lugar.</p>
      </section>

      <form className="auth-form" onSubmit={handleSubmit}>
        <h2>Iniciar sesión</h2>
        <Alert>{error}</Alert>
        <label>
          Número de documento
          <input inputMode="numeric" value={documentNumber} onChange={(e) => setDocumentNumber(e.target.value)} required autoFocus />
        </label>
        <label>
          Contraseña
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        <button className="button" type="submit" disabled={loading}>
          {loading ? 'Ingresando…' : 'Ingresar'}
        </button>
        <p className="hint">
          Paciente de prueba: documento <b>1085000000</b>, contraseña <b>1234</b>.
        </p>
        <p className="switch">
          ¿Primera vez aquí? <button type="button" className="link-button" onClick={goToRegister}>Crear una cuenta</button>
        </p>
      </form>
    </div>
  );
}
