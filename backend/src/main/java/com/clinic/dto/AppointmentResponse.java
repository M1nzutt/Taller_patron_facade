package com.clinic.dto;

import com.clinic.model.Appointment;

public record AppointmentResponse(Appointment appointment, String reminder) {
}
