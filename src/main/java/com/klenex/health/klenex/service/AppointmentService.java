package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Appointment;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface AppointmentService {
    List<Appointment> listAllAppointments();
    Appointment addNewAppointment(Appointment appointment);
    Appointment updateAppointment(Appointment appointment);
    void deleteAppointmentById(Long id); // Add this method
    Optional<Appointment> findAppointmentById(Long id);
}
