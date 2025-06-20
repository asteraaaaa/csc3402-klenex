package com.klenex.health.klenex.repository;

import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    List<Doctor> findByNameContainingIgnoreCase(String keyword);
    Optional<Doctor> findByUserEmail(String userEmail);
}