package com.klenex.health.klenex;

import com.klenex.health.klenex.dto.UserDto;
import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.model.User;
import com.klenex.health.klenex.repository.DoctorRepository;
import com.klenex.health.klenex.repository.UserRepository;
import com.klenex.health.klenex.service.CustomUserDetailsService;
import com.klenex.health.klenex.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Who can open what, how new accounts get a role, and the pages around that. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid}")
@AutoConfigureMockMvc
class KlenexRolesTests {
    @Autowired MockMvc mvc;
    @Autowired UserService userService;
    @Autowired UserRepository users;
    @Autowired DoctorRepository doctors;
    @Autowired CustomUserDetailsService userDetailsService;

    // ---------- helpers ----------

    /** Registers a brand-new account the way the registration page does (ordinary USER role). */
    private String newAccount() {
        String email = "acct-" + UUID.randomUUID() + "@example.com";
        userService.saveUser(new UserDto("New Person", email, UUID.randomUUID().toString()));
        return email;
    }

    /** Makes the next request as this stored account, using the app's own user lookup. */
    private RequestPostProcessor as(String email) {
        return SecurityMockMvcRequestPostProcessors.user(userDetailsService.loadUserByUsername(email));
    }

    private List<String> rolesOf(String email) {
        return users.findByEmail(email).getRoles().stream().map(r -> r.getName()).toList();
    }

    private Long idOf(String email) {
        return users.findByEmail(email).getId();
    }

    // ---------- doctor session (pages that need a real doctor account) ----------

