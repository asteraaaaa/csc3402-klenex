package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.*;
import com.klenex.health.klenex.service.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    @Autowired
    private TreatmentService treatmentService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccessPolicy accessPolicy;

    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 PatientService patientService,
                                 DoctorService doctorService,
                                 TreatmentService treatmentService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.treatmentService = treatmentService;
    }


    @GetMapping("")
    public String listAppointments(Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.findUserByEmail(email);

        boolean isAdmin = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        boolean isStaff = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_STAFF"));

        if (isAdmin || isStaff) {
            model.addAttribute("appointments", appointmentService.listAllAppointments());
            model.addAttribute("activePage", "appointment");
            return "admin/appointment";
        } else {
            Doctor doctor = doctorService.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            model.addAttribute("doctor", doctor);
            model.addAttribute("appointments", appointmentService.findAppointmentByDoctorId(doctor.getDoctorId()));
            model.addAttribute("activePage", "appointment");
            return "doctor-content/appointment";
        }
    }

    @GetMapping("/signup")
    public String showAddForm(@RequestParam(value = "patientId", required = false) Integer patientId, Model model) {
        model.addAttribute("appointment", new Appointment());

        if (patientId != null) {
            Optional<Patient> selectedPatient = patientService.findPatientById(patientId);
            if (selectedPatient.isPresent()) {
                model.addAttribute("patient", selectedPatient.get());
            } else {
                model.addAttribute("patient", new Patient());
                model.addAttribute("error", "Patient not found.");
            }
        } else {
            model.addAttribute("patient", new Patient());
        }

        model.addAttribute("patients", patientService.listAllPatients());
        model.addAttribute("doctors", doctorService.listAllDoctors());
        model.addAttribute("activePage", "appointment");
        return "appointment/add-appointment";
    }


    @PostMapping("/add")
    public String addAppointment(@ModelAttribute("appointment") Appointment appointment,
                                 @RequestParam(required = false) Integer patientId,
                                 @ModelAttribute Patient patient,
                                 BindingResult result,
                                 Model model) {

        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.listAllPatients());
            model.addAttribute("doctors", doctorService.listAllDoctors());
            return "appointment/add-appointment";
        }

        // Modified patient handling logic
        if (patientId != null) {
            // Existing patient
            Optional<Patient> existing = patientService.findPatientById(patientId);
            if (existing.isPresent()) {
                appointment.setPatient(existing.get());
            } else {
                model.addAttribute("error", "Selected patient does not exist.");
                return "appointment/add-appointment";
            }
        } else {
            // New patient
            Patient savedPatient = patientService.addNewPatient(patient);
            appointment.setPatient(savedPatient);
        }

        Appointment  savedAppointment = appointmentService.addNewAppointment(appointment);

        // Auto-create a blank treatment linked to the appointment
        Treatment blankTreatment = new Treatment();
        blankTreatment.setAppointment(savedAppointment); // if you have this relationship
        treatmentService.addNewTreatment(blankTreatment); // or whatever your method is

        appointmentService.addNewAppointment(appointment);
        return "redirect:/appointment";
    }

    @GetMapping("/update")
    public String showUpdateMainForm(Model model) {
        model.addAttribute("appointments", appointmentService.listAllAppointments());
        model.addAttribute("activePage", "appointment");
        return "choose-appointment-to-update";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Appointment appointment = appointmentService.findAppointmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid appointment ID: " + id));
        model.addAttribute("appointment", appointment);
        model.addAttribute("patients", patientService.listAllPatients());
        model.addAttribute("doctors", doctorService.listAllDoctors());
        return "appointment/update-appointment";
    }

    @PostMapping("/update/{id}")
    public String updateAppointment(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("appointment") Appointment appointment,
                                    BindingResult result,
                                    @RequestParam("patientId") Integer patientId,
                                    Model model) {
        if (result.hasErrors()) {
            appointment.setId(id);
            model.addAttribute("patients", patientService.listAllPatients());
            model.addAttribute("doctors", doctorService.listAllDoctors());
            return "appointment/update-appointment";
        }

        // 🔒 Prevent updating patient by reattaching the existing one
        Optional<Patient> existingPatient = patientService.findPatientById(patientId);
        appointment.setPatient(existingPatient.orElse(null));

        appointment.setId(id);
        appointmentService.updateAppointment(appointment);
        return "redirect:/appointment/view/{id}";
    }

    // Deleting changes data, so it is a POST (with the CSRF token the form carries), never a link that
    // another web page could trigger. Only admins and staff reach it (see SpringSecurity).
    @PostMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable("id") Long id) {
        appointmentService.deleteAppointmentById(id);
        return "redirect:/appointment";
    }

    @GetMapping("/view/{id}")
    public String viewAppointment(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Appointment appointment = appointmentService.findAppointmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid appointment ID: " + id));

        // a doctor may only open their own appointments
        accessPolicy.requireAppointment(authentication, appointment);

        if (accessPolicy.isPrivileged(authentication)) {
            model.addAttribute("appointment", appointment);
            model.addAttribute("activePage", "appointment");
            return "appointment/view-appointment";
        }
        Doctor doctor = accessPolicy.currentDoctor(authentication)
                .orElseThrow(() -> new AccessDeniedException("No doctor record for this account."));

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointment", appointment);
        model.addAttribute("activePage", "appointment");
        return "doctor-content/view-appointment";
    }
}