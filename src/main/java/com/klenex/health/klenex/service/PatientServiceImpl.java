package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Patient;
import com.klenex.health.klenex.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService{
    private final PatientRepository patientRepository;

    // Constructor Injection
    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public List<Patient> listAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Patient addNewPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public Optional<Patient> findPatientById(int id) {
        return patientRepository.findById(id);
    }

    @Override
    public Patient updatePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Override
    public void deletePatientById(int id) {
        patientRepository.deleteById(id);
    }

}
