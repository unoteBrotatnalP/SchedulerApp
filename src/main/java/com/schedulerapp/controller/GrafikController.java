package com.schedulerapp.controller;

import com.schedulerapp.model.*;
import com.schedulerapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.List;

@Controller
public class GrafikController {

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @Autowired
    private DyspoRepository dyspoRepository;

    @Autowired
    private UserDyspoRepository userDyspoRepository;

    @Autowired
    private UserDyspoDetailsRepository userDyspoDetailsRepository;

    @Autowired
    private ArchivedDyspoRepository archivedDyspoRepository;

    @Autowired
    private ArchivedUserDyspoRepository archivedUserDyspoRepository;

    @Autowired
    private ArchivedUserDyspoDetailsRepository archivedUserDyspoDetailsRepository;

    @GetMapping("/grafik")
    public String grafikPage(Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);


        return "grafik";
    }


}
