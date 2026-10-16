package com.clinic.dto;

import java.time.LocalDateTime;

/** doctorId is optional: when missing, the facade assigns the first available doctor. */
public record AppointmentRequest(Long patientId, String specialty, LocalDateTime date, Long doctorId) {
}
