package com.clinic.data;

import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.Consultation;
import com.clinic.model.Doctor;
import com.clinic.model.LabParameter;
import com.clinic.model.LabTest;
import com.clinic.model.Medication;
import com.clinic.model.Patient;
import com.clinic.model.Prescription;
import com.clinic.repository.AllergyRepository;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.ConsultationRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.LabTestRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.PrescriptionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Loads the initial in-memory data when the application starts:
 * 3 specialties, 5 doctors and a demo patient with a medical history.
 * Since there is no persistent database, this data is recreated on every start.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    public static final String DEMO_DOCUMENT = "1085000000";
    public static final String DEMO_PASSWORD = "1234";

    private static final List<String> OFFICE_HOURS =
            List.of("08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00");

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AllergyRepository allergyRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabTestRepository labTestRepository;

    public DataSeeder(DoctorRepository doctorRepository,
                      PatientRepository patientRepository,
                      AllergyRepository allergyRepository,
                      AppointmentRepository appointmentRepository,
                      ConsultationRepository consultationRepository,
                      PrescriptionRepository prescriptionRepository,
                      LabTestRepository labTestRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.allergyRepository = allergyRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.labTestRepository = labTestRepository;
    }

    @Override
    public void run(String... args) {
        if (doctorRepository.count() > 0) {
            return;
        }
        loadDoctors();
        loadDemoPatient();
    }

    private void loadDoctors() {
        doctor("Dra. Laura Gómez", "Medicina General");
        doctor("Dr. Andrés Muñoz", "Medicina General");
        doctor("Dr. Carlos Pérez", "Cardiología");
        doctor("Dra. Natalia Rosero", "Cardiología");
        doctor("Dra. Valentina Ortiz", "Pediatría");
    }

    private Doctor doctor(String name, String specialty) {
        return doctorRepository.save(new Doctor(doctorRepository.nextId(), name, specialty, OFFICE_HOURS));
    }

    private void loadDemoPatient() {
        Patient demo = patientRepository.save(new Patient(
                patientRepository.nextId(), DEMO_DOCUMENT, "Camila", "Rodríguez",
                LocalDate.of(1998, 5, 14), "camila.rodriguez@correo.com", "3001234567",
                DEMO_PASSWORD));
        Long id = demo.id();

        allergyRepository.save(id, List.of("Penicilina"));

        Doctor generalPractitioner = doctorRepository.findBySpecialty("Medicina General").get(0);
        Doctor cardiologist = doctorRepository.findBySpecialty("Cardiología").get(0);
        LocalDate today = LocalDate.now();

        appointmentRepository.save(new Appointment(appointmentRepository.nextId(), id,
                generalPractitioner.id(), generalPractitioner.name(), generalPractitioner.specialty(),
                today.minusDays(20).atTime(9, 0), AppointmentStatus.ATTENDED));
        appointmentRepository.save(new Appointment(appointmentRepository.nextId(), id,
                cardiologist.id(), cardiologist.name(), cardiologist.specialty(),
                today.plusDays(3).atTime(10, 0), AppointmentStatus.SCHEDULED));

        consultationRepository.save(new Consultation(consultationRepository.nextId(), id,
                today.minusDays(60), "Dr. Andrés Muñoz", "Medicina General",
                "Faringitis aguda", "Reposo de 3 días e hidratación."));
        consultationRepository.save(new Consultation(consultationRepository.nextId(), id,
                today.minusDays(20), generalPractitioner.name(), generalPractitioner.specialty(),
                "Control general; glucosa levemente elevada",
                "Se ordena glicemia y perfil lipídico. Reducir azúcares."));

        prescriptionRepository.save(new Prescription(prescriptionRepository.nextId(), id,
                today.minusDays(60),
                List.of(new Medication("Acetaminofén", "500 mg cada 8 horas", "5 días"),
                        new Medication("Loratadina", "10 mg cada 24 horas", "7 días")),
                "Tomar después de las comidas."));

        labTestRepository.save(new LabTest(labTestRepository.nextId(), id,
                "Glicemia", today.minusDays(18).atTime(7, 30),
                List.of(new LabParameter("Glucosa en ayunas", 108.0, "mg/dL", 70.0, 100.0, false))));
        labTestRepository.save(new LabTest(labTestRepository.nextId(), id,
                "Perfil lipídico", today.minusDays(18).atTime(7, 30),
                List.of(new LabParameter("Colesterol total", 186.0, "mg/dL", null, 200.0, true),
                        new LabParameter("LDL", 118.0, "mg/dL", null, 100.0, false),
                        new LabParameter("HDL", 52.0, "mg/dL", 40.0, null, true),
                        new LabParameter("Triglicéridos", 140.0, "mg/dL", null, 150.0, true))));
    }
}
