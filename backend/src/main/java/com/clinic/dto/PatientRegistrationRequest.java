package com.clinic.dto;

import java.time.LocalDate;
import java.util.List;

public record PatientRegistrationRequest(
        String documentNumber,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email,
        String phone,
        String password,
        List<String> allergies) {
}
