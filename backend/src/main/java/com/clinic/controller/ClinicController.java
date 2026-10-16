package com.clinic.controller;

import com.clinic.dto.AppointmentRequest;
import com.clinic.dto.AppointmentResponse;
import com.clinic.dto.DoctorAvailability;
import com.clinic.dto.FullMedicalRecordResponse;
import com.clinic.dto.LabTestRequest;
import com.clinic.dto.LoginRequest;
import com.clinic.dto.PatientRegistrationRequest;
import com.clinic.dto.PatientResponse;
import com.clinic.dto.PrescriptionRequest;
import com.clinic.facade.ClinicFacade;
import com.clinic.model.LabTest;
import com.clinic.model.Prescription;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller. It only depends on ClinicFacade and knows none of the subsystems.
 * The endpoint paths are the ones defined in the assignment.
 */
@RestController
@RequestMapping("/api/clinica")
public class ClinicController {

    private final ClinicFacade clinicFacade;

    public ClinicController(ClinicFacade clinicFacade) {
        this.clinicFacade = clinicFacade;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "Clinic API - Facade Pattern");
    }

    @PostMapping("/paciente")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse registerPatient(@RequestBody PatientRegistrationRequest request) {
        return clinicFacade.registerPatient(request);
    }

    @PostMapping("/login")
    public PatientResponse login(@RequestBody LoginRequest request) {
        return clinicFacade.login(request);
    }

    @PostMapping("/cita")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse scheduleAppointment(@RequestBody AppointmentRequest request) {
        return clinicFacade.scheduleAppointment(request.patientId(), request.specialty(),
                request.date(), request.doctorId());
    }

    @DeleteMapping("/cita/{appointmentId}")
    public AppointmentResponse cancelAppointment(@PathVariable("appointmentId") Long appointmentId,
                                                 @RequestParam("patientId") Long patientId) {
        return clinicFacade.cancelAppointment(appointmentId, patientId);
    }

    @GetMapping("/historia/{patientId}")
    public FullMedicalRecordResponse getFullMedicalRecord(@PathVariable("patientId") Long patientId) {
        return clinicFacade.getFullMedicalRecord(patientId);
    }

    @PostMapping("/prescripcion")
    @ResponseStatus(HttpStatus.CREATED)
    public Prescription generatePrescription(@RequestBody PrescriptionRequest request) {
        return clinicFacade.generatePrescription(request.patientId(), request.medications(), request.notes());
    }

    @PostMapping("/laboratorio")
    @ResponseStatus(HttpStatus.CREATED)
    public List<LabTest> requestLabTests(@RequestBody LabTestRequest request) {
        return clinicFacade.requestLabTests(request.patientId(), request.tests());
    }

    @GetMapping("/medicos")
    public List<DoctorAvailability> getDoctors(
            @RequestParam(value = "especialidad", required = false) String specialty,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return clinicFacade.getDoctors(specialty, date);
    }

    @GetMapping("/especialidades")
    public List<String> getSpecialties() {
        return clinicFacade.getSpecialties();
    }

    @GetMapping("/examenes")
    public List<String> getLabTestTypes() {
        return clinicFacade.getLabTestTypes();
    }
}
