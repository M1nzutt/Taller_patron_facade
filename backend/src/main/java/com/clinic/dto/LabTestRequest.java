package com.clinic.dto;

import java.util.List;

public record LabTestRequest(Long patientId, List<String> tests) {
}
