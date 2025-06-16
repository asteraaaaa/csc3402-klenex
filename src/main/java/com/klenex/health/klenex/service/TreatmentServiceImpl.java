package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Treatment;
import com.klenex.health.klenex.repository.TreatmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TreatmentServiceImpl implements TreatmentService {
    private final TreatmentRepository treatmentRepository;

    public TreatmentServiceImpl(TreatmentRepository treatmentRepository) {
        this.treatmentRepository = treatmentRepository;
    }

    @Override
    public List<Treatment> listAllTreatments() {
        return treatmentRepository.findAll();
    }

    @Override
    public Treatment addNewTreatment(Treatment treatment) {
        return treatmentRepository.save(treatment);
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
        return treatmentRepository.save(treatment);
    }

    @Override
    public void deleteTreatmentById(int id) {
        treatmentRepository.deleteById(id);
    }
}