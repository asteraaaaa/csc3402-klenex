package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.Patient;
import com.klenex.health.klenex.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("")
    public String showAllPatientsForm(Model model) {
        model.addAttribute("patients", patientService.listAllPatients());
        return "admin/patient";
    }

    @GetMapping("signup")
    public String showAddNewPatientForm(Model model){
        model.addAttribute("patient", new Patient());
        return "patient/add-patient";
    }


    @PostMapping("add")
    public String addPatient(@Valid Patient patient, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "patient/add-patient";
        }

        patientService.addNewPatient(patient);
        return "redirect:";
    }

    @GetMapping("update")
    public String showUpdateMainForm(Model model) {
        model.addAttribute("patients", patientService.listAllPatients());
        return "choose-patient-to-update";
    }

    @GetMapping("update/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        Patient patient = patientService.findPatientById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid patient ID: " + (int) id));

        model.addAttribute("patient", patient);
        return "update-patient";
    }

    @PostMapping("update/{id}")
    public String updatePatient(@PathVariable("id") long id, @Valid Patient patient,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            patient.setPatientId((int) id);
            return "update-patient";  // Changed from "index" to "update-patient"
        }
        patient.setPatientId((int) id);  // Ensure ID is set
        patientService.updatePatient(patient);
        return "redirect:/patients/list";  // Changed to absolute path
    }


    @GetMapping("delete")
    public String showDeleteMainForm(Model model) {
        model.addAttribute("patients", patientService.listAllPatients());
        return "choose-patient-to-delete";
    }

    @GetMapping("delete/{id}")
    public String deletePatient(@PathVariable("id") int id, Model model) {
        try {
            patientService.deletePatientById(id); // Changed to use ID directly
            return "redirect:/patients/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error deleting patient: " + e.getMessage());
            model.addAttribute("patients", patientService.listAllPatients());
            return "choose-patient-to-delete";
        }
    }

    @PostMapping("delete/{id}")
    public String deletePatient(@PathVariable("id") int id) {
        System.out.println("Delete request received for ID: " + id);
        patientService.deletePatientById(id);
        System.out.println("Delete operation completed for ID: " + id);
        return "redirect:/patients/list";
    }

}
