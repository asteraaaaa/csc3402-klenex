package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Treatment;
import java.util.List;
import java.util.Optional;

public interface TreatmentService {
    List<Treatment> listAllTreatments();
    Treatment addNewTreatment(Treatment treatment);
    Treatment updateTreatment(Treatment treatment);
    void deleteTreatmentById(int id);
    Optional<Treatment> findTreatmentById(int id);

    List<Treatment> findTreatmentsByDoctorId(int doctorId);

    List<Treatment> findTreatmentsByPatientId(int patientId);
    List<Treatment> searchTreatments(String keyword);

}