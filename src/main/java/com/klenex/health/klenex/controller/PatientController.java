package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.*;
import com.klenex.health.klenex.repository.UserRepository;
import com.klenex.health.klenex.service.AccessPolicy;
import com.klenex.health.klenex.service.AppointmentService;
import com.klenex.health.klenex.service.DoctorService;
import com.klenex.health.klenex.service.PatientService;
import com.klenex.health.klenex.service.TreatmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final TreatmentService treatmentService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final UserRepository userRepository;
    private final AccessPolicy accessPolicy;

    public PatientController(PatientService patientService, TreatmentService treatmentService, AppointmentService appointmentService, DoctorService doctorService, UserRepository userRepository, AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
        this.treatmentService = treatmentService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.userRepository = userRepository;
    }

    @GetMapping("")
    public String showAllPatientsForm(Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email);

        boolean isAdmin = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        boolean isStaff = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_STAFF"));

        if (isAdmin || isStaff) {
            model.addAttribute("patients", patientService.listAllPatients());
            model.addAttribute("activePage", "patient");
            return "admin/patient";
        } else {
            Doctor doctor = doctorService.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            model.addAttribute("doctor", doctor);
            model.addAttribute("patients", patientService.listPatientByDoctorId(doctor.getDoctorId()));
            model.addAttribute("activePage", "patient");
            return "doctor-content/patient";
        }
    }

    @GetMapping("signup")
    public String showAddNewPatientForm(Model model){
        model.addAttribute("patient", new Patient());
        model.addAttribute("activePage", "patient");
        return "patient/add-patient";
    }


    @PostMapping("add")
    public String addPatient(@Valid Patient patient, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "patient/add-patient";
        }

        patientService.addNewPatient(patient);
        return "redirect:/appointment/signup";
    }

    @GetMapping("update")
    public String showUpdateMainForm(Model model) {
        model.addAttribute("patients", patientService.listAllPatients());
        model.addAttribute("activePage", "patient");
        return "choose-patient-to-update";
    }

    @GetMapping("update/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        model.addAttribute("activePage", "patient");
        Patient patient = patientService.findPatientById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid patient ID: " + (int) id));

        model.addAttribute("patient", patient);
        return "patient/update-patient";
    }

    @PostMapping("update/{id}")
    public String updatePatient(@PathVariable("id") long id, @RequestParam("action") String action,
                                @Valid Patient patient, BindingResult result, Model model) {
        if ("delete".equals(action)) {
            patientService.deletePatientById((int) id);
            return "redirect:/patient";
        }

        if (result.hasErrors()) {
            patient.setPatientId((int) id);
            return "patient/update-patient";
        }

        patient.setPatientId((int) id);
        patientService.updatePatient(patient);
        return "redirect:/patient";
    }

    @GetMapping("view/{id}")
    public String viewPatient(@PathVariable("id") long id, Model model, Authentication authentication) {
        // Retrieve patient
        Patient patient = patientService.findPatientById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid patient ID: " + id));

        // A doctor may only open patients they have an appointment with. Check before loading anything else.
        accessPolicy.requirePatient(authentication, (int) id);

        model.addAttribute("activePage", "patient");
        model.addAttribute("treatments", treatmentService.findTreatmentsByPatientId((int) id));
        model.addAttribute("patient", patient);

        if (accessPolicy.isPrivileged(authentication)) {
            return "patient/view-patient";
        }
        Doctor doctor = accessPolicy.currentDoctor(authentication)
                .orElseThrow(() -> new AccessDeniedException("No doctor record for this account."));
        model.addAttribute("doctor", doctor); // for sidebar image
        model.addAttribute("layout", "layout-doctor");
        return "doctor-content/view-patient";
    }

    @PostMapping("delete/{id}")
    public String deletePatient(@PathVariable("id") int id) {
        patientService.deletePatientById(id);
        return "redirect:/patient";
    }

}
