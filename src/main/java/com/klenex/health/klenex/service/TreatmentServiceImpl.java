package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Medication;
import com.klenex.health.klenex.model.Treatment;
import com.klenex.health.klenex.repository.TreatmentRepository;
import com.klenex.health.klenex.repository.MedicationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TreatmentServiceImpl implements TreatmentService {
    private final TreatmentRepository treatmentRepository;
    private final MedicationRepository medicationRepository;

    public TreatmentServiceImpl(TreatmentRepository treatmentRepository, MedicationRepository medicationRepository) {
        this.treatmentRepository = treatmentRepository;
        this.medicationRepository = medicationRepository;
    }

    @Override
    public List<Treatment> listAllTreatments() {
        return treatmentRepository.findAll();
    }

    @Override
    public Treatment addNewTreatment(Treatment treatment) {
        Treatment savedTreatment = treatmentRepository.save(treatment);
        reduceMedicationStock(treatment);
        return savedTreatment;
    }

    @Override
    public Optional<Treatment> findTreatmentById(int id) {
        return treatmentRepository.findById(id);
    }

    @Override
    public List<Treatment> findTreatmentsByPatientId(int patientId) {
        return treatmentRepository.findByAppointmentPatientPatientId(patientId);
    }

    @Override
    public List<Treatment> findTreatmentsByDoctorId(int doctorId) {
        return treatmentRepository.findByAppointmentDoctorDoctorId(doctorId);
    }

    @Override
    public List<Treatment> searchTreatments(String keyword) {
        return treatmentRepository.searchByKeyword(keyword);
    }

    @Override
    public Treatment updateTreatment(Treatment treatment) {
        Treatment updatedTreatment = treatmentRepository.save(treatment);
        reduceMedicationStock(treatment);
        return treatmentRepository.save(treatment);
    }

    private void reduceMedicationStock(Treatment treatment) {
        for (Medication med : treatment.getMedications()) {
            Medication medication = medicationRepository.findById(med.getDrugId())
                    .orElseThrow(() -> new EntityNotFoundException("Medication not found: ID" + med.getDrugId()));

            int currentQty = medication.getQuantity();
            if (currentQty <= 0) {
                throw new IllegalStateException("Not enough stock for medication: " + medication.getName());
            }

            medication.setQuantity(currentQty - 1);
            medicationRepository.save(medication);
        }
    }

    @Override
    @Transactional
    public void deleteTreatmentById(int id) {
        treatmentRepository.findById(id).ifPresent(treatment -> {
            // Appointment.treatment is mapped with cascade ALL. If the appointment still points at this
            // treatment, Hibernate re-saves it when the changes are flushed and the delete is silently undone
            // (the Delete button used to do nothing). Clear the link first so the delete sticks.
            if (treatment.getAppointment() != null) {
                treatment.getAppointment().setTreatment(null);
            }
            treatmentRepository.delete(treatment);
        });
    }
}