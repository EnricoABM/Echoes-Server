package com.n0hana.echoes_server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping("/dashboard")
    public String dashboard(@CookieValue(value = "access_token", required = false) String token) {

        if (token == null || token.isEmpty()) {
            return "redirect:/";
        }

        return "dashboard";
    }
}
