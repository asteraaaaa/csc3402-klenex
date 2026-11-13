package com.klenex.health.klenex;

import com.klenex.health.klenex.model.Appointment;
import com.klenex.health.klenex.model.AppointmentStatus;
import com.klenex.health.klenex.model.Treatment;
import com.klenex.health.klenex.service.AppointmentService;
import com.klenex.health.klenex.service.DoctorService;
import com.klenex.health.klenex.service.PatientService;
import com.klenex.health.klenex.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Record-level rules: a doctor only reaches their own appointments, treatments and patients, and deleting
 * is a POST with a CSRF token, never a link.
 *
 * <p>Seed data (see data.sql): appointment N has treatment N. Dr. Ahmad (doctor 1) owns appointment 2 /
 * treatment 2 / patient 3 (Nurul Ain Zulkifli); Dr. Priya (doctor 2) owns appointment 1 / treatment 1 /
 * patient 1 (Aisyah Binti Ramli). Anything these tests change is a throwaway record they create.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid}")
@AutoConfigureMockMvc
class KlenexOwnershipTests {
    private static final String AHMAD = "ahmad.ismail@klenex.com";
    private static final String PRIYA = "priya.rajan@klenex.com";

    @Autowired MockMvc mvc;
    @Autowired AppointmentService appointmentService;
    @Autowired TreatmentService treatmentService;
    @Autowired DoctorService doctorService;
    @Autowired PatientService patientService;

    // ---------- helpers ----------

    /** A new appointment (with a treatment) for the given doctor and patient. Returns the treatment. */
    private Treatment newAppointmentWithTreatment(int doctorId, int patientId) {
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctorService.findDoctorById(doctorId).orElseThrow());
        appointment.setPatient(patientService.findPatientById(patientId).orElseThrow());
        appointment.setDate(java.sql.Date.valueOf(LocalDate.now()));
        appointment.setTime(LocalTime.NOON);
        appointment.setStatus(AppointmentStatus.Scheduled);
        Appointment saved = appointmentService.addNewAppointment(appointment);

