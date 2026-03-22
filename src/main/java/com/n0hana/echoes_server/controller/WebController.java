package com.n0hana.echoes_server.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import com.n0hana.echoes_server.model.User;

@Controller
public class WebController {

    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "register";
    }

    @GetMapping("/recover")
    public String recover(Model model) {
        return "recover";
    }

    @GetMapping("/dashboard")
    public String dashboard(@CookieValue(value = "access_token", required = false) String token) {

        if (token == null || token.isEmpty()) {
            return "redirect:/";
        }

        return "dashboard";
    }

    @GetMapping("/dashboard/profile")
    public String profile(Model model, @CookieValue(value = "access_token", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return "redirect:/";
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        model.addAttribute("user", user);

        return "profile";
    }
}
