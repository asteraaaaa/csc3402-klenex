package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("")
    public String showAllDoctorsForm(Model model) {
        List<Doctor> doctors = doctorService.listAllDoctors();
        model.addAttribute("doctors", doctors != null ? doctors : new ArrayList<>());
        return "admin/doctor"; // 🔁 Make sure this matches your actual template path
    }

    @GetMapping("/signup")
    public String showAddNewDoctorForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "doctor/add-doctor";
    }

    @PostMapping("/add")
    public String addDoctor(@Valid Doctor doctor, BindingResult result) {
        if (result.hasErrors()) {
            return "doctor/add-doctor";
        }

        doctorService.addNewDoctor(doctor);
        return "redirect:/doctor";
    }

    @GetMapping("/update")
    public String showUpdateMainForm(Model model) {
        model.addAttribute("doctors", doctorService.listAllDoctors());
        return "choose-doctor-to-update";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        Doctor doctor = doctorService.findDoctorById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid doctor Id:" + id));
        model.addAttribute("doctor", doctor);
        return "update-doctor";
    }

    @PostMapping("/update/{id}")
    public String updateDoctor(@PathVariable("id") long id, @Valid Doctor doctor, BindingResult result) {
        if (result.hasErrors()) {
            doctor.setDoctorId((int) id);
            return "update-doctor";
        }
        doctor.setDoctorId((int) id);
        doctorService.updateDoctor(doctor);
        return "redirect:/doctor";
    }

    @GetMapping("/delete")
    public String showDeleteMainForm(Model model) {
        model.addAttribute("doctors", doctorService.listAllDoctors());
        return "choose-doctor-to-delete";
    }

    @GetMapping("/delete/{id}")
    public String deleteDoctor(@PathVariable("id") long id, Model model) {
        Doctor doctor = doctorService.findDoctorById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid doctor Id:" + id));
        doctorService.deleteDoctor(doctor);
        return "redirect:/doctor";
    }
}