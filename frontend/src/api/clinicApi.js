// HTTP client. Every route goes through /api/clinica (ClinicController -> ClinicFacade).
const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');
const BASE_URL = `${API_URL}/api/clinica`;

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(BASE_URL + path, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    throw new Error('No hay conexión con el servidor. Verifica que el backend esté en línea.');
  }

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!response.ok) {
    throw new Error(data?.message || `El servidor respondió con error ${response.status}.`);
  }
  return data;
}

const post = (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) });

export const clinicApi = {
  registerPatient: (data) => post('/paciente', data),
  login: (documentNumber, password) => post('/login', { documentNumber, password }),
  getSpecialties: () => request('/especialidades'),
  getDoctors: (specialty, date) => {
    const params = new URLSearchParams();
    if (specialty) params.set('especialidad', specialty);
    if (date) params.set('date', date);
    return request(`/medicos?${params.toString()}`);
  },
  scheduleAppointment: (data) => post('/cita', data),
  cancelAppointment: (appointmentId, patientId) =>
    request(`/cita/${appointmentId}?patientId=${patientId}`, { method: 'DELETE' }),
  getMedicalRecord: (patientId) => request(`/historia/${patientId}`),
  generatePrescription: (data) => post('/prescripcion', data),
  requestLabTests: (data) => post('/laboratorio', data),
};
