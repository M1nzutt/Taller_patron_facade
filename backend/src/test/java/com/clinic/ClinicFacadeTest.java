package com.clinic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinic.data.DataSeeder;
import com.clinic.dto.AppointmentResponse;
import com.clinic.dto.FullMedicalRecordResponse;
import com.clinic.dto.LoginRequest;
import com.clinic.exception.ConflictException;
import com.clinic.facade.ClinicFacade;
import com.clinic.model.LabTest;
import com.clinic.model.Medication;
import com.clinic.model.Prescription;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClinicFacadeTest {

    @Autowired
    private ClinicFacade facade;

    private Long demoPatientId() {
        return facade.login(new LoginRequest(DataSeeder.DEMO_DOCUMENT, DataSeeder.DEMO_PASSWORD)).id();
    }

    @Test
    void loadsThreeSpecialtiesAndFiveDoctors() {
        assertEquals(3, facade.getSpecialties().size());
        assertEquals(5, facade.getDoctors(null, null).size());
    }

    @Test
    void rejectsPrescriptionWithAllergenicMedication() {
        Long id = demoPatientId();
        List<Medication> medications = List.of(new Medication("Amoxicilina", "500 mg cada 8 horas", "7 días"));
        assertThrows(ConflictException.class, () -> facade.generatePrescription(id, medications, ""));
    }

    @Test
    void createsPrescriptionWithoutConflicts() {
        Long id = demoPatientId();
        Prescription p = facade.generatePrescription(id,
                List.of(new Medication("Acetaminofén", "500 mg cada 8 horas", "3 días")), "");
        assertNotNull(p.id());
    }

    @Test
    void schedulesAppointmentWithReminder() {
        Long id = demoPatientId();
        LocalDateTime monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(15, 0);
        AppointmentResponse response = facade.scheduleAppointment(id, "Pediatría", monday, null);
        assertNotNull(response.appointment().id());
        assertTrue(response.reminder().startsWith("Recordatorio"));
    }

    @Test
    void fullMedicalRecordConsolidatesSubsystems() {
        Long id = demoPatientId();
        List<LabTest> results = facade.requestLabTests(id, List.of("COMPLETE_BLOOD_COUNT"));
        assertEquals(4, results.get(0).parameters().size());

        FullMedicalRecordResponse record = facade.getFullMedicalRecord(id);
        assertFalse(record.patient().allergies().isEmpty());
        assertFalse(record.consultations().isEmpty());
        assertFalse(record.prescriptions().isEmpty());
        assertFalse(record.labTests().isEmpty());
    }
}
