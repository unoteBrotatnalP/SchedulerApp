package com.schedulerapp.controller;

import com.schedulerapp.model.ArchivedDyspo;
import com.schedulerapp.repository.ArchivedDyspoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ArchivedDyspoController {

    @Autowired
    private ArchivedDyspoRepository archivedDyspoRepository;

    @GetMapping("/archiwum")
    public String getArchivedDyspoList(Model model, Authentication authentication) {
        // Sprawdź czy użytkownik ma rolę ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return "redirect:/access-denied"; // lub inna strona o braku dostępu
        }

        // Pobierz wszystkie zarchiwizowane dyspozycje
        List<ArchivedDyspo> archivedList = archivedDyspoRepository.findAll();

        model.addAttribute("archivedList", archivedList);
        return "archiwum"; // nazwa pliku szablonu html
    }
}
