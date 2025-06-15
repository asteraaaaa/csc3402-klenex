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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
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
        return "admin/appointment";
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

        appointmentService.addNewAppointment(appointment);
        return "redirect:/appointment";
    }

    @GetMapping("update")
    public String showUpdateMainForm(Model model) {
        model.addAttribute("appointments", appointmentService.listAllAppointments());
        model.addAttribute("activePage", "appointment");
        return "choose-appointment-to-update";
    }

    @GetMapping("update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Appointment appointment = appointmentService.findAppointmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid appointment ID: " + id));

        List<Patient> patients = patientService.listAllPatients();
        List<Doctor> doctors = doctorService.listAllDoctors();

        model.addAttribute("appointment", appointment);
        model.addAttribute("patients", patients);
        model.addAttribute("doctors", doctors);
        //model.addAttribute("treatments", treatmentService.findAllTreatments());
        model.addAttribute("activePage", "appointment");
        return "appointment/update-appointment";
    }

    @PostMapping("update/{id}")
    public String updateAppointment(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("appointment") Appointment appointment,
                                    BindingResult result,
                                    Model model) {
        if (result.hasErrors()) {
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
        System.out.println("Delete request received for ID: " + id);
        appointmentService.deleteAppointmentById(id);
        System.out.println("Delete operation completed for ID: " + id);
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

    @GetMapping("/search")
    public String searchAppointments(
            @RequestParam(required = false) String searchBy,
            @RequestParam(required = false) String searchTerm,
            Model model) {

        List<Appointment> appointments;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            switch (searchBy) {
                case "patientName":
                    appointments = appointmentService.findByPatientNameContainingIgnoreCase(searchTerm);
                    break;
                case "appointmentId":
                    try {
                        Long id = Long.parseLong(searchTerm);
                        appointments = appointmentService.findAppointmentById(id)
                                .map(List::of)
                                .orElse(Collections.emptyList());
                    } catch (NumberFormatException e) {
                        appointments = Collections.emptyList();
                    }
                    break;
                case "date":
                    try {
                        LocalDate date = LocalDate.parse(searchTerm);
                        appointments = appointmentService.findByDate(date);
                    } catch (DateTimeParseException e) {
                        appointments = Collections.emptyList();
                    }
                    break;
                case "time":
                    appointments = appointmentService.findByTimeContaining(searchTerm);
                    break;
                case "status":
                    appointments = appointmentService.findByStatus(searchTerm);
                    break;
                default:
                    appointments = appointmentService.listAllAppointments();
            }
        } else {
            appointments = appointmentService.listAllAppointments();
        }

        model.addAttribute("appointments", appointments);
        return "admin/appointment"; // Your template name
    }
}
