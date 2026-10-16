package com.clinic.model;

import java.time.LocalDateTime;

public record Appointment(
        Long id,
        Long patientId,
        Long doctorId,
        String doctorName,
        String specialty,
        LocalDateTime dateTime,
        AppointmentStatus status) {

    /** Returns a copy of the appointment with a new status (records are immutable). */
    public Appointment withStatus(AppointmentStatus newStatus) {
        return new Appointment(id, patientId, doctorId, doctorName, specialty, dateTime, newStatus);
    }
}
