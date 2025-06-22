package com.schedulerapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminSettingsController {

    @GetMapping("/adminsettings")
    public String home() {
        return "adminsettings"; // Odnosi się do pliku index.html w templates
    }
}