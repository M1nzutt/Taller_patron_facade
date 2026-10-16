package com.clinic.dto;

import java.util.List;

public record DoctorAvailability(
        Long id,
        String name,
        String specialty,
        List<String> availableSlots) {
}
