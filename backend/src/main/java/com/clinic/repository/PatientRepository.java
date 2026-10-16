package com.clinic.repository;

import com.clinic.model.Patient;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PatientRepository extends InMemoryRepository<Patient> {

    public PatientRepository() {
        super(Patient::id);
    }

    public Optional<Patient> findByDocumentNumber(String documentNumber) {
        if (documentNumber == null) {
            return Optional.empty();
        }
        String wanted = documentNumber.trim();
        return findAll().stream()
                .filter(p -> p.documentNumber().equalsIgnoreCase(wanted))
                .findFirst();
    }
}
