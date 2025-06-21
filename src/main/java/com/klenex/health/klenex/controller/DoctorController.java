package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.model.User;
import com.klenex.health.klenex.repository.DoctorRepository;
import com.klenex.health.klenex.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {
    private final DoctorRepository doctorRepository;
    private final UserService userService;

    public DoctorController(DoctorRepository doctorRepository, UserService userService) {
        this.doctorRepository = doctorRepository;
        this.userService = userService;
    }

    @GetMapping("")
    public String showAllDoctorsForm(Model model) {
        model.addAttribute("activePage", "doctor");
        model.addAttribute("doctors", doctorRepository.findAll());
        return "admin/doctor";
    }

    @GetMapping("signup")
    public String showSignUpForm(Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.findUserByEmail(email);

        boolean isAdmin = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        boolean isStaff = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_STAFF"));

        if (isAdmin) {
            model.addAttribute("activePage", "doctor");
            model.addAttribute("doctor", new Doctor());
            return "doctor/add-doctor";
        }
        return null;
    }

    @PostMapping("add")
    public String addDoctor(@Valid Doctor doctor, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "doctor/add-doctor";
        }

        doctorRepository.save(doctor);
        return "redirect:/doctor";
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

        doctor.setDoctorId((int) id);
        doctorRepository.save(doctor);
        return "redirect:/doctor";
    }

    @PostMapping("delete/{id}")
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