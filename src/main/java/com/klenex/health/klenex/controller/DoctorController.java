package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.repository.DoctorRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {
    private final DoctorRepository doctorRepository;

    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("")
    public String showAllDoctorsForm(Model model) {
        model.addAttribute("activePage", "doctor");
        model.addAttribute("doctors", doctorRepository.findAll());
        return "admin/doctor";
    }

    @GetMapping("signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("activePage", "doctor");
        model.addAttribute("doctor", new Doctor());
        return "doctor/add-doctor";
    }

    @PostMapping("add")
    public String addDoctor(@Valid Doctor doctor, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "doctor/add-doctor";
        }

        doctorRepository.save(doctor);
        return "redirect:/doctor";
    }

    @GetMapping("update")
    public String showUpdateMainForm(Model model) {
        model.addAttribute("activePage", "doctor");
        model.addAttribute("doctors", doctorRepository.findAll());
        return "doctor/choose-doctor-to-update";
    }

    @GetMapping("update/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        Doctor doctor = doctorRepository.findById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid doctor Id:" + id));
        model.addAttribute("doctor", doctor);
        return "doctor/update-doctor";
    }

    @PostMapping("update/{id}")
    public String updateDoctor(@PathVariable("id") long id, @Valid Doctor doctor, BindingResult result, Model model) {
        if (result.hasErrors()) {
            doctor.setDoctorId((int) id);
            return "doctor/update-doctor";
        }

        doctorRepository.save(doctor);
        return "redirect:/doctor/update";
    }

    @GetMapping("delete")
    public String showDeleteMainForm(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "doctor/choose-doctor-to-delete";
    }

    @GetMapping("delete/{id}")
    public String deleteDoctor(@PathVariable("id") long id, Model model) {
        Doctor doctor = doctorRepository.findById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid doctor Id:" + id));
        doctorRepository.delete(doctor);
        return "redirect:/doctor";
    }

    @GetMapping("/search")
    public String searchDoctors(@RequestParam("keyword") String keyword, Model model) {
        List<Doctor> results = doctorRepository.findByNameContainingIgnoreCase(keyword);
        model.addAttribute("doctors", results);
        return "admin/doctor"; // your view that shows the table
    }

}