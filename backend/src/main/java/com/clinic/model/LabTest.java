package com.clinic.model;

import java.time.LocalDateTime;
import java.util.List;

public record LabTest(
        Long id,
        Long patientId,
        String type,
        LocalDateTime date,
        List<LabParameter> parameters) {
}
