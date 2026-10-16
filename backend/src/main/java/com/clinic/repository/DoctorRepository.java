package com.clinic.repository;

import com.clinic.model.Doctor;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class DoctorRepository extends InMemoryRepository<Doctor> {

    public DoctorRepository() {
        super(Doctor::id);
    }

    public List<Doctor> findBySpecialty(String specialty) {
        return filter(d -> d.specialty().equalsIgnoreCase(specialty.trim()));
    }

    public List<String> findAllSpecialties() {
        return findAll().stream()
                .map(Doctor::specialty)
                .distinct()
                .sorted()
                .toList();
    }
}
