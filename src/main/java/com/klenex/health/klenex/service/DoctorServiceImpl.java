package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public List<Doctor> listAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public Doctor addNewDoctor(Doctor doctor){
        return doctorRepository.save(doctor);
    }

    public Optional<Doctor> findDoctorById(Integer doctorId) {
        return doctorRepository.findById(doctorId);
    }

    @Override
    public Doctor updateDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public void deleteDoctor(Doctor doctor) {
        doctorRepository.delete(doctor);
    }

    @Override
    public Optional<Doctor> findByUserEmail(String email) {
        return Optional.empty();
    }
}
