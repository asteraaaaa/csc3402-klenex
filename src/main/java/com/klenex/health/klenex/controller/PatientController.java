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
        model.addAttribute("activePage", "patient");
        return "admin/patient";
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
        return "redirect:/patient";
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
    public String updatePatient(@PathVariable("id") long id, @Valid Patient patient,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            patient.setPatientId((int) id);
            return "patient/update-patient";
        }
        patient.setPatientId((int) id);
        patientService.updatePatient(patient);
        return "redirect:/patient";
    }



    @PostMapping("delete/{id}")
    public String deletePatient(@PathVariable("id") int id) {
        System.out.println("Delete request received for ID: " + id);
        patientService.deletePatientById(id);
        System.out.println("Delete operation completed for ID: " + id);
        return "redirect:/patient";
    }

}
