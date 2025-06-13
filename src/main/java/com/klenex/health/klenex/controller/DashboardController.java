package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.Appointment;
import com.klenex.health.klenex.service.AppointmentService;
import com.klenex.health.klenex.service.DoctorService;
import com.klenex.health.klenex.service.MedicationService;
import com.klenex.health.klenex.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicationService medicationService;

    @Autowired
    public DashboardController(DoctorService doctorService,
                               PatientService patientService,
                               AppointmentService appointmentService,
                               MedicationService medicationService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.medicationService = medicationService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        // Get counts directly from services
        model.addAttribute("totalDoctors", doctorService.listAllDoctors().size());
        model.addAttribute("totalPatients", patientService.listAllPatients().size());
        model.addAttribute("totalAppointments", appointmentService.listAllAppointments().size());
        model.addAttribute("totalMedications", medicationService.listAllMedications().size());

        // Get recent appointments (last 10)
        List<Appointment> recentAppointments = appointmentService.listAllAppointments().stream()
                .limit(10)
                .collect(Collectors.toList());
        model.addAttribute("appointments", recentAppointments);

        return "admin/dashboard";
    }
}