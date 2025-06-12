package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientService {
    List<Patient> listAllPatients();
    Patient addNewPatient(Patient patient);

    Patient updatePatient(Patient patient);
    void deletePatientById(int id); // Add this method
    Optional<Patient> findPatientById(int id);
}
