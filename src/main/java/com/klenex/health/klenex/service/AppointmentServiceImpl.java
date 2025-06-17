package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Appointment;
import com.klenex.health.klenex.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public List<Appointment> listAllAppointments(){
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment addNewAppointment(Appointment appointment){
        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment updateAppointment(Appointment appointment){
        return appointmentRepository.save(appointment);
    }

    @Override
    public void deleteAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment != null) {
            appointmentRepository.delete(appointment);  // Triggers cascade
        }
    }

    @Override
    public Optional<Appointment> findAppointmentById(Long id){
        return appointmentRepository.findById(id);
    }

    @Override
    public List<Appointment> findByDoctorId(Long doctorId) {
        return List.of();
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        return List.of();
    }

    @Override
    public List<Appointment> findByTimeContaining(String time) {
        return List.of();
    }

    @Override
    public List<Appointment> findByStatus(String status) {
        return List.of();
    }

    @Override
    public List<Appointment> findByPatientNameContainingIgnoreCase(String searchTerm) {
        return List.of();
    }
}
