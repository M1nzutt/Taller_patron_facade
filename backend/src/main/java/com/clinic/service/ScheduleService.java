package com.clinic.service;

import com.clinic.dto.DoctorAvailability;
import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.exception.UnauthorizedException;
import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.Doctor;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Subsystem 2: doctor availability, booking and cancelling appointments, reminders. */
@Service
public class ScheduleService {

    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter LONG_DATE =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'a las' HH:mm", Locale.forLanguageTag("es-CO"));

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public ScheduleService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<String> getSpecialties() {
        return doctorRepository.findAllSpecialties();
    }

    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        if (specialty == null || specialty.isBlank()) {
            return doctorRepository.findAll();
        }
        List<Doctor> doctors = doctorRepository.findBySpecialty(specialty);
        if (doctors.isEmpty()) {
            throw new ResourceNotFoundException("No hay médicos para la especialidad '" + specialty
                    + "'. Especialidades disponibles: " + String.join(", ", getSpecialties()) + ".");
        }
        return doctors;
    }

    /** Doctor's slots that are still free that day (and have not passed yet). */
    public List<String> getFreeSlots(Doctor doctor, LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }
        Set<String> taken = appointmentRepository.findScheduledByDoctorAndDate(doctor.id(), date).stream()
                .map(a -> a.dateTime().format(HOUR))
                .collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now();
        return doctor.schedule().stream()
                .filter(slot -> !taken.contains(slot))
                .filter(slot -> LocalDateTime.of(date, LocalTime.parse(slot)).isAfter(now))
                .toList();
    }

    public List<DoctorAvailability> getAvailability(String specialty, LocalDate date) {
        return getDoctorsBySpecialty(specialty).stream()
                .map(d -> new DoctorAvailability(d.id(), d.name(), d.specialty(),
                        date == null ? d.schedule() : getFreeSlots(d, date)))
                .toList();
    }

    public Appointment book(Long patientId, String specialty, LocalDateTime date, Long doctorId) {
        if (specialty == null || specialty.isBlank()) {
            throw new BadRequestException("La especialidad es obligatoria.");
        }
        if (date == null) {
            throw new BadRequestException("La fecha es obligatoria (formato yyyy-MM-ddTHH:mm).");
        }
        LocalDateTime dateTime = date.withSecond(0).withNano(0);
        if (!dateTime.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La cita debe agendarse en una fecha y hora futura.");
        }
        if (dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BadRequestException("La clínica no atiende los domingos.");
        }

        List<Doctor> candidates = getDoctorsBySpecialty(specialty);
        if (doctorId != null) {
            candidates = candidates.stream().filter(d -> d.id().equals(doctorId)).toList();
            if (candidates.isEmpty()) {
                throw new BadRequestException(
                        "El médico seleccionado no pertenece a la especialidad " + specialty + ".");
            }
        }

        boolean patientBusy = appointmentRepository.findByPatient(patientId).stream()
                .anyMatch(a -> a.status() == AppointmentStatus.SCHEDULED && a.dateTime().equals(dateTime));
        if (patientBusy) {
            throw new ConflictException("Ya tienes una cita programada en ese mismo horario.");
        }

        String slot = dateTime.format(HOUR);
        Doctor doctor = candidates.stream()
                .filter(d -> getFreeSlots(d, dateTime.toLocalDate()).contains(slot))
                .findFirst()
                .orElseThrow(() -> new ConflictException("No hay disponibilidad en " + specialty
                        + " para el " + dateTime.format(LONG_DATE) + ". Elige otro horario."));

        Appointment appointment = new Appointment(appointmentRepository.nextId(), patientId, doctor.id(),
                doctor.name(), doctor.specialty(), dateTime, AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    public Appointment cancel(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la cita con id " + appointmentId + "."));
        if (!appointment.patientId().equals(patientId)) {
            throw new UnauthorizedException("La cita no pertenece a este paciente.");
        }
        if (appointment.status() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException("Solo se pueden cancelar citas programadas.");
        }
        return appointmentRepository.save(appointment.withStatus(AppointmentStatus.CANCELLED));
    }

    public List<Appointment> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatient(patientId);
    }

    public String buildReminder(Appointment appointment) {
        return "Recordatorio: tienes cita de " + appointment.specialty() + " con " + appointment.doctorName()
                + " el " + appointment.dateTime().format(LONG_DATE)
                + ". Llega 15 minutos antes con tu documento de identidad.";
    }
}
