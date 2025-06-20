package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.*;
import com.klenex.health.klenex.service.*;
import jakarta.validation.Valid;
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

    public TreatmentController(TreatmentService treatmentService,
                               AppointmentService appointmentService,
                               MedicationService medicationService,
                               PatientService patientService,
                               DoctorService doctorService, UserService userService) {
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

        if(isAdmin) {
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
    public String showUpdateForm(@PathVariable("id") int id, Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.findUserByEmail(email);

        Treatment treatment = treatmentService.findTreatmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid treatment ID: " + id));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            Doctor doctor = doctorService.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            if (!treatment.getAppointment().getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
                throw new SecurityException("Access denied.");
            }
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
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<Medication> medications = medicationService.listAllMedications();
            model.addAttribute("medications", medications);
            model.addAttribute("activePage", "treatment");
            return "treatment/update-treatment";
        }

        treatment.setTreatmentId(id);

        // Fetch latest appointment from DB
        Appointment appointment = appointmentService.findAppointmentById(
                        treatment.getAppointment().getId())
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


    @GetMapping("/delete/{id}")
    public String deleteTreatment(@PathVariable("id") int id,
                                  RedirectAttributes redirectAttributes) {
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
    public String viewTreatment(@PathVariable("id") int id, Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.findUserByEmail(email);

        Treatment treatment = treatmentService.findTreatmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid treatment ID: " + id));

        boolean isAdmin = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            Doctor doctor = doctorService.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            if (!treatment.getAppointment().getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
                throw new SecurityException("Access denied: This treatment is not yours.");
            }

            model.addAttribute("doctor", doctor);
            model.addAttribute("treatment", treatment);
            model.addAttribute("activePage", "treatment");
            return "doctor-content/view-treatment";
        }

        model.addAttribute("treatment", treatment);
        model.addAttribute("activePage", "treatment");
        return "treatment/view-treatment";
    }
}