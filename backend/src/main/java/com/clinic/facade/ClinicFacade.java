package com.clinic.facade;

import com.clinic.dto.AppointmentResponse;
import com.clinic.dto.DoctorAvailability;
import com.clinic.dto.FullMedicalRecordResponse;
import com.clinic.dto.LoginRequest;
import com.clinic.dto.MedicalRecordSummary;
import com.clinic.dto.PatientRegistrationRequest;
import com.clinic.dto.PatientResponse;
import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.Consultation;
import com.clinic.model.LabTest;
import com.clinic.model.Medication;
import com.clinic.model.Patient;
import com.clinic.model.Prescription;
import com.clinic.service.LaboratoryService;
import com.clinic.service.MedicalRecordService;
import com.clinic.service.PatientService;
import com.clinic.service.PrescriptionService;
import com.clinic.service.ScheduleService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * FACADE PATTERN.
 *
 * Single entry point to the clinic's business logic. The REST controller only
 * knows this class; here the five subsystems (which do not know each other)
 * are coordinated, and the patient never sees the internal validations.
 * Every operation calls at least two subsystems.
 */
@Service
public class ClinicFacade {

    private final PatientService patientService;
    private final ScheduleService scheduleService;
    private final MedicalRecordService medicalRecordService;
    private final PrescriptionService prescriptionService;
    private final LaboratoryService laboratoryService;

    public ClinicFacade(PatientService patientService,
                        ScheduleService scheduleService,
                        MedicalRecordService medicalRecordService,
                        PrescriptionService prescriptionService,
                        LaboratoryService laboratoryService) {
        this.patientService = patientService;
        this.scheduleService = scheduleService;
        this.medicalRecordService = medicalRecordService;
        this.prescriptionService = prescriptionService;
        this.laboratoryService = laboratoryService;
    }

    /** Subsystems: PatientService + MedicalRecordService. */
    public PatientResponse registerPatient(PatientRegistrationRequest request) {
        Patient patient = patientService.register(request);
        List<String> allergies = medicalRecordService.createRecord(patient.id(), request.allergies());
        return PatientResponse.from(patient, allergies);
    }

    /** Subsystems: PatientService + MedicalRecordService. */
    public PatientResponse login(LoginRequest credentials) {
        if (credentials == null) {
            throw new BadRequestException("Ingresa tu documento y tu contraseña.");
        }
        Patient patient = patientService.authenticate(credentials.documentNumber(), credentials.password());
        return PatientResponse.from(patient, medicalRecordService.getAllergies(patient.id()));
    }

    /** Subsystems: PatientService + ScheduleService + MedicalRecordService. */
    public AppointmentResponse scheduleAppointment(Long patientId, String specialty,
                                                   LocalDateTime date, Long doctorId) {
        Patient patient = patientService.getById(patientId);
        Appointment appointment = scheduleService.book(patient.id(), specialty, date, doctorId);

        String reminder = scheduleService.buildReminder(appointment);
        List<String> allergies = medicalRecordService.getAllergies(patient.id());
        if (!allergies.isEmpty()) {
            reminder += " Recuerda informar al médico tus alergias: " + String.join(", ", allergies) + ".";
        }
        return new AppointmentResponse(appointment, reminder);
    }

    /** Subsystems: PatientService + ScheduleService. */
    public AppointmentResponse cancelAppointment(Long appointmentId, Long patientId) {
        Patient patient = patientService.getById(patientId);
        Appointment appointment = scheduleService.cancel(appointmentId, patient.id());
        return new AppointmentResponse(appointment, "Tu cita fue cancelada y el horario quedó libre.");
    }

    /** Subsystems: all five. Consolidates everything in a single response object. */
    public FullMedicalRecordResponse getFullMedicalRecord(Long patientId) {
        Patient patient = patientService.getById(patientId);
        List<String> allergies = medicalRecordService.getAllergies(patientId);
        List<Appointment> appointments = scheduleService.getPatientAppointments(patientId);
        List<Consultation> consultations = medicalRecordService.getConsultations(patientId);
        List<Prescription> prescriptions = prescriptionService.getPrescriptions(patientId);
        List<LabTest> labTests = laboratoryService.getResults(patientId);

        LocalDateTime now = LocalDateTime.now();
        List<Appointment> upcoming = appointments.stream()
                .filter(a -> a.status() == AppointmentStatus.SCHEDULED && a.dateTime().isAfter(now))
                .sorted(Comparator.comparing(Appointment::dateTime))
                .toList();
        List<Appointment> past = appointments.stream()
                .filter(a -> !upcoming.contains(a))
                .sorted(Comparator.comparing(Appointment::dateTime).reversed())
                .toList();
        int outOfRange = (int) labTests.stream()
                .flatMap(t -> t.parameters().stream())
                .filter(p -> !p.withinRange())
                .count();

        MedicalRecordSummary summary = new MedicalRecordSummary(upcoming.size(), consultations.size(),
                prescriptions.size(), labTests.size(), outOfRange);

        return new FullMedicalRecordResponse(PatientResponse.from(patient, allergies), upcoming, past,
                consultations, prescriptions, labTests, summary);
    }

    /** Subsystems: PatientService + MedicalRecordService (allergies) + PrescriptionService. */
    public Prescription generatePrescription(Long patientId, List<Medication> medications, String notes) {
        Patient patient = patientService.getById(patientId);
        prescriptionService.validateMedications(medications);

        List<String> conflicts = medicalRecordService.findAllergyConflicts(patient.id(),
                medications.stream().map(Medication::name).toList());
        if (!conflicts.isEmpty()) {
            throw new ConflictException("No se puede generar la prescripción. Riesgo de reacción alérgica: "
                    + String.join("; ", conflicts) + ".");
        }
        return prescriptionService.create(patient.id(), medications, notes);
    }

    /** Subsystems: PatientService + LaboratoryService. */
    public List<LabTest> requestLabTests(Long patientId, List<String> tests) {
        Patient patient = patientService.getById(patientId);
        return laboratoryService.requestTests(patient.id(), tests);
    }

    public List<DoctorAvailability> getDoctors(String specialty, LocalDate date) {
        return scheduleService.getAvailability(specialty, date);
    }

    public List<String> getSpecialties() {
        return scheduleService.getSpecialties();
    }

    public List<String> getLabTestTypes() {
        return laboratoryService.getAvailableTests();
    }
}
