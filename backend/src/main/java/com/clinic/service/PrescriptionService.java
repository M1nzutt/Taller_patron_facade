package com.clinic.service;

import com.clinic.exception.BadRequestException;
import com.clinic.model.Medication;
import com.clinic.model.Prescription;
import com.clinic.repository.PrescriptionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/** Subsystem 4: prescriptions with medication, dose and duration. */
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public void validateMedications(List<Medication> medications) {
        if (medications == null || medications.isEmpty()) {
            throw new BadRequestException("La prescripción debe tener al menos un medicamento.");
        }
        for (int i = 0; i < medications.size(); i++) {
            Medication m = medications.get(i);
            if (m == null || isBlank(m.name()) || isBlank(m.dose()) || isBlank(m.duration())) {
                throw new BadRequestException(
                        "El medicamento #" + (i + 1) + " debe tener nombre, dosis y duración.");
            }
        }
    }

    public Prescription create(Long patientId, List<Medication> medications, String notes) {
        validateMedications(medications);
        List<Medication> cleaned = medications.stream()
                .map(m -> new Medication(m.name().trim(), m.dose().trim(), m.duration().trim()))
                .toList();
        return prescriptionRepository.save(new Prescription(prescriptionRepository.nextId(),
                patientId, LocalDate.now(), cleaned, notes == null ? "" : notes.trim()));
    }

    public List<Prescription> getPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatient(patientId);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
