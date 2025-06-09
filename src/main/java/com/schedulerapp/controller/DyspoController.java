package com.schedulerapp.controller;

import com.schedulerapp.model.Dyspo;
import com.schedulerapp.repository.DyspoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DyspoController {

    @Autowired
    private DyspoRepository dyspoRepository;

    @GetMapping("/dyspozycja")
    public String dyspozycja(Model model, Authentication authentication) {
        model.addAttribute("dyspoList", dyspoRepository.findAll());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        return "dyspozycja";
    }

    @PostMapping("/admin/generate-dyspo")
    public String generateDyspo(@RequestParam int year, @RequestParam int month, Model model) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Dyspo> newDyspoEntries = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Dyspo dyspo = new Dyspo();
            dyspo.setDate(date);
            newDyspoEntries.add(dyspo);
        }

        dyspoRepository.saveAll(newDyspoEntries);
        return "redirect:/dyspozycja";
    }
}
