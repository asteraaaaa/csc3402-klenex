package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Appointment;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentService {
    List<Appointment> listAllAppointments();
    Appointment addNewAppointment(Appointment appointment);
    Appointment updateAppointment(Appointment appointment);
    void deleteAppointmentById(Long id); // Add this method
    Optional<Appointment> findAppointmentById(Long id);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByDate(LocalDate date);
    List<Appointment> findByTimeContaining(String time);
    List<Appointment> findByStatus(String status);
    List<Appointment> findByPatientNameContainingIgnoreCase(String searchTerm);
    List<Appointment> findAppointmentByDoctorId(Integer doctorId);
}
