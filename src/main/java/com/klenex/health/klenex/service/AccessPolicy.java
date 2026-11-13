package com.klenex.health.klenex.service;

import com.klenex.health.klenex.model.Appointment;
import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.model.Treatment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Decides whether the logged-in person may touch one particular record.
 *
 * <p>The URL rules in {@code SpringSecurity} say which <em>roles</em> may open a page. This class adds the
 * record-level rule on top: admins and staff may see everything, but a doctor may only see their own
 * appointments, the treatments of those appointments, and the patients they have an appointment with.
 * A refusal throws {@link AccessDeniedException}, which Spring Security turns into the 403 Access denied page.
 */
@Component
public class AccessPolicy {
    private final DoctorService doctorService;
    private final PatientService patientService;

    public AccessPolicy(DoctorService doctorService, PatientService patientService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /** Admins and staff are not limited to particular records. */
    public boolean isPrivileged(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
    }

    /** The doctor record behind this login, if there is one. */
    public Optional<Doctor> currentDoctor(Authentication authentication) {
        if (authentication == null) {
            return Optional.empty();
        }
        return doctorService.findByUserEmail(authentication.getName());
    }

    public void requireAppointment(Authentication authentication, Appointment appointment) {
        if (isPrivileged(authentication)) {
            return;
        }
        if (!ownsAppointment(currentDoctor(authentication), appointment)) {
            throw new AccessDeniedException("That appointment is not yours.");
        }
    }

    public void requireTreatment(Authentication authentication, Treatment treatment) {
        if (isPrivileged(authentication)) {
            return;
        }
        if (treatment == null || !ownsAppointment(currentDoctor(authentication), treatment.getAppointment())) {
            throw new AccessDeniedException("That treatment is not yours.");
        }
    }

    /** A doctor may open a patient they have (or had) an appointment with. */
    public void requirePatient(Authentication authentication, int patientId) {
        if (isPrivileged(authentication)) {
            return;
        }
        Doctor doctor = currentDoctor(authentication)
                .orElseThrow(() -> new AccessDeniedException("No doctor record for this account."));
        boolean mine = patientService.listPatientByDoctorId(doctor.getDoctorId()).stream()
                .anyMatch(p -> p.getPatientId() == patientId);
        if (!mine) {
            throw new AccessDeniedException("That patient is not yours.");
        }
    }

    private static boolean ownsAppointment(Optional<Doctor> doctor, Appointment appointment) {
        return doctor.isPresent()
                && appointment != null
                && appointment.getDoctor() != null
                && appointment.getDoctor().getDoctorId().equals(doctor.get().getDoctorId());
    }
}
