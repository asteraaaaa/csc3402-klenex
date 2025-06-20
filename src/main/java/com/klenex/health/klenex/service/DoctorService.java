package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Doctor;


import java.util.List;
import java.util.Optional;

public interface DoctorService {
    List<Doctor> listAllDoctors();
    Doctor addNewDoctor(Doctor doctor);
    Optional<Doctor> findDoctorById(Integer doctorId);
    Doctor updateDoctor(Doctor doctor);
    void deleteDoctor(Doctor doctor);
    Optional<Doctor> findByUserEmail(String email);
}