        Treatment treatment = new Treatment();
        treatment.setAppointment(saved);
        treatment.setDiagnosis("throwaway diagnosis");
        return treatmentService.addNewTreatment(treatment);
    }

    private boolean treatmentExists(int id) {
        return treatmentService.findTreatmentById(id).isPresent();
    }

    private boolean appointmentExists(long id) {
        return appointmentService.findAppointmentById(id).isPresent();
    }

    // ---------- a doctor reaches their own records ----------

    @Test
    @WithUserDetails(AHMAD)
    void doctorCanOpenTheirOwnAppointmentTreatmentAndPatient() throws Exception {
        mvc.perform(get("/appointment/view/2")).andExpect(status().isOk());
        mvc.perform(get("/treatment/view/2")).andExpect(status().isOk());
        mvc.perform(get("/treatment/update/2")).andExpect(status().isOk());
        mvc.perform(get("/patient/view/3")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(PRIYA)
    void theRuleIsNotHardCodedToOneDoctor() throws Exception {
        mvc.perform(get("/appointment/view/1")).andExpect(status().isOk());
        mvc.perform(get("/treatment/view/1")).andExpect(status().isOk());
        mvc.perform(get("/patient/view/1")).andExpect(status().isOk());
        // ...and Dr. Ahmad's records are off limits to Dr. Priya
        mvc.perform(get("/appointment/view/2")).andExpect(status().isForbidden());
        mvc.perform(get("/treatment/view/2")).andExpect(status().isForbidden());
        mvc.perform(get("/patient/view/3")).andExpect(status().isForbidden());
    }

    // ---------- a doctor cannot reach someone else's records ----------

    @Test
    @WithUserDetails(AHMAD)
    void doctorCannotOpenAnotherDoctorsAppointmentTreatmentOrPatient() throws Exception {
        for (String path : List.of("/appointment/view/1", "/treatment/view/1", "/treatment/update/1", "/patient/view/1")) {
            mvc.perform(get(path)).andExpect(status().isForbidden()).andExpect(forwardedUrl("/access-denied"));
        }
    }

    @Test
    @WithUserDetails(AHMAD)
    void doctorCannotChangeAnotherDoctorsTreatment() throws Exception {
        String before = treatmentService.findTreatmentById(1).orElseThrow().getDiagnosis();

        mvc.perform(post("/treatment/update/1").with(csrf())
                        .param("appointment.id", "1").param("diagnosis", "TAMPERED").param("status", "Canceled"))
                .andExpect(status().isForbidden());

        Treatment after = treatmentService.findTreatmentById(1).orElseThrow();
        assertEquals(before, after.getDiagnosis(), "another doctor's diagnosis must not change");
        assertEquals(AppointmentStatus.Completed, after.getAppointment().getStatus());
    }

    @Test
    @WithUserDetails(AHMAD)
    void doctorCannotRepointTheirTreatmentAtSomeoneElsesAppointment() throws Exception {
        Treatment mine = newAppointmentWithTreatment(1, 3);
        long myAppointmentId = mine.getAppointment().getId();

        // the form posts another doctor's appointment id (1) and a new status
        mvc.perform(post("/treatment/update/" + mine.getTreatmentId()).with(csrf())
                        .param("appointment.id", "1").param("diagnosis", "updated").param("status", "Canceled"))
                .andExpect(status().is3xxRedirection());

        Treatment after = treatmentService.findTreatmentById(mine.getTreatmentId()).orElseThrow();
        assertEquals("updated", after.getDiagnosis(), "their own treatment is updated");
        assertEquals(myAppointmentId, after.getAppointment().getId(), "it stays on their own appointment");
        assertEquals(AppointmentStatus.Canceled, after.getAppointment().getStatus());
        assertEquals(AppointmentStatus.Completed, appointmentService.findAppointmentById(1L).orElseThrow().getStatus(),
                "the other doctor's appointment must not be touched");
    }

    @Test
    @WithUserDetails(AHMAD)
    void doctorSeesOnlyTheirOwnPatientsAndAppointmentsInTheLists() throws Exception {
        mvc.perform(get("/appointment")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nurul Ain Zulkifli")))
                .andExpect(content().string(not(containsString("Aisyah Binti Ramli"))));
        mvc.perform(get("/patient")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nurul Ain Zulkifli")))
                .andExpect(content().string(not(containsString("Aisyah Binti Ramli"))));
    }

    // ---------- doctors cannot create, edit or delete appointments and patients ----------

    @Test
    @WithUserDetails(AHMAD)
    void doctorCannotUseTheAppointmentAndPatientEditingPages() throws Exception {
        for (String path : List.of("/patient/signup", "/patient/update/3", "/appointment/signup",
                "/appointment/update/2", "/treatment/signup/2")) {
            mvc.perform(get(path)).andExpect(status().isForbidden());
        }
        for (String path : List.of("/patient/add", "/patient/update/3", "/patient/delete/3", "/appointment/add",
                "/appointment/update/2", "/appointment/delete/2", "/treatment/add")) {
            mvc.perform(post(path).with(csrf())).andExpect(status().isForbidden());
        }
        assertTrue(patientService.findPatientById(3).isPresent(), "the patient must still exist");
        assertTrue(appointmentExists(2), "the appointment must still exist");
    }

    // ---------- deleting is a POST with a CSRF token ----------

    @Test
    @WithUserDetails("admin@klenex.com")
    void aDeleteLinkNoLongerDeletesAnything() throws Exception {
        Treatment t = newAppointmentWithTreatment(1, 3);
        long appointmentId = t.getAppointment().getId();

        mvc.perform(get("/treatment/delete/" + t.getTreatmentId())).andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/appointment/delete/" + appointmentId)).andExpect(status().isMethodNotAllowed());

        assertTrue(treatmentExists(t.getTreatmentId()));
        assertTrue(appointmentExists(appointmentId));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void deletingNeedsACsrfToken() throws Exception {
        Treatment t = newAppointmentWithTreatment(1, 3);
        long appointmentId = t.getAppointment().getId();

        mvc.perform(post("/treatment/delete/" + t.getTreatmentId())).andExpect(status().isForbidden());
        mvc.perform(post("/appointment/delete/" + appointmentId)).andExpect(status().isForbidden());

        assertTrue(treatmentExists(t.getTreatmentId()));
        assertTrue(appointmentExists(appointmentId));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void adminCanDeleteAnAppointmentAndATreatmentWithAPost() throws Exception {
        Treatment t = newAppointmentWithTreatment(1, 3);
        long appointmentId = t.getAppointment().getId();

        mvc.perform(post("/treatment/delete/" + t.getTreatmentId()).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/treatment"));
        assertFalse(treatmentExists(t.getTreatmentId()));

        mvc.perform(post("/appointment/delete/" + appointmentId).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/appointment"));
        assertFalse(appointmentExists(appointmentId));
    }

    @Test
    @WithUserDetails("staff@klenex.com")
    void staffCanDeleteWithAPostToo() throws Exception {
        Treatment t = newAppointmentWithTreatment(1, 3);
        mvc.perform(post("/treatment/delete/" + t.getTreatmentId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertFalse(treatmentExists(t.getTreatmentId()));
    }

    @Test
    @WithUserDetails(AHMAD)
    void doctorCanDeleteOnlyTheirOwnTreatmentAndOnlyWithAPost() throws Exception {
        Treatment mine = newAppointmentWithTreatment(1, 3);
        Treatment theirs = newAppointmentWithTreatment(2, 1);

        mvc.perform(post("/treatment/delete/" + theirs.getTreatmentId()).with(csrf())).andExpect(status().isForbidden());
        assertTrue(treatmentExists(theirs.getTreatmentId()), "another doctor's treatment must survive");

        mvc.perform(get("/treatment/delete/" + mine.getTreatmentId())).andExpect(status().isMethodNotAllowed());
        assertTrue(treatmentExists(mine.getTreatmentId()), "a GET must not delete");

        mvc.perform(post("/treatment/delete/" + mine.getTreatmentId()).with(csrf())).andExpect(status().is3xxRedirection());
        assertFalse(treatmentExists(mine.getTreatmentId()));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void deleteButtonsOnThePagesAreFormsNotLinks() throws Exception {
        Treatment t = newAppointmentWithTreatment(1, 3);
        long appointmentId = t.getAppointment().getId();

        mvc.perform(get("/treatment/update/" + t.getTreatmentId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"deleteTreatmentForm\"")))
                .andExpect(content().string(not(containsString("href=\"/treatment/delete"))));
        mvc.perform(get("/appointment/update/" + appointmentId)).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"deleteAppointmentForm\"")))
                .andExpect(content().string(not(containsString("href=\"/appointment/delete"))));
    }

    @Test
    @WithUserDetails(AHMAD)
    void theDoctorTreatmentPageAlsoUsesAPostForm() throws Exception {
        Treatment mine = newAppointmentWithTreatment(1, 3);
        mvc.perform(get("/treatment/update/" + mine.getTreatmentId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"deleteTreatmentForm\"")))
                .andExpect(content().string(not(containsString("href=\"/treatment/delete"))));
    }

    // ---------- admins and staff are not limited ----------

    @Test
    @WithUserDetails("staff@klenex.com")
    void staffCanOpenAndEditAnyRecord() throws Exception {
        for (String path : List.of("/appointment/view/1", "/appointment/view/2", "/treatment/view/1",
                "/patient/view/1", "/patient/view/3", "/treatment/update/1", "/appointment/update/1", "/patient/update/1")) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void adminCanOpenAnyRecord() throws Exception {
        for (String path : List.of("/appointment/view/1", "/appointment/view/2", "/treatment/view/2",
                "/patient/view/3", "/treatment/update/2")) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }
}
