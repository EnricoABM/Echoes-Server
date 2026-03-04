package com.n0hana.echoes_server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/hello")
public class HelloWorld {
    
    @GetMapping
    public String hello() {
        return "Hello World";
    }
    
}
