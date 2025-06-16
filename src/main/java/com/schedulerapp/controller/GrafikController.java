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

    @PostMapping("/grafik/archive-dyspo")
    public String archiveDyspo(RedirectAttributes redirectAttributes) {
        List<Dyspo> dyspoList = dyspoRepository.findAll();

        if (dyspoList.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nie ma żadnej dyspozycji do zapisania w archiwum.");
            return "redirect:/grafik";
        }

        // Archive Dyspo
        for (Dyspo dyspo : dyspoList) {
            ArchivedDyspo archivedDyspo = new ArchivedDyspo();
            archivedDyspo.setDate(dyspo.getDate());
            archivedDyspoRepository.save(archivedDyspo);

            // Archive UserDyspo
            List<UserDyspo> userDyspoList = userDyspoRepository.findByDyspo(dyspo);
            for (UserDyspo userDyspo : userDyspoList) {
                ArchivedUserDyspo archivedUserDyspo = new ArchivedUserDyspo();
                archivedUserDyspo.setArchivedDyspo(archivedDyspo);
                archivedUserDyspo.setUser(userDyspo.getUser());
                archivedUserDyspo.setStartHour(userDyspo.getStartHour());
                archivedUserDyspoRepository.save(archivedUserDyspo);
            }
        }

        // Archive UserDyspoDetails
        List<UserDyspoDetails> userDetailsList = userDyspoDetailsRepository.findAll();
        for (UserDyspoDetails details : userDetailsList) {
            ArchivedUserDyspoDetails archivedDetails = new ArchivedUserDyspoDetails();
            archivedDetails.setUser(details.getUser());
            archivedDetails.setShiftCount(details.getShiftCount());
            archivedDetails.setPreference(details.getPreference());
            archivedDetails.setCompleted(details.getCompleted());
            archivedUserDyspoDetailsRepository.save(archivedDetails);
        }

        redirectAttributes.addFlashAttribute("success", "Obecna dyspozycja została zapisana do archiwum.");
        return "redirect:/grafik";
    }
}
