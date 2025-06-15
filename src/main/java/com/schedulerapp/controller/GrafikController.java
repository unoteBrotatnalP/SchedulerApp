package com.schedulerapp.controller;

import com.schedulerapp.model.AdminSettings;
import com.schedulerapp.repository.AdminSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GrafikController {

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @GetMapping("/grafik")
    public String grafikPage(Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        AdminSettings settings = adminSettingsRepository.findById(1L)
                .orElseGet(() -> {
                    AdminSettings s = new AdminSettings();
                    s.setId(1L);
                    s.setDyspoLocked(false);
                    return adminSettingsRepository.save(s);
                });
        model.addAttribute("isDyspoLocked", settings.isDyspoLocked());

        return "grafik";
    }

    @PostMapping("/grafik/toggle-lock")
    public String toggleLock() {
        AdminSettings settings = adminSettingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("AdminSettings not found"));
        settings.setDyspoLocked(!settings.isDyspoLocked());
        adminSettingsRepository.save(settings);
        return "redirect:/grafik";
    }
}
