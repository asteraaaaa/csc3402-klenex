package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Medication;
import com.klenex.health.klenex.model.Treatment;
import com.klenex.health.klenex.repository.MedicationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicationServiceImpl implements MedicationService {
    private final MedicationRepository medicationRepository;

    public MedicationServiceImpl(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Override
    public List<Medication> listAllMedications() {
        return medicationRepository.findAll();
    }

    @Override
    public Medication addNewMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    @Override
    public Optional<Medication> findMedicationById(int id) {
        return medicationRepository.findById(id);
    }

    @Override
    public Medication updateMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    @Override
    public void deleteMedicationById(int id) {
        Optional<Medication> optionalMedication = medicationRepository.findById(id);
        if (optionalMedication.isPresent()) {
            Medication medication = optionalMedication.get();

            // Break the @ManyToMany relationship from both sides
            for (Treatment treatment : medication.getTreatments()) {
                treatment.getMedications().remove(medication);
            }

            medication.getTreatments().clear(); // Clear the set in Medication too

            medicationRepository.delete(medication);
        } else {
            throw new EntityNotFoundException("Medication with ID " + id + " not found.");
        }
    }

}