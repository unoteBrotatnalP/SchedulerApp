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

        return "dyspozycja";
    }

    @PostMapping("/dyspo/add")
    public String addUserToDyspo(@RequestParam Long dyspoId, Authentication authentication) {
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
    public String removeUserFromDyspo(@RequestParam Long dyspoId, Authentication authentication) {
        dyspoRepository.findById(dyspoId).ifPresent(dyspo -> {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            userDyspoRepository.findByDyspoAndUser(dyspo, user)
                    .ifPresent(userDyspoRepository::delete);

            // Usuń UserDyspoDetails
            userDetailsRepository.findByUser(user)
                    .ifPresent(details -> {
                        System.out.println("Deleting UserDyspoDetails with id: " + details.getId());
                        userDetailsRepository.delete(details);
                    });
        });
        return "redirect:/dyspozycja";
    }

    @PostMapping("/dyspo/add-hour")
    public String setStartHour(@RequestParam Long dyspoId, @RequestParam String startHour, Authentication authentication) {
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
            Authentication authentication) {

        // Pobierz użytkownika
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Pobierz lub stwórz nowe szczegóły dyspozycji
        UserDyspoDetails details = userDetailsRepository.findByUser(user)
                .orElseGet(() -> {
                    UserDyspoDetails newDetails = new UserDyspoDetails();
                    newDetails.setUser(user);
                    return newDetails;
                });

        // Zaktualizuj szczegóły
        details.setShiftCount(shiftCount);
        details.setPreference(Preference.valueOf(preference));
        details.setCompleted(completed);

        // Zapisz do repozytorium
        userDetailsRepository.save(details);

        // Przekieruj na stronę dyspozycji
        return "redirect:/dyspozycja";
    }


    @PostMapping("/admin/clear-dyspo")
    public String clearDyspo() {
        userDyspoRepository.deleteAll();
        dyspoRepository.deleteAll();
        return "redirect:/dyspozycja";
    }
}
