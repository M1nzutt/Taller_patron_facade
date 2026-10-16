package com.clinic.repository;

import com.clinic.model.Consultation;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultationRepository extends InMemoryRepository<Consultation> {

    public ConsultationRepository() {
        super(Consultation::id);
    }

    public List<Consultation> findByPatient(Long patientId) {
        return filter(c -> c.patientId().equals(patientId)).stream()
                .sorted(Comparator.comparing(Consultation::date).reversed())
                .toList();
    }
}
