package com.clinic.service;

import com.clinic.dto.PatientRegistrationRequest;
import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.exception.UnauthorizedException;
import com.clinic.model.Patient;
import com.clinic.repository.PatientRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/** Subsystem 1: registration, unique document validation and profile lookup. */
@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient register(PatientRegistrationRequest request) {
        if (request == null) {
            throw new BadRequestException("Los datos del paciente son obligatorios.");
        }
        required(request.documentNumber(), "documento");
        required(request.firstName(), "nombres");
        required(request.lastName(), "apellidos");
        required(request.password(), "contraseña");

        String document = request.documentNumber().trim();
        if (!document.matches("\\d{5,15}")) {
            throw new BadRequestException("El documento debe tener entre 5 y 15 dígitos numéricos.");
        }
        if (patientRepository.findByDocumentNumber(document).isPresent()) {
            throw new ConflictException("Ya existe un paciente registrado con el documento " + document + ".");
        }
        if (request.password().length() < 4) {
            throw new BadRequestException("La contraseña debe tener al menos 4 caracteres.");
        }
        if (request.birthDate() != null && request.birthDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de nacimiento no puede ser futura.");
        }
        if (request.email() != null && !request.email().isBlank() && !request.email().contains("@")) {
            throw new BadRequestException("El correo electrónico no es válido.");
        }

        Patient patient = new Patient(patientRepository.nextId(), document,
                request.firstName().trim(), request.lastName().trim(), request.birthDate(),
                clean(request.email()), clean(request.phone()), request.password());
        return patientRepository.save(patient);
    }

    public Patient getById(Long patientId) {
        if (patientId == null) {
            throw new BadRequestException("El id del paciente es obligatorio.");
        }
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un paciente con id " + patientId + "."));
    }

    public Patient authenticate(String documentNumber, String password) {
        if (documentNumber == null || documentNumber.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("Ingresa tu documento y tu contraseña.");
        }
        return patientRepository.findByDocumentNumber(documentNumber)
                .filter(p -> p.password().equals(password))
                .orElseThrow(() -> new UnauthorizedException("Documento o contraseña incorrectos."));
    }

    private static void required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("El campo '" + field + "' es obligatorio.");
        }
    }

    private static String clean(String value) {
        return value == null ? null : value.trim();
    }
}
