package com.klenex.health.klenex;

import com.klenex.health.klenex.model.Role;
import com.klenex.health.klenex.model.User;
import com.klenex.health.klenex.repository.DoctorRepository;
import com.klenex.health.klenex.repository.RoleRepository;
import com.klenex.health.klenex.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid}")
@AutoConfigureMockMvc
class KlenexApplicationTests {
    /** The documented demo password from data.sql / README. */
    private static final String DEMO_PASSWORD = "KlenexDemo#2025";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired DoctorRepository doctors;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    // ---------- seed data ----------

    /** Only the seeded accounts: other tests in this class register extra users in the same database. */
    private List<User> seededUsers() {
        return users.findAll().stream().filter(u -> u.getEmail().endsWith("@klenex.com")).toList();
    }

    @Test
    void seedDataIsLoaded() {
        assertEquals(7, seededUsers().size());
        assertEquals(5, doctors.count());
        assertNotNull(roles.findByName("ROLE_USER"), "ROLE_USER must be seeded so registration works");
        assertNotNull(roles.findByName("ROLE_ADMIN"));
        assertNotNull(roles.findByName("ROLE_DOCTOR"));
        assertNotNull(roles.findByName("ROLE_STAFF"));
    }

    @Test
    void everySeededPasswordIsABcryptHashOfTheDemoPassword() {
        for (User u : seededUsers()) {
            assertTrue(u.getPassword().startsWith("$2"), u.getEmail() + " must store a BCrypt hash");
            assertNotEquals(DEMO_PASSWORD, u.getPassword());
            assertTrue(passwordEncoder.matches(DEMO_PASSWORD, u.getPassword()),
                    u.getEmail() + " should accept the documented demo password");
        }
    }

    // ---------- login ----------

    @Test
    void demoAccountsCanLogInWithTheDocumentedPassword() throws Exception {
        for (String email : List.of("admin@klenex.com", "staff@klenex.com", "ahmad.ismail@klenex.com")) {
            mvc.perform(formLogin("/login").user(email).password(DEMO_PASSWORD))
                    .andExpect(authenticated().withUsername(email))
                    .andExpect(redirectedUrl("/"));
        }
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(formLogin("/login").user("admin@klenex.com").password("not-the-password"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void unknownUserIsRejected() throws Exception {
        mvc.perform(formLogin("/login").user("nobody@example.com").password(DEMO_PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    // ---------- anonymous access ----------

    @Test
    void loginAndRegistrationPagesArePublic() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/registration")).andExpect(status().isOk());
    }

    @Test
    void protectedPagesRedirectAnonymousVisitorsToLogin() throws Exception {
        for (String path : List.of("/", "/admin", "/doctor", "/patient", "/medication",
                "/appointment", "/treatment", "/actuator/health", "/actuator/env")) {
            mvc.perform(get(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Test
    void h2ConsoleIsNotOpenToAnonymousVisitors() throws Exception {
        mvc.perform(get("/h2-console/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void h2ConsoleIsDisabledByDefaultEvenForAdmins() throws Exception {
        mvc.perform(get("/h2-console/")).andExpect(status().isNotFound());
    }

    // ---------- roles ----------

    @Test
    @WithUserDetails("admin@klenex.com")   // the real seeded admin, loaded through the app's own UserDetailsService
    void adminCanOpenTheStaffPages() throws Exception {
        for (String path : List.of("/admin", "/doctor", "/patient", "/medication", "/appointment", "/treatment")) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(roles = "USER")
    void ordinaryRegisteredUserIsKeptOutOfStaffPages() throws Exception {
        for (String path : List.of("/admin", "/doctor", "/patient", "/medication", "/appointment", "/treatment")) {
            mvc.perform(get(path)).andExpect(status().isForbidden());
        }
    }

    // ---------- registration (was crashing: seeded ids collided with generated ids) ----------

    @Test
    @Transactional
    void registrationCreatesAnOrdinaryUserWithAHashedPassword() throws Exception {
        String email = "new-" + UUID.randomUUID() + "@example.com";
        String password = UUID.randomUUID().toString();

        mvc.perform(post("/registration").with(csrf())
                        .param("name", "New Person").param("email", email).param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration?success"));

        User saved = users.findByEmail(email);
        assertNotNull(saved, "the new account should be stored");
        assertNotEquals(password, saved.getPassword(), "the password must not be stored in plain text");
        assertTrue(passwordEncoder.matches(password, saved.getPassword()));
        assertEquals(List.of("ROLE_USER"), saved.getRoles().stream().map(Role::getName).toList());
    }

    @Test
    void newlyRegisteredUserCanLogIn() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@example.com";
        String password = UUID.randomUUID().toString();
        mvc.perform(post("/registration").with(csrf())
                        .param("name", "New Person").param("email", email).param("password", password))
                .andExpect(redirectedUrl("/registration?success"));

        mvc.perform(formLogin("/login").user(email).password(password))
                .andExpect(authenticated().withUsername(email));
    }

    @Test
    void registeringAnExistingEmailIsRejected() throws Exception {
        mvc.perform(post("/registration").with(csrf())
                        .param("name", "Copycat").param("email", "admin@klenex.com").param("password", "whatever1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User already registered")));
        assertEquals(1, users.findAll().stream().filter(u -> u.getEmail().equals("admin@klenex.com")).count());
    }

    @Test
    void registrationRejectsMissingFields() throws Exception {
        long before = users.count();
        mvc.perform(post("/registration").with(csrf())
                        .param("name", "").param("email", "not-an-email").param("password", ""))
                .andExpect(status().isOk());
        assertEquals(before, users.count());
    }

    @Test
    void registrationWithoutACsrfTokenIsRefused() throws Exception {
        mvc.perform(post("/registration")
                        .param("name", "No Token").param("email", "no-token@example.com").param("password", "x"))
                .andExpect(status().isForbidden());
        assertNull(users.findByEmail("no-token@example.com"));
    }
}
