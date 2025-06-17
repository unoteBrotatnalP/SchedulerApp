package com.schedulerapp.controller;

import com.schedulerapp.model.*;
import com.schedulerapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;



@Controller
public class DyspoController {

    @Autowired
    private DyspoRepository dyspoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDyspoRepository userDyspoRepository;

    @Autowired
    private UserDyspoDetailsRepository userDetailsRepository;

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @Autowired
    private ArchivedDyspoRepository archivedDyspoRepository;

    @Autowired
    private ArchivedUserDyspoRepository archivedUserDyspoRepository;

    @Autowired
    private ArchivedUserDyspoDetailsRepository archivedUserDyspoDetailsRepository;


    @GetMapping("/dyspozycja")
    public String getDyspozycja(Model model, Authentication authentication) {
        List<Dyspo> dyspoList = dyspoRepository.findAll();
        model.addAttribute("dyspoList", dyspoList);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername).orElse(null);

        Map<Long, UserDyspo> userDyspoMap = new HashMap<>();
        if (user != null) {
            userDyspoRepository.findByUser(user).forEach(ud -> {
                if (ud.getDyspo() != null) {
                    userDyspoMap.put(ud.getDyspo().getId(), ud);
                }
            });
        }
        model.addAttribute("userDyspoMap", userDyspoMap);

        int startDayOffset = 0;
        if (!dyspoList.isEmpty()) {
            LocalDate firstDay = dyspoList.get(0).getDate().withDayOfMonth(1);
            startDayOffset = (firstDay.getDayOfWeek().getValue() % 7) - 1;
            startDayOffset = startDayOffset < 0 ? 6 : startDayOffset;
        }
        model.addAttribute("startDayOffset", startDayOffset);

        if (user != null) {
            UserDyspoDetails details = userDetailsRepository.findByUser(user)
                    .orElse(new UserDyspoDetails());
            model.addAttribute("dyspoDetails", details);
        }

        AdminSettings settings = adminSettingsRepository.findById(1L)
                .orElseGet(() -> {
                    AdminSettings s = new AdminSettings();
                    s.setId(1L);
                    s.setDyspoLocked(false);
                    return adminSettingsRepository.save(s);
                });
        model.addAttribute("isDyspoLocked", settings.isDyspoLocked());

