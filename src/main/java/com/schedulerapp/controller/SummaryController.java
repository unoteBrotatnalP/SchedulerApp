package com.schedulerapp.controller;

import com.schedulerapp.model.User;
import com.schedulerapp.model.UserDyspo;
import com.schedulerapp.model.UserDyspoDetails;
import com.schedulerapp.repository.UserDyspoDetailsRepository;
import com.schedulerapp.repository.UserDyspoRepository;
import com.schedulerapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;

@Controller
public class SummaryController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDyspoDetailsRepository userDyspoDetailsRepository;

    @Autowired
    private UserDyspoRepository userDyspoRepository;

    @GetMapping("/summary")
    public String showSummary(Model model, Authentication authentication,
                              @RequestParam(required = false) Long selectedUserId) {

        // Pobierz zalogowanego użytkownika
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Sprawdź, czy jest adminem
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        model.addAttribute("isAdmin", isAdmin);

        // Pobierz użytkowników niebędących adminami
        List<User> nonAdminUsers = userRepository.findAll().stream()
                .filter(user -> !"ROLE_ADMIN".equals(user.getRole()))
                .toList();

        // Oblicz procent użytkowników, którzy mają completed = true w UserDyspoDetails
        int totalUsers = nonAdminUsers.size();
        long usersWithCompleted = nonAdminUsers.stream()
                .filter(user -> {
                    UserDyspoDetails details = userDyspoDetailsRepository.findByUser(user).orElse(null);
                    return details != null && Boolean.TRUE.equals(details.getCompleted());
                }).count();

        double dyspoPercentage = totalUsers > 0 ? (usersWithCompleted * 100.0 / totalUsers) : 0;
        model.addAttribute("dyspoPercentage", dyspoPercentage);

        if (isAdmin) {
            // Podaj listę użytkowników do wyboru
            model.addAttribute("users", nonAdminUsers);

            if (selectedUserId != null) {
                // Pobierz wybranego użytkownika i jego dane
                User selectedUser = userRepository.findById(selectedUserId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                populateUserDetails(selectedUser, model);
            }
        } else {
            // Dla zwykłego użytkownika pokaż jego dane
            populateUserDetails(currentUser, model);
        }

        if (isAdmin) {
            // Oblicz liczbę dyspozycji na dzień
            Map<LocalDate, Long> dailyDyspoCounts = userDyspoRepository.findAll().stream()
                    .filter(dyspo -> dyspo.getDyspo() != null && dyspo.getDyspo().getDate() != null)
                    .collect(Collectors.groupingBy(dyspo -> dyspo.getDyspo().getDate(), Collectors.counting()));

            model.addAttribute("dailyDyspoCounts", dailyDyspoCounts);
        }


        return "summary";
    }

    private void populateUserDetails(User user, Model model) {
        List<UserDyspo> dyspoList = userDyspoRepository.findByUser(user);
        UserDyspoDetails details = userDyspoDetailsRepository.findByUser(user).orElse(null);

        model.addAttribute("user", user);
        model.addAttribute("userDyspoList", dyspoList);
        model.addAttribute("userDetails", details);
    }
}
