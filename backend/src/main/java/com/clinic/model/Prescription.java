package com.clinic.model;

import java.time.LocalDate;
import java.util.List;

public record Prescription(
        Long id,
        Long patientId,
        LocalDate date,
        List<Medication> medications,
        String notes) {
}
