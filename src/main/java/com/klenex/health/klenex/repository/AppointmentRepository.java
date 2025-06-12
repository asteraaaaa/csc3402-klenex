package com.klenex.health.klenex.repository;

import com.klenex.health.klenex.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    void deleteById(Long id);
    Optional<Appointment> findById(Long id);
}
