# Sistema de Gestión Médica · Patrón Facade

Taller de Patrones de Software (patrón estructural **Facade**). Portal para pacientes de una clínica donde se pueden agendar citas, consultar la historia clínica, recibir prescripciones y solicitar exámenes de laboratorio. El código está escrito en inglés; los textos que ve el paciente están en español.

## Integrantes

| Integrante | Responsabilidad |
|---|---|
| Arley Riascos | Capa de datos en memoria: modelos, repositorios y datos iniciales |
| Armando Hernández | Backend Spring Boot: subsistemas, `ClinicFacade`, API REST |
| Sara Delgado | Frontend React + Vite |

## Cómo se aplica el patrón Facade

```
Frontend (React) ──HTTP──▶ ClinicController ──▶ ClinicFacade ──┬──▶ PatientService
                                                               ├──▶ ScheduleService
                                                               ├──▶ MedicalRecordService
                                                               ├──▶ PrescriptionService
                                                               └──▶ LaboratoryService
                                                                        │
                                                        Repositorios en memoria (datos)
```

El controlador **solo conoce a `ClinicFacade`**. Los cinco subsistemas no se conocen entre sí; la fachada los coordina y cada operación invoca como mínimo dos de ellos.

| Enunciado del taller | Clase / método en el código | Subsistemas que invoca |
|---|---|---|
| PacienteService | `PatientService` | — |
| AgendaService | `ScheduleService` | — |
| HistoriaClinicaService | `MedicalRecordService` | — |
| PrescripcionService | `PrescriptionService` | — |
| LaboratorioService | `LaboratoryService` | — |
| ClinicaFacade | `ClinicFacade` | — |
| agendarCita(pacienteId, especialidad, fecha) | `scheduleAppointment` | Patient + Schedule + MedicalRecord |
| verHistoriaCompleta(pacienteId) | `getFullMedicalRecord` | Los cinco |
| generarPrescripcion(pacienteId, medicamentos[]) | `generatePrescription` | Patient + MedicalRecord (alergias) + Prescription |
| solicitarExamenes(pacienteId, examenes[]) | `requestLabTests` | Patient + Laboratory |
| Registro de paciente | `registerPatient` | Patient + MedicalRecord |

## Endpoints

Las rutas son las definidas en el enunciado.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/clinica/paciente` | Registrar paciente |
| POST | `/api/clinica/login` | Iniciar sesión |
| POST | `/api/clinica/cita` | Agendar cita |
| DELETE | `/api/clinica/cita/{appointmentId}?patientId=` | Cancelar cita |
| GET | `/api/clinica/historia/{patientId}` | Historia clínica completa |
| POST | `/api/clinica/prescripcion` | Generar prescripción (valida alergias) |
| POST | `/api/clinica/laboratorio` | Solicitar exámenes (resultados simulados) |
| GET | `/api/clinica/medicos?especialidad=&date=` | Médicos y horarios disponibles |
| GET | `/api/clinica/especialidades` | Especialidades |
| GET | `/api/clinica/health` | Estado del servicio |

```json
POST /api/clinica/cita
{ "patientId": 1, "specialty": "Cardiología", "date": "2026-10-20T09:00", "doctorId": 3 }

POST /api/clinica/prescripcion
{ "patientId": 1, "medications": [{ "name": "Acetaminofén", "dose": "500 mg cada 8 horas", "duration": "5 días" }], "notes": "" }

POST /api/clinica/laboratorio
{ "patientId": 1, "tests": ["COMPLETE_BLOOD_COUNT", "BLOOD_GLUCOSE", "LIPID_PROFILE"] }
```

## Datos

Todos los datos viven en memoria (`ConcurrentHashMap`), como exige el enunciado. Al iniciar se cargan 3 especialidades (Medicina General, Cardiología, Pediatría), 5 médicos y un paciente de prueba: documento `1085000000`, contraseña `1234`, alergia a Penicilina (prescribir "Amoxicilina" es rechazado). Los datos se reinician cada vez que el servidor se reinicia.

## Ejecutar en local

```bash
cd backend
mvn spring-boot:run        # http://localhost:8080/api/clinica/health
mvn test

cd frontend
npm install
cp .env.example .env       # VITE_API_URL=http://localhost:8080
npm run dev                # http://localhost:5173
```
