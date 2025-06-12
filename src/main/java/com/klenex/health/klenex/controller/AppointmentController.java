package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.Appointment;
import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.model.Patient;
import com.klenex.health.klenex.model.Treatment;
import com.klenex.health.klenex.service.AppointmentService;
import com.klenex.health.klenex.service.DoctorService;
import com.klenex.health.klenex.service.PatientService;
//import com.klenex.health.klenex.service.TreatmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    //private final TreatmentService treatmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 PatientService patientService,
                                 DoctorService doctorService/*,
                                 TreatmentService treatmentService*/) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        //this.treatmentService = treatmentService;
    }

    @GetMapping("")
    public String listAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.listAllAppointments());
        model.addAttribute("activePage", "appointment");
        return "appointment/list";
    }

    @GetMapping("signup")
    public String showAddForm(Model model) {
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("patients", patientService.listAllPatients());
        model.addAttribute("doctors", doctorService.listAllDoctors());
        //model.addAttribute("treatments", treatmentService.findAllTreatments());
        model.addAttribute("activePage", "appointment");
        return "appointment/add-appointment";
    }

    @PostMapping("add")
    public String addAppointment(@Valid @ModelAttribute Appointment appointment,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.listAllPatients());
            model.addAttribute("doctors", doctorService.listAllDoctors());
            //model.addAttribute("treatments", treatmentService.findAllTreatments());
            return "appointment/add-appointment";
        }

        appointmentService.updateAppointment(appointment);
        return "redirect:/appointment";
    }

    @GetMapping("update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Optional<Appointment> optionalAppointment = appointmentService.findAppointmentById(id);
        if (optionalAppointment.isEmpty()) {
            throw new IllegalArgumentException("Invalid appointment ID: " + id);
        }

        model.addAttribute("appointment", optionalAppointment.get());
        model.addAttribute("patients", patientService.listAllPatients());
        model.addAttribute("doctors", doctorService.listAllDoctors());
        //model.addAttribute("treatments", treatmentService.findAllTreatments());
        model.addAttribute("activePage", "appointment");
        return "appointment/update-appointment";
    }

    @PostMapping("update/{id}")
    public String updateAppointment(@PathVariable("id") Long id,
                                    @Valid Appointment appointment,
                                    BindingResult result,
                                    Model model) {
        if (result.hasErrors()) {
            appointment.setId(id);
            model.addAttribute("patients", patientService.listAllPatients());
            model.addAttribute("doctors", doctorService.listAllDoctors());
            //model.addAttribute("treatments", treatmentService.findAllTreatments());
            return "appointment/update-appointment";
        }

        appointment.setId(id);
        appointmentService.updateAppointment(appointment);
        return "redirect:/appointment";
    }

    @PostMapping("delete/{id}")
    public String deleteAppointment(@PathVariable("id") Long id) {
        appointmentService.deleteAppointmentById(id);
        return "redirect:/appointment";
    }

    @GetMapping("view/{id}")
    public String viewAppointment(@PathVariable("id") Long id, Model model) {
        Optional<Appointment> appointment = appointmentService.findAppointmentById(id);
        if (appointment.isEmpty()) {
            throw new IllegalArgumentException("Invalid appointment ID: " + id);
        }

        model.addAttribute("appointment", appointment.get());
        model.addAttribute("activePage", "appointment");
        return "appointment/view-appointment";
    }
}