    @Test
    @WithUserDetails("ahmad.ismail@klenex.com")
    void doctorLandsOnTheirOwnDashboardAndCanOpenTheirPages() throws Exception {
        mvc.perform(get("/")).andExpect(redirectedUrl("/doctors"));
        mvc.perform(get("/doctors")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Ahmad")));
        for (String path : List.of("/appointment", "/treatment", "/patient")) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    @WithUserDetails("ahmad.ismail@klenex.com")
    void doctorSidebarShowsTheirAvatarWithAWorkingAddress() throws Exception {
        mvc.perform(get("/doctors"))
                .andExpect(content().string(containsString("src=\"/images/ahmad.svg\"")))
                .andExpect(content().string(not(containsString("+?"))));
    }

    @Test
    @WithUserDetails("ahmad.ismail@klenex.com")
    void doctorDashboardLinksPointAtPagesThatExist() throws Exception {
        mvc.perform(get("/doctors"))
                .andExpect(content().string(containsString("href=\"/appointment\"")))
                .andExpect(content().string(containsString("href=\"/treatment\"")))
                .andExpect(content().string(not(containsString("/appointment/doctor"))))
                .andExpect(content().string(not(containsString("/medication/doctor"))));
    }

    @Test
    @WithUserDetails("ahmad.ismail@klenex.com")
    void doctorCannotManageDoctorsOrOpenAdminPages() throws Exception {
        for (String path : List.of("/doctor", "/doctor/signup", "/doctor/update/1", "/admin", "/admin/users", "/medication")) {
            mvc.perform(get(path)).andExpect(status().isForbidden()).andExpect(forwardedUrl("/access-denied"));
        }
        mvc.perform(post("/doctor/delete/1").with(csrf())).andExpect(status().isForbidden());
        assertEquals(5, doctors.count(), "no doctor may be deleted by a doctor account");
    }

    // ---------- staff ----------

    @Test
    @WithUserDetails("staff@klenex.com")
    void staffCanLookAtDoctorsButNotChangeThem() throws Exception {
        mvc.perform(get("/doctor")).andExpect(status().isOk());
        mvc.perform(get("/admin")).andExpect(status().isOk());
        mvc.perform(get("/medication")).andExpect(status().isOk());

        mvc.perform(get("/doctor/signup")).andExpect(status().isForbidden());
        mvc.perform(post("/doctor/add").with(csrf()).param("name", "Intruder")).andExpect(status().isForbidden());
        mvc.perform(post("/doctor/delete/1").with(csrf())).andExpect(status().isForbidden());
        assertEquals(5, doctors.count());
    }

    @Test
    @WithUserDetails("staff@klenex.com")
    void staffDoesNotSeeDoctorEditingControlsAdminsDo() throws Exception {
        mvc.perform(get("/doctor")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/doctor/signup"))))
                .andExpect(content().string(not(containsString("/doctor/delete"))))
                .andExpect(content().string(not(containsString("/doctor/update"))));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void adminSeesDoctorEditingControls() throws Exception {
        mvc.perform(get("/doctor")).andExpect(status().isOk())
                .andExpect(content().string(containsString("/doctor/signup")))
                .andExpect(content().string(containsString("/doctor/delete/")))
                .andExpect(content().string(containsString("/doctor/update/")));
    }

    @Test
    @WithUserDetails("staff@klenex.com")
    void staffCannotManageUsersAndDoesNotSeeTheLink() throws Exception {
        mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
        mvc.perform(get("/admin")).andExpect(content().string(not(containsString("/admin/users"))));
    }

    // ---------- admin ----------

    @Test
    @WithUserDetails("admin@klenex.com")
    void adminCanAddDoctorsAndSeesTheUsersPage() throws Exception {
        mvc.perform(get("/doctor/signup")).andExpect(status().isOk());
        mvc.perform(get("/admin")).andExpect(content().string(containsString("/admin/users")));
        mvc.perform(get("/admin/users")).andExpect(status().isOk())
                .andExpect(content().string(containsString("admin@klenex.com")))
                .andExpect(content().string(containsString("ahmad.ismail@klenex.com")))
                .andExpect(content().string(containsString("This is you")));
    }

    // ---------- a freshly registered account ----------

    @Test
    void newAccountLandsOnAFriendlyPendingPageInsteadOfAnError() throws Exception {
        String email = newAccount();

        mvc.perform(get("/").with(as(email))).andExpect(redirectedUrl("/pending"));
        mvc.perform(get("/pending").with(as(email))).andExpect(status().isOk())
                .andExpect(content().string(containsString("waiting for a role")))
                .andExpect(content().string(containsString(email)));
    }

    @Test
    void newAccountGetsAnAccessDeniedPageNotAServerErrorOrA404() throws Exception {
        String email = newAccount();

        for (String path : List.of("/admin", "/doctor", "/doctors", "/patient", "/medication", "/appointment", "/treatment", "/admin/users")) {
            mvc.perform(get(path).with(as(email)))
                    .andExpect(status().isForbidden())
                    .andExpect(forwardedUrl("/access-denied"));
        }
        mvc.perform(get("/access-denied").with(as(email))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Access denied")));
    }

    @Test
    void theAccessDeniedPageAlsoAcceptsAPostBecauseTheForwardKeepsTheMethod() throws Exception {
        // MockMvc does not follow the forward, so check the target directly: a refused POST is forwarded
        // here as a POST, and it must show the page rather than "405 Method Not Allowed".
        String email = newAccount();
        mvc.perform(post("/access-denied").with(csrf()).with(as(email)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Access denied")));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void accountsThatAlreadyHaveARoleAreSentAwayFromThePendingPage() throws Exception {
        mvc.perform(get("/pending")).andExpect(redirectedUrl("/"));
    }

    // ---------- assigning roles ----------

    @Test
    @WithUserDetails("admin@klenex.com")
    void adminCanGiveANewAccountTheStaffRole() throws Exception {
        String email = newAccount();

        mvc.perform(post("/admin/users/" + idOf(email) + "/role").with(csrf()).param("role", "ROLE_STAFF"))
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("message", "Role updated."));

        assertEquals(List.of("ROLE_STAFF"), rolesOf(email));
        mvc.perform(get("/").with(as(email))).andExpect(redirectedUrl("/admin"));
        mvc.perform(get("/admin").with(as(email))).andExpect(status().isOk());
        mvc.perform(get("/pending").with(as(email))).andExpect(redirectedUrl("/"));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void adminCannotChangeTheirOwnRole() throws Exception {
        mvc.perform(post("/admin/users/" + idOf("admin@klenex.com") + "/role").with(csrf()).param("role", "ROLE_USER"))
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("error", "You cannot change your own role."));
        assertEquals(List.of("ROLE_ADMIN"), rolesOf("admin@klenex.com"));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void unknownRolesAreRejected() throws Exception {
        String email = newAccount();

        mvc.perform(post("/admin/users/" + idOf(email) + "/role").with(csrf()).param("role", "ROLE_SUPERUSER"))
                .andExpect(flash().attribute("error", "Unknown role."));
        assertEquals(List.of("ROLE_USER"), rolesOf(email));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void theDoctorRoleNeedsADoctorRecordWithTheSameEmail() throws Exception {
        String email = newAccount();

        // no doctor record yet: refused, and the account is left exactly as it was
        mvc.perform(post("/admin/users/" + idOf(email) + "/role").with(csrf()).param("role", "ROLE_DOCTOR"))
                .andExpect(flash().attribute("error", containsString("no doctor record")));
        assertEquals(List.of("ROLE_USER"), rolesOf(email));

        // add the record (without a picture), then it works and links the two
        Doctor record = new Doctor();
        record.setName("Dr. New Person");
        record.setEmail(email);
        doctors.save(record);

        mvc.perform(post("/admin/users/" + idOf(email) + "/role").with(csrf()).param("role", "ROLE_DOCTOR"))
                .andExpect(flash().attribute("message", "Role updated."));
        assertEquals(List.of("ROLE_DOCTOR"), rolesOf(email));

        mvc.perform(get("/").with(as(email))).andExpect(redirectedUrl("/doctors"));
        mvc.perform(get("/doctors").with(as(email))).andExpect(status().isOk())
                // a doctor without a picture gets the default avatar rather than a broken image
                .andExpect(content().string(containsString("/images/default-avatar.svg")));
    }

    @Test
    @WithUserDetails("staff@klenex.com")
    void onlyAdminsCanAssignRoles() throws Exception {
        String email = newAccount();
        mvc.perform(post("/admin/users/" + idOf(email) + "/role").with(csrf()).param("role", "ROLE_ADMIN"))
                .andExpect(status().isForbidden());
        assertEquals(List.of("ROLE_USER"), rolesOf(email));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void assigningARoleNeedsACsrfToken() throws Exception {
        String email = newAccount();
        mvc.perform(post("/admin/users/" + idOf(email) + "/role").param("role", "ROLE_ADMIN"))
                .andExpect(status().isForbidden());
        assertEquals(List.of("ROLE_USER"), rolesOf(email));
    }

    // ---------- logout ----------

    @Test
    void logoutIsAPostThatReallyEndsTheSession() throws Exception {
        String email = newAccount();
        mvc.perform(post("/logout").with(csrf()).with(as(email)))
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());
    }

    @Test
    void thereIsNoFakeGetLogout() throws Exception {
        String email = newAccount();
        // a GET that only redirected would look like a logout while leaving the session open
        mvc.perform(get("/logout").with(as(email))).andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void theLogoutButtonInThePageIsAPostFormNotALink() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(not(containsString("href=\"/logout\""))));
    }

    // ---------- login page and logging ----------

    @Test
    void loginPageHasNoControlsThatDoNothing() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Forgot password"))))
                .andExpect(content().string(not(containsString("Remember me"))))
                .andExpect(content().string(not(containsString("social-btn"))));
    }

    @Test
    void loginAttemptsAreNotPrintedToTheConsole() {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            userDetailsService.loadUserByUsername("admin@klenex.com");
            try {
                userDetailsService.loadUserByUsername("nobody@example.com");
            } catch (Exception expected) {
                // unknown user: expected
            }
        } finally {
            System.setOut(original);
        }
        String out = captured.toString();
        assertFalse(out.contains("admin@klenex.com"), "a login attempt must not print the email address");
        assertFalse(out.contains("nobody@example.com"), "a login attempt must not print the email address");
    }

    // ---------- doctor pictures ----------

    // ---------- adding and editing doctors (found by the end-to-end run) ----------

    @Test
    @WithUserDetails("admin@klenex.com")
    void doctorFormsAskForAnEmail() throws Exception {
        mvc.perform(get("/doctor/signup")).andExpect(content().string(containsString("name=\"email\"")));
        mvc.perform(get("/doctor/update/1")).andExpect(content().string(containsString("name=\"email\"")))
                .andExpect(content().string(containsString("ahmad.ismail@klenex.com")));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void aNewDoctorNeedsAValidEmailAndNothingIsSavedWithoutOne() throws Exception {
        long before = doctors.count();
        mvc.perform(post("/doctor/add").with(csrf()).param("name", "Dr. No Email"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Please enter the doctor")));
        mvc.perform(post("/doctor/add").with(csrf()).param("name", "Dr. Bad Email").param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("valid email")));
        assertEquals(before, doctors.count());
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void aRequestCannotChooseTheIdAccountOrPictureOfANewDoctor() throws Exception {
        String email = "added-" + UUID.randomUUID() + "@example.com";
        mvc.perform(post("/doctor/add").with(csrf())
                        .param("name", "Dr. Added").param("email", email).param("room", "Z1")
                        .param("doctorId", "1").param("user.id", "1").param("image", "evil.svg"))
                .andExpect(redirectedUrl("/doctor"));

        List<Doctor> saved = doctors.findAllByEmailIgnoreCase(email);
        assertEquals(1, saved.size());
        assertNotEquals(1, saved.get(0).getDoctorId(), "must not overwrite doctor 1");
        assertNull(saved.get(0).getUser(), "must not be linked to someone else's login");
        assertNull(saved.get(0).getImage());
        assertEquals("Dr. Ahmad bin Ismail", doctors.findById(1).orElseThrow().getName(), "doctor 1 is untouched");
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void editingADoctorKeepsTheirPictureAndTheirLoginLink() throws Exception {
        // a doctor with a picture and a login account, like the seeded ones
        String email = newAccount();
        Doctor doctor = new Doctor();
        doctor.setName("Dr. Linked");
        doctor.setEmail(email);
        doctor.setPhone("0100000001");
        doctor.setImage("linked.svg");
        doctor.setUser(users.findByEmail(email));
        doctor = doctors.save(doctor);

        // the Edit form posts only its own fields: no picture, no account
        mvc.perform(post("/doctor/update/" + doctor.getDoctorId()).with(csrf())
                        .param("name", "Dr. Linked").param("email", email).param("phone", "0100000002")
                        .param("room", "Q9").param("street", "1 Road").param("city", "Town")
                        .param("state", "State").param("postcode", "11111").param("country", "Malaysia"))
                .andExpect(redirectedUrl("/doctor"));

        Doctor after = doctors.findById(doctor.getDoctorId()).orElseThrow();
        assertEquals("0100000002", after.getPhone(), "the edit is saved");
        assertEquals(email, after.getEmail(), "the email survives");
        assertEquals("linked.svg", after.getImage(), "the picture survives");
        assertNotNull(after.getUser(), "the link to the login account survives");
        assertTrue(doctors.findByUserEmail(email).isPresent(), "the doctor can still be found by their login");
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void editingADoctorWithABlankEmailIsRefused() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setName("Dr. Keep");
        doctor.setEmail("keep-" + UUID.randomUUID() + "@example.com");
        doctor = doctors.save(doctor);
        String originalEmail = doctor.getEmail();

        mvc.perform(post("/doctor/update/" + doctor.getDoctorId()).with(csrf()).param("name", "Dr. Changed").param("email", ""))
                .andExpect(status().isOk());

        Doctor after = doctors.findById(doctor.getDoctorId()).orElseThrow();
        assertEquals("Dr. Keep", after.getName());
        assertEquals(originalEmail, after.getEmail());
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void anAdminCanTurnARegisteredAccountIntoAWorkingDoctorEndToEnd() throws Exception {
        String email = newAccount();

        // 1. the admin adds the doctor with the same email, through the Add Doctor form
        mvc.perform(post("/doctor/add").with(csrf()).param("name", "Dr. Fresh").param("email", email).param("room", "N1"))
                .andExpect(redirectedUrl("/doctor"));
        // 2. then gives the account the Doctor role on the Users screen
        mvc.perform(post("/admin/users/" + idOf(email) + "/role").with(csrf()).param("role", "ROLE_DOCTOR"))
                .andExpect(flash().attribute("message", "Role updated."));
        // 3. and the person lands on their own dashboard
        mvc.perform(get("/").with(as(email))).andExpect(redirectedUrl("/doctors"));
        mvc.perform(get("/doctors").with(as(email))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Dr. Fresh")));
    }

    @Test
    @WithUserDetails("admin@klenex.com")
    void doctorAvatarsAreServedAndTheUnverifiedPhotosAreGone() throws Exception {
        mvc.perform(get("/images/ahmad.svg")).andExpect(status().isOk());
        mvc.perform(get("/images/default-avatar.svg")).andExpect(status().isOk());
        mvc.perform(get("/images/ahmad.jpg")).andExpect(status().isNotFound());
        mvc.perform(get("/doctor")).andExpect(content().string(containsString("/images/ahmad.svg")));
    }
}
