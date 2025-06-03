package com.klenex.health.klenex.respiratory;

import com.klenex.health.klenex.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRespiratory extends JpaRepository<Patient, Integer> {
}
