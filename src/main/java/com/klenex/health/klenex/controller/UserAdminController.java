package com.klenex.health.klenex.controller;

import com.klenex.health.klenex.service.UserService;
import com.klenex.health.klenex.service.UserServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Admin-only page for giving registered accounts a role. Access is enforced in SpringSecurity. */
@Controller
@RequestMapping("/admin/users")
public class UserAdminController {
    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public String listUsers(Model model, Principal principal) {
        model.addAttribute("users", userService.listAllUsers());
        model.addAttribute("roles", UserServiceImpl.ASSIGNABLE_ROLES);
        model.addAttribute("currentEmail", principal.getName());
        model.addAttribute("activePage", "users");
        return "admin/users";
    }

    @PostMapping("/{id}/role")
    public String assignRole(@PathVariable("id") Long id, @RequestParam("role") String role,
                             Principal principal, RedirectAttributes redirect) {
        try {
            userService.assignRole(id, role, principal.getName());
            redirect.addFlashAttribute("message", "Role updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
