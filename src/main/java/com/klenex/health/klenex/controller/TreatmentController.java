package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.*;
import com.klenex.health.klenex.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/treatment")
public class TreatmentController {

    private final TreatmentService treatmentService;
    private final AppointmentService appointmentService;
    private final MedicationService medicationService;

    public TreatmentController(TreatmentService treatmentService,
                               AppointmentService appointmentService,
                               MedicationService medicationService) {
        this.treatmentService = treatmentService;
        this.appointmentService = appointmentService;
        this.medicationService = medicationService;
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
    public String showAllTreatmentsForm(Model model) {
        model.addAttribute("treatments", treatmentService.listAllTreatments());
        model.addAttribute("activePage", "treatment");
        return "admin/treatment";
    }

    @GetMapping("/add")
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
    public String showUpdateForm(@PathVariable("id") int id, Model model) {
        Treatment treatment = treatmentService.findTreatmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid treatment ID: " + id));

        model.addAttribute("treatment", treatment);
        model.addAttribute("activePage", "treatment");
        return "treatment/update-treatment";
    }

    @PostMapping("/update/{id}")
    public String updateTreatment(@PathVariable("id") int id,
                                  @Valid @ModelAttribute Treatment treatment,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "treatment");
            return "treatment/update-treatment";
        }

        treatment.setTreatmentId(id);
        treatmentService.updateTreatment(treatment);
        redirectAttributes.addFlashAttribute("successMessage", "Treatment updated successfully!");
        return "redirect:/treatment";
    }

    @PostMapping("/delete/{id}")
    public String deleteTreatment(@PathVariable("id") int id,
                                  RedirectAttributes redirectAttributes) {
        treatmentService.deleteTreatmentById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Treatment deleted successfully!");
        return "redirect:/treatment";
    }
}
