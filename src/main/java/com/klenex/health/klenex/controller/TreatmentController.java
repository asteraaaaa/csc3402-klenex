package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.*;
import com.klenex.health.klenex.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/treatment")
public class TreatmentController {

    private final TreatmentService treatmentService;
    private final AppointmentService appointmentService;
    private final MedicationService medicationService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final UserService userService;
    private final AccessPolicy accessPolicy;

    public TreatmentController(TreatmentService treatmentService,
                               AppointmentService appointmentService,
                               MedicationService medicationService,
                               PatientService patientService,
                               DoctorService doctorService, UserService userService,
                               AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
        this.treatmentService = treatmentService;
        this.appointmentService = appointmentService;
        this.medicationService = medicationService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.userService = userService;
    }


    @ModelAttribute("appointments")
    public List<Appointment> appointments() {
        return appointmentService.listAllAppointments();
    }

    @ModelAttribute("medications")
    public List<Medication> medications() {
        return medicationService.listAllMedications();
    }

    @GetMapping("")
    public String showAllTreatmentsForm(Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.findUserByEmail(email);

        boolean isAdmin = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        boolean isStaff = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_STAFF"));

        if(isAdmin || isStaff) {
            model.addAttribute("treatments", treatmentService.listAllTreatments());
            model.addAttribute("activePage", "treatment");
            return "admin/treatment";
        } else {
            Doctor doctor = doctorService.findByUserEmail(email).
                    orElseThrow(() -> new RuntimeException("Doctor not found"));
            model.addAttribute("treatments", treatmentService.findTreatmentsByDoctorId(doctor.getDoctorId()));
            model.addAttribute("doctor", doctor);
            model.addAttribute("activePage", "treatment");
            return "doctor-content/treatment";
        }
    }

//    @GetMapping("/patient/{id}")
//    public String listTreatmentsByPatient(@PathVariable("id") int id, Model model) {
//        Patient patient = patientService.findPatientById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid patient ID: " + id));
//
//
//        model.addAttribute("treatments", treatmentService.listAllTreatments());
//        model.addAttribute("patient", patient);
//        model.addAttribute("activePage", "treatment");
//        return "treatment/view-patient-treatment";
//    }
//
//    @GetMapping("/doctor/{id}")
//    public String listTreatmentsByDoctor(@PathVariable("id") int id, Model model) {
//        Doctor doctor = doctorService.findDoctorById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid doctor ID: " + id));
//
//        List<Treatment> doctorTreatments = treatmentService.findTreatmentsByDoctorId(id);
//
//        model.addAttribute("doctor", doctor);
//        model.addAttribute("treatments", doctorTreatments);
//        model.addAttribute("activePage", "treatment");
//        return "admin/view-doctor-treatment";
//    }


    @GetMapping("/signup/{id}")
    public String showAddNewTreatmentForm(Model model) {
        model.addAttribute("treatment", new Treatment());
        model.addAttribute("activePage", "treatment");
        return "treatment/add-treatment";
    }

    @PostMapping("/add")
    public String addTreatment(@Valid @ModelAttribute Treatment treatment,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "treatment");
            return "treatment/add-treatment";
        }

        treatmentService.addNewTreatment(treatment);
        redirectAttributes.addFlashAttribute("successMessage", "Treatment added successfully!");
        return "redirect:/treatment";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") int id, Model model, Authentication authentication) {
        Treatment treatment = treatmentService.findTreatmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid treatment ID: " + id));

        // a doctor may only edit the treatments of their own appointments
        accessPolicy.requireTreatment(authentication, treatment);

        if (!accessPolicy.isPrivileged(authentication)) {
            Doctor doctor = accessPolicy.currentDoctor(authentication)
                    .orElseThrow(() -> new AccessDeniedException("No doctor record for this account."));
            model.addAttribute("doctor", doctor);
            model.addAttribute("treatment", treatment);
            model.addAttribute("medications", medicationService.listAllMedications());
            model.addAttribute("activePage", "treatment");
            return "doctor-content/update-treatment";
        }

        List<Medication> medications = medicationService.listAllMedications();
        model.addAttribute("treatment", treatment);
        model.addAttribute("medications", medications);
        model.addAttribute("activePage", "treatment");
        return "treatment/update-treatment";
    }

    @PostMapping("/update/{id}")
    public String updateTreatment(@PathVariable("id") int id,
                                  @Valid @ModelAttribute("treatment") Treatment treatment,
                                  BindingResult result,
                                  Model model,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        // Check against the stored treatment, not against what the form posted
        Treatment stored = treatmentService.findTreatmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid treatment ID: " + id));
        accessPolicy.requireTreatment(authentication, stored);

        if (result.hasErrors()) {
            List<Medication> medications = medicationService.listAllMedications();
            model.addAttribute("medications", medications);
            model.addAttribute("activePage", "treatment");
            return "treatment/update-treatment";
        }

        treatment.setTreatmentId(id);

        // The treatment keeps the appointment it already belongs to. The appointment id in the form is ignored,
        // otherwise a doctor could re-point their treatment at (and change the status of) someone else's appointment.
        Appointment appointment = appointmentService.findAppointmentById(stored.getAppointment().getId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // ⬇️ Apply the status from the form to the fetched appointment
        if (treatment.getStatus() != null) {
            appointment.setStatus(treatment.getStatus());
        }

        // Set the updated appointment back to treatment
        treatment.setAppointment(appointment);

        // Save both
        appointmentService.updateAppointment(appointment); // Optional if cascade works
        treatmentService.updateTreatment(treatment);

        redirectAttributes.addFlashAttribute("successMessage", "Treatment updated successfully!");
        return "redirect:/treatment/view/{id}";
    }


    // Deleting changes data, so it is a POST (with the CSRF token the form carries), never a link that
    // another web page could trigger. A doctor may only delete treatments of their own appointments.
    @PostMapping("/delete/{id}")
    public String deleteTreatment(@PathVariable("id") int id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Treatment treatment = treatmentService.findTreatmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid treatment ID: " + id));
        accessPolicy.requireTreatment(authentication, treatment);

        treatmentService.deleteTreatmentById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Treatment deleted successfully!");
        return "redirect:/treatment";
    }

//    @GetMapping("/view/{id}")
//    public String viewTreatment(@PathVariable("id") int id, Model model) {
//        Treatment treatment = treatmentService.findTreatmentById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid treatment ID: " + id));
//
//        model.addAttribute("treatment", treatment);
//        model.addAttribute("activePage", "treatment");
//        int patientId = treatment.getAppointment().getPatient().getPatientId();
//        return "redirect:view-treatment" + patientId;
//    }

    @GetMapping("/view/{id}")
    public String viewTreatment(@PathVariable("id") int id, Model model, Authentication authentication) {
        Treatment treatment = treatmentService.findTreatmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid treatment ID: " + id));

        // a doctor may only open the treatments of their own appointments
        accessPolicy.requireTreatment(authentication, treatment);

        if (accessPolicy.isPrivileged(authentication)) {
            model.addAttribute("treatment", treatment);
            model.addAttribute("activePage", "treatment");
            return "treatment/view-treatment";
        }
        Doctor doctor = accessPolicy.currentDoctor(authentication)
                .orElseThrow(() -> new AccessDeniedException("No doctor record for this account."));

        model.addAttribute("doctor", doctor);
        model.addAttribute("treatment", treatment);
        model.addAttribute("activePage", "treatment");
        return "doctor-content/view-treatment";
    }
}