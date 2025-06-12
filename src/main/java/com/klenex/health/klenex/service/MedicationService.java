package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Medication;
import java.util.List;
import java.util.Optional;

public interface MedicationService {
    List<Medication> listAllMedications();
    Medication addNewMedication(Medication medication);
    Medication updateMedication(Medication medication);
    void deleteMedicationById(int id);
    Optional<Medication> findMedicationById(int id);
}