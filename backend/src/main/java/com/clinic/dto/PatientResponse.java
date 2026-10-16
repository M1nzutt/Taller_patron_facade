package com.clinic.dto;

import com.clinic.model.Patient;
import java.time.LocalDate;
import java.util.List;

/** Public patient data (without password) together with the patient's allergies. */
public record PatientResponse(
        Long id,
        String documentNumber,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email,
        String phone,
        List<String> allergies) {

    public static PatientResponse from(Patient p, List<String> allergies) {
        return new PatientResponse(p.id(), p.documentNumber(), p.firstName(), p.lastName(),
                p.birthDate(), p.email(), p.phone(), allergies);
    }
}
