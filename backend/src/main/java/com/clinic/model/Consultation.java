package com.clinic.model;

import java.time.LocalDate;

/** Previous medical consultation stored in the medical record. */
public record Consultation(
        Long id,
        Long patientId,
        LocalDate date,
        String doctor,
        String specialty,
        String diagnosis,
        String notes) {
}
