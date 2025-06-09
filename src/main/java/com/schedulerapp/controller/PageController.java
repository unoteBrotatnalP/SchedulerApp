package com.schedulerapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/grafik")
    public String grafik() {
        return "grafik"; // Odnosi się do pliku grafik.html
    }
}
