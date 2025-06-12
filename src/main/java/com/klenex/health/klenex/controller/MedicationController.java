package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.Medication;
import com.klenex.health.klenex.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/medication")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping("")
    public String showAllMedicationsForm(Model model) {
        model.addAttribute("medications", medicationService.listAllMedications());
        model.addAttribute("activePage", "medication");
        return "admin/medication";
    }

    @GetMapping("signup")
    public String showAddNewMedicationForm(Model model) {
        model.addAttribute("medication", new Medication());
        model.addAttribute("activePage", "medication");
        return "medication/add-medication";
    }

    @PostMapping("add")
    public String addMedication(@Valid Medication medication, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "medication/add-medication";
        }

        medicationService.addNewMedication(medication);
        return "redirect:/medication";
    }

    @GetMapping("update/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        model.addAttribute("activePage", "medication");
        Medication medication = medicationService.findMedicationById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid medication ID: " + id));

        model.addAttribute("medication", medication);
        return "medication/update-medication";
    }

    @PostMapping("update/{id}")
    public String updateMedication(@PathVariable("id") long id, @Valid Medication medication,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            medication.setDrugId((int) id);
            return "medication/update-medication";
        }
        medication.setDrugId((int) id);
        medicationService.updateMedication(medication);
        return "redirect:/medication";
    }

    @PostMapping("delete/{id}")
    public String deleteMedication(@PathVariable("id") int id) {
        medicationService.deleteMedicationById(id);
        return "redirect:/medication";
    }
}