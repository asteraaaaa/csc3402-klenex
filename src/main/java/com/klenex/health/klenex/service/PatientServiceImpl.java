package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Patient;
import com.klenex.health.klenex.respiratory.PatientRespiratory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService{
    private final PatientRespiratory patientRespiratory;

    // Constructor Injection
    public PatientServiceImpl(PatientRespiratory patientRespiratory) {
        this.patientRespiratory = patientRespiratory;
    }

    @Override
    public List<Patient> listAllPatients() {
        return patientRespiratory.findAll();
    }

    @Override
    public Patient addNewPatient(Patient patient) {
        return patientRespiratory.save(patient);
    }

    public Optional<Patient> findPatientById(int id) {
        return patientRespiratory.findById(id);
    }

    @Override
    public Patient updatePatient(Patient patient) {
        return patientRespiratory.save(patient);
    }
    @Override
    public void deletePatient(Patient patient) {
        patientRespiratory.delete(patient);
    }

    @Override
    public void deletePatientById(int id) {
        patientRespiratory.deleteById(id); // More direct approach
    }

}
