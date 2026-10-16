package com.clinic.dto;

import com.clinic.model.Medication;
import java.util.List;

public record PrescriptionRequest(Long patientId, List<Medication> medications, String notes) {
}
