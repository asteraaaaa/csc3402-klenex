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
        requireEmail(doctor, result);
        if (result.hasErrors()) {
            model.addAttribute("activePage", "doctor");
            return "doctor/add-doctor";
        }

        // Only the fields on the form count. A request must not be able to choose the id, the login account
        // or the appointments of a new doctor.
        doctor.setDoctorId(null);
        doctor.setUser(null);
        doctor.setImage(null);
        doctor.setAppointments(null);
        doctorRepository.save(doctor);
        return "redirect:/doctor";
    }

    /** The email is what links a doctor record to a login account, so it is required. */
    private static void requireEmail(Doctor doctor, BindingResult result) {
        String email = doctor.getEmail();
        if (email == null || email.isBlank()) {
            result.rejectValue("email", "email.required", "Please enter the doctor's email.");
        } else if (!email.contains("@")) {
            result.rejectValue("email", "email.invalid", "Please enter a valid email address.");
        }
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
        Doctor existing = doctorRepository.findById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid doctor Id:" + id));

        requireEmail(doctor, result);
        if (result.hasErrors()) {
            doctor.setDoctorId((int) id);
            doctor.setImage(existing.getImage());
            return "doctor/update-doctor";
        }

        // Copy only what the form edits onto the stored doctor. Saving the posted object itself would wipe the
        // picture and the link to the doctor's login account (fields the form does not contain), and the
        // doctor could no longer reach their dashboard.
        existing.setName(doctor.getName());
        existing.setRoom(doctor.getRoom());
        existing.setPhone(doctor.getPhone());
        existing.setEmail(doctor.getEmail().trim());
        existing.setStreet(doctor.getStreet());
        existing.setCity(doctor.getCity());
        existing.setState(doctor.getState());
        existing.setPostcode(doctor.getPostcode());
        existing.setCountry(doctor.getCountry());
        doctorRepository.save(existing);
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