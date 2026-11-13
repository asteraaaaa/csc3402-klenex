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

    @Test
    @WithUserDetails("admin@klenex.com")
    void doctorAvatarsAreServedAndTheUnverifiedPhotosAreGone() throws Exception {
        mvc.perform(get("/images/ahmad.svg")).andExpect(status().isOk());
        mvc.perform(get("/images/default-avatar.svg")).andExpect(status().isOk());
        mvc.perform(get("/images/ahmad.jpg")).andExpect(status().isNotFound());
        mvc.perform(get("/doctor")).andExpect(content().string(containsString("/images/ahmad.svg")));
    }
}
