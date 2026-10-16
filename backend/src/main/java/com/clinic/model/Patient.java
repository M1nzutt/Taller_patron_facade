package com.clinic.model;

import java.time.LocalDate;

/**
 * Patient registered in the portal. The password is never exposed in API
 * responses (see PatientResponse).
 */
public record Patient(
        Long id,
        String documentNumber,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email,
        String phone,
        String password) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
