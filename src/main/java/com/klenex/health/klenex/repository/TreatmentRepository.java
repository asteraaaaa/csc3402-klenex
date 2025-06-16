package com.klenex.health.klenex.repository;

import com.klenex.health.klenex.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Integer> {
    List<Treatment> findByAppointmentPatientPatientId(int patientId);
    List<Treatment> findByAppointmentDoctorDoctorId(int doctorId);

    @Query("SELECT t FROM Treatment t WHERE " +
            "LOWER(t.diagnosis) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.prescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.appointment.patient.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.appointment.doctor.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Treatment> searchByKeyword(String keyword);
}