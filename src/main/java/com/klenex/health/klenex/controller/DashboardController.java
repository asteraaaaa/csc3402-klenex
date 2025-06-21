package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.Appointment;
import com.klenex.health.klenex.model.AppointmentStatus;
import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.model.User;
import com.klenex.health.klenex.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicationService medicationService;
    private final TreatmentService treatmentService;
    private final UserService userService;

    @Autowired
    public DashboardController(DoctorService doctorService,
                               PatientService patientService,
                               AppointmentService appointmentService,
                               MedicationService medicationService, TreatmentService treatmentService, UserService userService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.medicationService = medicationService;
        this.treatmentService = treatmentService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String redirectToDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        User user = userService.findUserByEmail(email);

        Doctor doctor = doctorService.findByUserEmail(email).orElse(null);
        // If doctor exists, it's a doctor login
        if (doctor != null) {
            return "redirect:/doctors";
        }

        boolean isStaff = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_STAFF"));
        if (isStaff) {
            return "redirect:/admin";
        }

       return "redirect:/admin";
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        // Get counts directly from services
        model.addAttribute("totalDoctors", doctorService.listAllDoctors().size());
        model.addAttribute("totalPatients", patientService.listAllPatients().size());
        model.addAttribute("totalAppointments", appointmentService.listAllAppointments().size());
        model.addAttribute("totalMedications", medicationService.listAllMedications().size());
        model.addAttribute("activePage", "dashboard");

        // Get recent appointments (last 10)
        List<Appointment> recentAppointments = appointmentService.listAllAppointments().stream()
                .limit(10)
                .collect(Collectors.toList());
        model.addAttribute("appointments", recentAppointments);

        return "admin/dashboard";
    }

    @GetMapping("/doctors")
    public String showDoctorDashboard(Model model, Principal principal) {
        String email = principal.getName();
        Doctor doctor = doctorService.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found for " + email));

        model.addAttribute("activePage", "dashboard");
        if (doctor == null) {
            return "redirect:/login?error";
        }

        Integer docId = doctor.getDoctorId();
        LocalDate today = LocalDate.now();

        // 1) Today's appointments
        List<Appointment> todays = appointmentService.listAllAppointments().stream()
                .filter(a ->
                        a.getDoctor().getDoctorId().equals(docId) &&
                                a.getDate().toLocalDate().equals(today)
                )
                .collect(Collectors.toList());

        // 2) Pending treatments - using appointment status since Treatment doesn't have status
        long pendingTreatments = treatmentService.listAllTreatments().stream()
                .filter(t -> t.getAppointment().getDoctor().getDoctorId().equals(docId))
                .filter(t -> t.getAppointment().getStatus() == AppointmentStatus.Scheduled
                        || t.getAppointment().getStatus() == AppointmentStatus.Comfirmed)
                .count();

        // 3) Pending medications total
        long pendingMedications = treatmentService.listAllTreatments().stream()
                .filter(t -> t.getAppointment().getDoctor().getDoctorId().equals(docId))
                .filter(t -> t.getAppointment().getStatus() == AppointmentStatus.Scheduled
                        || t.getAppointment().getStatus() == AppointmentStatus.Comfirmed)
                .mapToLong(t -> t.getMedications() != null ? t.getMedications().size() : 0)
                .sum();

        // 4) Other stats
        long totalAppointments = appointmentService.listAllAppointments().stream()
                .filter(a -> a.getDoctor().getDoctorId().equals(docId))
                .count();

        long completedTreatments = treatmentService.listAllTreatments().stream()
                .filter(t -> t.getAppointment().getDoctor().getDoctorId().equals(docId))
                .filter(t -> t.getAppointment().getStatus() == AppointmentStatus.Completed)
                .count();

        long totalPatientsSeen = appointmentService.listAllAppointments().stream()
                .filter(a -> a.getDoctor().getDoctorId().equals(docId))
                .map(a -> a.getPatient().getPatientId())
                .distinct()
                .count();

        // 5) Model attributes for Thymeleaf
        model.addAttribute("doctorId", docId);
        model.addAttribute("todayAppointments", todays.size());
        model.addAttribute("appointments", todays);
        model.addAttribute("pendingTreatments", pendingTreatments);
        model.addAttribute("pendingMedications", pendingMedications);
        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("completedTreatments", completedTreatments);
        model.addAttribute("totalPatientsSeen", totalPatientsSeen);
        model.addAttribute("doctor", doctor);

        return "doctor-content/dashboard";
    }
}