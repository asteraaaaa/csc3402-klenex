package com.klenex.health.klenex.config;

import com.klenex.health.klenex.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SpringSecurity {
    @Bean
    public static PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    /**
     * Roles: ADMIN (everything), STAFF (day-to-day pages, read-only doctor list), DOCTOR (own dashboard,
     * appointments, treatments, patients) and USER (a freshly registered account that an admin has not
     * given a role yet). The first matching rule wins, so specific rules come before general ones.
     */
    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http, CustomUserDetailsService customUserDetailsService) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // The H2 console is off by default (spring.h2.console.enabled=false). If you switch it on
                        // for debugging, only a logged-in admin can open it.
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).hasRole("ADMIN")
                        .requestMatchers("/registration", "/registration/**").permitAll()
                        .requestMatchers("/login", "/login/**").permitAll()

                        // any logged-in account, including one that has no role yet
                        .requestMatchers("/", "/pending", "/access-denied").authenticated()

                        // user and role management: admins only (before the general /admin rule)
                        .requestMatchers("/admin/users", "/admin/users/**").hasRole("ADMIN")
                        .requestMatchers("/admin", "/admin/**").hasAnyRole("ADMIN", "STAFF")

                        // doctor directory: staff can look, only admins can add, edit or delete doctors
                        .requestMatchers("/doctor", "/doctor/search").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/doctor/**").hasRole("ADMIN")

                        // a doctor's own dashboard
                        .requestMatchers("/doctors").hasRole("DOCTOR")

                        .requestMatchers("/medication/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/appointment/**").hasAnyRole("DOCTOR", "ADMIN", "STAFF")
                        .requestMatchers("/patient/**").hasAnyRole("DOCTOR", "ADMIN", "STAFF")
                        .requestMatchers("/treatment/**").hasAnyRole("DOCTOR", "ADMIN", "STAFF")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                // Logging out is a POST (with CSRF token) to /logout; the header has a form for it.
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling((exceptionHandling) -> exceptionHandling.accessDeniedPage("/access-denied"))
                .csrf((csrf) -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")))
                // keep Spring's default security headers; the H2 console only needs same-origin framing
                .headers((headers) -> headers.frameOptions((frame) -> frame.sameOrigin()));
        return http.build();
    }
}
