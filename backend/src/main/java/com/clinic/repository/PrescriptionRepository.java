package com.clinic.repository;

import com.clinic.model.Prescription;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionRepository extends InMemoryRepository<Prescription> {

    public PrescriptionRepository() {
        super(Prescription::id);
    }

    public List<Prescription> findByPatient(Long patientId) {
        return filter(p -> p.patientId().equals(patientId)).stream()
                .sorted(Comparator.comparing(Prescription::date)
                        .thenComparing(Prescription::id).reversed())
                .toList();
    }
}
