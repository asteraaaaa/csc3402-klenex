package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.model.Appointment;
import com.klenex.health.klenex.model.Patient;
import com.klenex.health.klenex.model.Treatment;
import com.klenex.health.klenex.service.AppointmentService;
import com.klenex.health.klenex.service.DoctorService;
import com.klenex.health.klenex.service.PatientService;

import com.klenex.health.klenex.service.TreatmentService;
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

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 PatientService patientService,
                                 DoctorService doctorService,
                                 TreatmentService treatmentService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.treatmentService = treatmentService;
    }


    @GetMapping("")
    public String listAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.listAllAppointments());
        model.addAttribute("activePage", "appointment");
        return "admin/appointment";
    }

    @GetMapping("/signup")
    public String showAddForm(@RequestParam(value = "patientId", required = false) Integer patientId, Model model) {
        model.addAttribute("appointment", new Appointment());

        if (patientId != null) {
            Optional<Patient> selectedPatient = patientService.findPatientById(patientId);
            if (selectedPatient.isPresent()) {
                model.addAttribute("patient", selectedPatient.get());
            } else {
                model.addAttribute("patient", new Patient());
                model.addAttribute("error", "Patient not found.");
            }
        } else {
            model.addAttribute("patient", new Patient());
        }

        model.addAttribute("patients", patientService.listAllPatients());
        model.addAttribute("doctors", doctorService.listAllDoctors());
        model.addAttribute("activePage", "appointment");
        return "appointment/add-appointment";
    }


    @PostMapping("/add")
    public String addAppointment(@ModelAttribute("appointment") Appointment appointment,
                                 @RequestParam(required = false) Integer patientId,
                                 @ModelAttribute Patient patient,
                                 BindingResult result,
                                 Model model) {

        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.listAllPatients());
            model.addAttribute("doctors", doctorService.listAllDoctors());
            return "appointment/add-appointment";
        }

        // Modified patient handling logic
        if (patientId != null) {
            // Existing patient
            Optional<Patient> existing = patientService.findPatientById(patientId);
            if (existing.isPresent()) {
                appointment.setPatient(existing.get());
            } else {
                model.addAttribute("error", "Selected patient does not exist.");
                return "appointment/add-appointment";
            }
        } else {
            // New patient
            Patient savedPatient = patientService.addNewPatient(patient);
            appointment.setPatient(savedPatient);
        }

        Appointment  savedAppointment = appointmentService.addNewAppointment(appointment);

        // Auto-create a blank treatment linked to the appointment
        Treatment blankTreatment = new Treatment();
        blankTreatment.setAppointment(savedAppointment); // if you have this relationship
        treatmentService.addNewTreatment(blankTreatment); // or whatever your method is

        appointmentService.addNewAppointment(appointment);
        return "redirect:/appointment";
    }

    @GetMapping("/update")
    public String showUpdateMainForm(Model model) {
        model.addAttribute("appointments", appointmentService.listAllAppointments());
        model.addAttribute("activePage", "appointment");
        return "choose-appointment-to-update";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Appointment appointment = appointmentService.findAppointmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid appointment ID: " + id));
        model.addAttribute("appointment", appointment);
        model.addAttribute("patients", patientService.listAllPatients());
        model.addAttribute("doctors", doctorService.listAllDoctors());
        return "appointment/update-appointment";
    }

    @PostMapping("/update/{id}")
    public String updateAppointment(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("appointment") Appointment appointment,
                                    BindingResult result,
                                    @RequestParam("patientId") Integer patientId,
                                    Model model) {
        if (result.hasErrors()) {
            appointment.setId(id);
            model.addAttribute("patients", patientService.listAllPatients());
            model.addAttribute("doctors", doctorService.listAllDoctors());
            return "appointment/update-appointment";
        }

        // 🔒 Prevent updating patient by reattaching the existing one
        Optional<Patient> existingPatient = patientService.findPatientById(patientId);
        appointment.setPatient(existingPatient.orElse(null));

        appointment.setId(id);
        appointmentService.updateAppointment(appointment);
        return "redirect:/appointment/view/{id}";
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable("id") Long id) {
        appointmentService.deleteAppointmentById(id);
        return "redirect:/appointment";
    }

    @GetMapping("/view/{id}")
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