package com.clinic.service;

import com.clinic.exception.BadRequestException;
import com.clinic.model.LabParameter;
import com.clinic.model.LabTest;
import com.clinic.repository.LabTestRepository;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

/** Subsystem 5: lab test requests with simulated results and reference values. */
@Service
public class LaboratoryService {

    /** Parameter definition: normal range (min/max) and simulation range. */
    private record ParameterDefinition(String name, String unit, Double min, Double max,
                                       double simulatedMin, double simulatedMax) {
    }

    private record TestType(String displayName, List<ParameterDefinition> parameters) {
    }

    private static final Map<String, TestType> CATALOG = new LinkedHashMap<>();

    static {
        CATALOG.put("COMPLETE_BLOOD_COUNT", new TestType("Hemograma", List.of(
                new ParameterDefinition("Hemoglobina", "g/dL", 12.0, 17.5, 10.5, 18.5),
                new ParameterDefinition("Hematocrito", "%", 36.0, 50.0, 32.0, 54.0),
                new ParameterDefinition("Leucocitos", "/µL", 4500.0, 11000.0, 3500.0, 12500.0),
                new ParameterDefinition("Plaquetas", "/µL", 150000.0, 450000.0, 120000.0, 480000.0))));
        CATALOG.put("BLOOD_GLUCOSE", new TestType("Glicemia", List.of(
                new ParameterDefinition("Glucosa en ayunas", "mg/dL", 70.0, 100.0, 65.0, 130.0))));
        CATALOG.put("LIPID_PROFILE", new TestType("Perfil lipídico", List.of(
                new ParameterDefinition("Colesterol total", "mg/dL", null, 200.0, 150.0, 260.0),
                new ParameterDefinition("LDL", "mg/dL", null, 100.0, 70.0, 170.0),
                new ParameterDefinition("HDL", "mg/dL", 40.0, null, 30.0, 75.0),
                new ParameterDefinition("Triglicéridos", "mg/dL", null, 150.0, 90.0, 230.0))));
    }

    private final LabTestRepository labTestRepository;

    public LaboratoryService(LabTestRepository labTestRepository) {
        this.labTestRepository = labTestRepository;
    }

    public List<String> getAvailableTests() {
        return List.copyOf(CATALOG.keySet());
    }

    public List<LabTest> requestTests(Long patientId, List<String> tests) {
        if (tests == null || tests.isEmpty()) {
            throw new BadRequestException("Selecciona al menos un examen.");
        }
        List<String> keys = tests.stream().map(LaboratoryService::toKey).distinct().toList();
        for (String key : keys) {
            if (!CATALOG.containsKey(key)) {
                throw new BadRequestException("Examen no disponible: '" + key
                        + "'. Opciones: " + String.join(", ", CATALOG.keySet()) + ".");
            }
        }
        LocalDateTime now = LocalDateTime.now().withNano(0);
        return keys.stream()
                .map(key -> labTestRepository.save(new LabTest(labTestRepository.nextId(), patientId,
                        CATALOG.get(key).displayName(), now, simulate(CATALOG.get(key)))))
                .toList();
    }

    public List<LabTest> getResults(Long patientId) {
        return labTestRepository.findByPatient(patientId);
    }

    private List<LabParameter> simulate(TestType type) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return type.parameters().stream().map(def -> {
            double value = def.simulatedMin() + random.nextDouble() * (def.simulatedMax() - def.simulatedMin());
            value = value >= 1000 ? Math.round(value / 100.0) * 100.0 : Math.round(value * 10.0) / 10.0;
            boolean normal = (def.min() == null || value >= def.min())
                    && (def.max() == null || value <= def.max());
            return new LabParameter(def.name(), value, def.unit(), def.min(), def.max(), normal);
        }).toList();
    }

    /** "Lipid profile" -> "LIPID_PROFILE" */
    private static String toKey(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase()
                .replaceAll("[\\s-]+", "_");
    }
}
