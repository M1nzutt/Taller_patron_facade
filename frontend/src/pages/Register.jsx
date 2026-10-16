import { useState } from 'react';
import { clinicApi } from '../api/clinicApi.js';
import Alert from '../components/Alert.jsx';
import { todayISO } from '../utils/format.js';

const EMPTY_FORM = {
  documentNumber: '', firstName: '', lastName: '', birthDate: '',
  email: '', phone: '', password: '', allergies: '',
};

export default function Register({ onRegistered, goToLogin }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const patient = await clinicApi.registerPatient({
        ...form,
        birthDate: form.birthDate || null,
        allergies: form.allergies.split(',').map((a) => a.trim()).filter(Boolean),
      });
      onRegistered(patient);
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
        <h1>Crea tu cuenta</h1>
        <p>Con tu documento de identidad podrás ingresar al portal. Registra tus alergias: las usamos para validar cada prescripción.</p>
      </section>

      <form className="auth-form wide" onSubmit={handleSubmit}>
        <h2>Registro de paciente</h2>
        <Alert>{error}</Alert>
        <div className="grid-2">
          <label>Número de documento<input inputMode="numeric" value={form.documentNumber} onChange={update('documentNumber')} required /></label>
          <label>Fecha de nacimiento<input type="date" max={todayISO()} value={form.birthDate} onChange={update('birthDate')} /></label>
          <label>Nombres<input value={form.firstName} onChange={update('firstName')} required /></label>
          <label>Apellidos<input value={form.lastName} onChange={update('lastName')} required /></label>
          <label>Correo electrónico<input type="email" value={form.email} onChange={update('email')} /></label>
          <label>Teléfono<input inputMode="tel" value={form.phone} onChange={update('phone')} /></label>
        </div>
        <label>
          Alergias <small>(separadas por coma; déjalo vacío si no tienes)</small>
          <input placeholder="Penicilina, Ibuprofeno" value={form.allergies} onChange={update('allergies')} />
        </label>
        <label>Contraseña <small>(mínimo 4 caracteres)</small>
          <input type="password" minLength={4} value={form.password} onChange={update('password')} required />
        </label>
        <button className="button" type="submit" disabled={loading}>
          {loading ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
        <p className="switch">
          ¿Ya tienes cuenta? <button type="button" className="link-button" onClick={goToLogin}>Iniciar sesión</button>
        </p>
      </form>
    </div>
  );
}
