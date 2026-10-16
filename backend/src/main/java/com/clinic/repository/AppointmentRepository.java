package com.clinic.repository;

import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentRepository extends InMemoryRepository<Appointment> {

    public AppointmentRepository() {
        super(Appointment::id);
    }

    public List<Appointment> findByPatient(Long patientId) {
        return filter(a -> a.patientId().equals(patientId)).stream()
                .sorted(Comparator.comparing(Appointment::dateTime))
                .toList();
    }

    /** Scheduled (not cancelled) appointments of a doctor on a given day. */
    public List<Appointment> findScheduledByDoctorAndDate(Long doctorId, LocalDate date) {
        return filter(a -> a.doctorId().equals(doctorId)
                && a.status() == AppointmentStatus.SCHEDULED
                && a.dateTime().toLocalDate().equals(date));
    }
}
