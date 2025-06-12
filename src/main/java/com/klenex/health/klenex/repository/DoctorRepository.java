package com.klenex.health.klenex.repository;

import com.klenex.health.klenex.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    List<Doctor> findByNameContainingIgnoreCase(String keyword);
    Doctor findByEmail(String email);
}