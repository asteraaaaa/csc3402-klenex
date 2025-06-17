package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Patient;
import com.klenex.health.klenex.repository.PatientRepository;
import com.klenex.health.klenex.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public List<Patient> listAllPatients() {
        return patientRepository.findAll();
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