        return "dyspozycja";
    }

    @PostMapping("/dyspo/add")
    public String addUserToDyspo(@RequestParam Long dyspoId, Authentication authentication, RedirectAttributes redirectAttributes) {
        AdminSettings settings = adminSettingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("AdminSettings not found"));

        if (settings.isDyspoLocked()) {
            redirectAttributes.addFlashAttribute("error", "Modyfikacja dyspozycji jest zablokowana.");
            return "redirect:/dyspozycja";
        }

        dyspoRepository.findById(dyspoId).ifPresent(dyspo -> {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            userDyspoRepository.findByDyspoAndUser(dyspo, user).ifPresentOrElse(
                    ud -> { /* User already exists, do nothing */ },
                    () -> {
                        UserDyspo newUserDyspo = new UserDyspo();
                        newUserDyspo.setDyspo(dyspo);
                        newUserDyspo.setUser(user);
                        userDyspoRepository.save(newUserDyspo);
                    }
            );
        });
        return "redirect:/dyspozycja";
    }

    @PostMapping("/dyspo/remove")
    public String removeUserFromDyspo(@RequestParam Long dyspoId, Authentication authentication, RedirectAttributes redirectAttributes) {
        AdminSettings settings = adminSettingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("AdminSettings not found"));

        if (settings.isDyspoLocked()) {
            redirectAttributes.addFlashAttribute("error", "Modyfikacja dyspozycji jest zablokowana.");
            return "redirect:/dyspozycja";
        }

        dyspoRepository.findById(dyspoId).ifPresent(dyspo -> {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            userDyspoRepository.findByDyspoAndUser(dyspo, user)
                    .ifPresent(userDyspoRepository::delete);
        });

        return "redirect:/dyspozycja";
    }



    @PostMapping("/dyspo/add-hour")
    public String setStartHour(@RequestParam Long dyspoId, @RequestParam String startHour, Authentication authentication, RedirectAttributes redirectAttributes) {
        AdminSettings settings = adminSettingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("AdminSettings not found"));

        if (settings.isDyspoLocked()) {
            redirectAttributes.addFlashAttribute("error", "Modyfikacja godzin jest zablokowana.");
            return "redirect:/dyspozycja";
        }

        dyspoRepository.findById(dyspoId).ifPresent(dyspo -> {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserDyspo userDyspo = userDyspoRepository.findByDyspoAndUser(dyspo, user)
                    .orElseGet(() -> {
                        UserDyspo newUserDyspo = new UserDyspo();
                        newUserDyspo.setDyspo(dyspo);
                        newUserDyspo.setUser(user);
                        return newUserDyspo;
                    });

            userDyspo.setStartHour(LocalTime.parse(startHour));
            userDyspoRepository.save(userDyspo);
        });

        return "redirect:/dyspozycja";
    }


    @PostMapping("/dyspo/details/save")
    public String saveDyspoDetails(
            @RequestParam int shiftCount,
            @RequestParam String preference,
            @RequestParam(required = false) boolean completed,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        AdminSettings settings = adminSettingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("AdminSettings not found"));

        if (settings.isDyspoLocked()) {
            redirectAttributes.addFlashAttribute("error", "Edycja szczegółów dyspozycji jest zablokowana.");
            return "redirect:/dyspozycja";
        }

        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDyspoDetails details = userDetailsRepository.findByUser(user)
                .orElseGet(() -> {
                    UserDyspoDetails newDetails = new UserDyspoDetails();
                    newDetails.setUser(user);
                    return newDetails;
                });

        details.setShiftCount(shiftCount);
        details.setPreference(Preference.valueOf(preference));
        details.setCompleted(completed);

        userDetailsRepository.save(details);

        return "redirect:/dyspozycja";
    }

    @PostMapping("/dyspo/toggle-lock")
    public String toggleLock(RedirectAttributes redirectAttributes) {
        AdminSettings settings = adminSettingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("AdminSettings not found"));

        settings.setDyspoLocked(!settings.isDyspoLocked());
        adminSettingsRepository.save(settings);

        redirectAttributes.addFlashAttribute("success", settings.isDyspoLocked()
                ? "Dyspozycja została zablokowana."
                : "Dyspozycja została odblokowana.");

        return "redirect:/dyspozycja";
    }

    @PostMapping("/dyspo/archive")
    public String archiveDyspo(RedirectAttributes redirectAttributes) {
        // Pobierz listę dyspozycji
        List<Dyspo> dyspoList = dyspoRepository.findAll();

        if (dyspoList.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nie ma żadnej dyspozycji do zapisania w archiwum.");
            return "redirect:/dyspozycja";
        }

        // Sprawdź, czy istnieje już dyspozycja w archiwum dla tego miesiąca i roku
        LocalDate firstDayOfMonth = dyspoList.get(0).getDate().withDayOfMonth(1);
        int year = firstDayOfMonth.getYear();
        int month = firstDayOfMonth.getMonthValue();

        boolean alreadyArchived = archivedDyspoRepository.existsByYearAndMonth(year, month);

        if (alreadyArchived) {
            redirectAttributes.addFlashAttribute("error", "Dyspozycja jest już w archiwum.");
            return "redirect:/dyspozycja";
        }

        // Archiwizacja dyspozycji i użytkowników
        ArchivedDyspo archivedDyspo = null;

        for (Dyspo dyspo : dyspoList) {
            // Tworzenie archiwalnej dyspozycji
            archivedDyspo = new ArchivedDyspo();
            archivedDyspo.setDate(dyspo.getDate());
            archivedDyspoRepository.save(archivedDyspo);

            // Pobieranie użytkowników powiązanych z dyspozycją
            List<UserDyspo> userDyspoList = userDyspoRepository.findByDyspo(dyspo);
            for (UserDyspo userDyspo : userDyspoList) {
                ArchivedUserDyspo archivedUserDyspo = new ArchivedUserDyspo();
                archivedUserDyspo.setArchivedDyspo(archivedDyspo);
                archivedUserDyspo.setUser(userDyspo.getUser());
                archivedUserDyspo.setStartHour(userDyspo.getStartHour());
                archivedUserDyspoRepository.save(archivedUserDyspo);
            }
        }

        // Archiwizacja szczegółów użytkowników
        List<UserDyspoDetails> userDetailsList = userDetailsRepository.findAll();
        for (UserDyspoDetails details : userDetailsList) {
            ArchivedUserDyspoDetails archivedDetails = new ArchivedUserDyspoDetails();
            archivedDetails.setUser(details.getUser());
            archivedDetails.setShiftCount(details.getShiftCount());
            archivedDetails.setPreference(details.getPreference());
            archivedDetails.setCompleted(details.getCompleted());

            // Powiązanie z archiwalną dyspozycją
            if (archivedDyspo != null) {
                archivedDetails.setArchivedDyspo(archivedDyspo);
            }

            archivedUserDyspoDetailsRepository.save(archivedDetails);
        }

        redirectAttributes.addFlashAttribute("success", "Obecna dyspozycja została zapisana do archiwum.");
        return "redirect:/dyspozycja";
    }



    @PostMapping("/admin/clear-dyspo")
    public String clearDyspo() {
        userDyspoRepository.deleteAll();
        dyspoRepository.deleteAll();
        userDetailsRepository.deleteAll();
        return "redirect:/dyspozycja";
    }
}
