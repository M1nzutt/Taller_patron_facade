package com.clinic.service;

import com.clinic.model.Consultation;
import com.clinic.repository.AllergyRepository;
import com.clinic.repository.ConsultationRepository;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Subsystem 3: previous consultations, diagnoses and patient allergies. */
@Service
public class MedicalRecordService {

    /** Drugs related to common allergies (allergy -> active ingredients). */
    private static final Map<String, List<String>> DRUG_FAMILIES = Map.of(
            "penicilina", List.of("penicilina", "amoxicilina", "ampicilina", "dicloxacilina", "oxacilina"),
            "aines", List.of("ibuprofeno", "naproxeno", "diclofenaco", "aspirina", "acido acetilsalicilico"),
            "sulfa", List.of("sulfametoxazol", "sulfadiazina", "trimetoprim sulfametoxazol"),
            "cefalosporina", List.of("cefalexina", "ceftriaxona", "cefuroxima"));

    private final ConsultationRepository consultationRepository;
    private final AllergyRepository allergyRepository;

    public MedicalRecordService(ConsultationRepository consultationRepository,
                                AllergyRepository allergyRepository) {
        this.consultationRepository = consultationRepository;
        this.allergyRepository = allergyRepository;
    }

    public List<String> createRecord(Long patientId, List<String> allergies) {
        List<String> cleaned = allergies == null ? List.of() : allergies.stream()
                .filter(a -> a != null && !a.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        return allergyRepository.save(patientId, cleaned);
    }

    public List<String> getAllergies(Long patientId) {
        return allergyRepository.findByPatient(patientId);
    }

    public List<Consultation> getConsultations(Long patientId) {
        return consultationRepository.findByPatient(patientId);
    }

    public Consultation addConsultation(Long patientId, String doctor, String specialty,
                                        String diagnosis, String notes) {
        return consultationRepository.save(new Consultation(consultationRepository.nextId(), patientId,
                LocalDate.now(), doctor, specialty, diagnosis, notes));
    }

    /**
     * Compares the medications with the patient's allergies and returns the conflicts
     * found, e.g. "Amoxicilina (alergia a Penicilina)". An empty list means no risk.
     */
    public List<String> findAllergyConflicts(Long patientId, List<String> medications) {
        List<String> conflicts = new ArrayList<>();
        for (String allergy : getAllergies(patientId)) {
            String a = normalize(allergy);
            for (String medication : medications) {
                String m = normalize(medication);
                if (m.isEmpty()) {
                    continue;
                }
                boolean directMatch = m.contains(a) || a.contains(m);
                boolean familyMatch = DRUG_FAMILIES.entrySet().stream()
                        .filter(f -> a.contains(f.getKey()))
                        .anyMatch(f -> f.getValue().stream().anyMatch(m::contains));
                if (directMatch || familyMatch) {
                    conflicts.add(medication.trim() + " (alergia a " + allergy + ")");
                }
            }
        }
        return conflicts;
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}
