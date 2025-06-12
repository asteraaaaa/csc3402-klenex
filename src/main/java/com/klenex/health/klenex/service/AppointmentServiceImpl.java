package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Appointment;
import com.klenex.health.klenex.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
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
    public void deleteAppointmentById(Long id){
        appointmentRepository.deleteById(id);
    }

    @Override
    public Optional<Appointment> findAppointmentById(Long id){
        return appointmentRepository.findById(id);
    }
}
