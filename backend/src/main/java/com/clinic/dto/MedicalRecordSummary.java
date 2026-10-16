package com.clinic.dto;

public record MedicalRecordSummary(
        int upcomingAppointments,
        int consultations,
        int prescriptions,
        int labTests,
        int outOfRangeResults) {
}
