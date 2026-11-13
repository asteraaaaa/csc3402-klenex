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
        if (user == null) {
            return "redirect:/login";
        }

        if (hasRole(user, "ROLE_ADMIN") || hasRole(user, "ROLE_STAFF")) {
            return "redirect:/admin";
        }

        // a doctor account lands on their own dashboard
        if (hasRole(user, "ROLE_DOCTOR") && doctorService.findByUserEmail(email).isPresent()) {
            return "redirect:/doctors";
        }

        // a freshly registered account has no working role yet
        return "redirect:/pending";
    }

    /** Shown to accounts that are logged in but have not been given a role by an admin yet. */
    @GetMapping("/pending")
    public String pendingAccount(Model model, Principal principal) {
        User user = userService.findUserByEmail(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }
        // someone who already has a working role does not belong here
        if (hasRole(user, "ROLE_ADMIN") || hasRole(user, "ROLE_STAFF")
                || (hasRole(user, "ROLE_DOCTOR") && doctorService.findByUserEmail(user.getEmail()).isPresent())) {
            return "redirect:/";
        }
        model.addAttribute("name", user.getName());
        model.addAttribute("email", user.getEmail());
        return "pending";
    }

    /** Spring Security forwards here (with a 403 status) when a logged-in user opens a page their role cannot. */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    private static boolean hasRole(User user, String roleName) {
        return user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.getName().equals(roleName));
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
        Doctor doctor = doctorService.findByUserEmail(email).orElse(null);
        if (doctor == null) {
            // doctor role but no doctor record linked to this account
            return "redirect:/pending";
        }

        model.addAttribute("activePage", "dashboard");

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