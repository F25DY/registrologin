package com.taller.registrologin.config.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.taller.registrologin.models.Users;
import com.taller.registrologin.servicies.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;




@Controller 
public class appController {

    private UserService userService;

    public appController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String viewHomePage() {
        return "index";
    }
    
    @GetMapping("/register")
    public String registro(Model model) {
        model.addAttribute("user", new Users());
        return "register_form";
    }
    
    @PostMapping("/process_register")
    public String processRegister(
            @Valid @ModelAttribute("user") Users user,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            user.setPassword(null);
            return "register_form";
        }

        try {
            userService.register(user);
            return "registration_success";
        } catch (IllegalArgumentException e) {
            user.setPassword(null);
            model.addAttribute("user", user);
            model.addAttribute("error", e.getMessage());
        }
        return "register_form";
    }
    
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("listUsers", userService.getAllUsers());
        return "users_list";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        model.addAttribute("currentUserEmail", currentUser.getUsername());
        model.addAttribute("currentUserRole", currentUser.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado no tiene un rol asignado."))
                .getAuthority()
                .substring("ROLE_".length()));
        return "profile";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        if (logout != null) {
            model.addAttribute("loggedOut", true);
        }
        return "login_form";
    }


}
