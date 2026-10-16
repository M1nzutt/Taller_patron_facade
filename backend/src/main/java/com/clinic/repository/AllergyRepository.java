package com.clinic.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** Allergies stored per patient (patientId -> list of allergies). */
@Repository
public class AllergyRepository {

    private final Map<Long, List<String>> allergies = new ConcurrentHashMap<>();

    public List<String> save(Long patientId, List<String> list) {
        List<String> copy = List.copyOf(list);
        allergies.put(patientId, copy);
        return copy;
    }

    public List<String> findByPatient(Long patientId) {
        return allergies.getOrDefault(patientId, List.of());
    }

    public List<String> add(Long patientId, String allergy) {
        List<String> updated = new ArrayList<>(findByPatient(patientId));
        updated.add(allergy);
        return save(patientId, updated);
    }

    public boolean hasRecord(Long patientId) {
        return allergies.containsKey(patientId);
    }
}
