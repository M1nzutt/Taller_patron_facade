package com.clinic.repository;

import com.clinic.model.LabTest;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class LabTestRepository extends InMemoryRepository<LabTest> {

    public LabTestRepository() {
        super(LabTest::id);
    }

    public List<LabTest> findByPatient(Long patientId) {
        return filter(t -> t.patientId().equals(patientId)).stream()
                .sorted(Comparator.comparing(LabTest::date)
                        .thenComparing(LabTest::id).reversed())
                .toList();
    }
}
