package com.n0hana.echoes_server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("message", "Olá do Spring + Thymeleaf");

        return "home";
    }
}
