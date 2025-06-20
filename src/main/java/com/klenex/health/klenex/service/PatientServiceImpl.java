package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Patient;
import com.klenex.health.klenex.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public List<Patient> listAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public List<Patient> listPatientByDoctorId(Integer doctorId) {
        return patientRepository.findAll().stream()
                .filter(p -> p.getAppointments().stream()
                        .anyMatch(a -> a.getDoctor().getDoctorId().equals(doctorId)))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Patient addNewPatient(Patient patient) {
        if (patient.getPatientId() != null) {
            Optional<Patient> existing = patientRepository.findById(patient.getPatientId());
            return existing.orElseGet(() -> patientRepository.save(patient));
        }
        return patientRepository.save(patient);
    }

    @Override
    public Patient updatePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Override
    public void deletePatientById(Integer id) {
        patientRepository.deleteById(id);
    }

    @Override
    public Optional<Patient> findPatientById(Integer id) {
        return patientRepository.findById(id);
    }
}
