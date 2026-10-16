package com.clinic.dto;

import com.clinic.model.Appointment;
import com.clinic.model.Consultation;
import com.clinic.model.LabTest;
import com.clinic.model.Prescription;
import java.util.List;

/** Single object that consolidates the information of the five subsystems. */
public record FullMedicalRecordResponse(
        PatientResponse patient,
        List<Appointment> upcomingAppointments,
        List<Appointment> pastAppointments,
        List<Consultation> consultations,
        List<Prescription> prescriptions,
        List<LabTest> labTests,
        MedicalRecordSummary summary) {
}
