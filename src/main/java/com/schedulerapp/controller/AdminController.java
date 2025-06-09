package com.schedulerapp.controller;

import com.schedulerapp.model.Dyspo;
import com.schedulerapp.model.User;
import com.schedulerapp.repository.DyspoRepository;
import com.schedulerapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DyspoRepository dyspoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "user-list";
    }

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "add-user";
    }

    @PostMapping("/add-user")
    public String addUser(User user, Model model) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Już istnieje taki użytkownik.");
            model.addAttribute("user", user);
            return "add-user";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return "redirect:/admin/users";
    }

    @GetMapping("/edit-user/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Użytkownik nie znaleziony.");
            return "error";
        }
        model.addAttribute("user", user);
        return "edit-user";
    }

    @PostMapping("/edit-user/{id}")
    public String editUser(@PathVariable Long id, User updatedUser, Model model) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            model.addAttribute("error", "Użytkownik nie znaleziony.");
            return "error";
        }

        userRepository.findByUsername(updatedUser.getUsername())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    model.addAttribute("error", "Nazwa użytkownika jest już zajęta.");
                    model.addAttribute("user", updatedUser);
                });

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmploymentDate(updatedUser.getEmploymentDate());
        existingUser.setJobTitle(updatedUser.getJobTitle());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userRepository.save(existingUser);

        return "redirect:/admin/users";
    }

    @GetMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id, Authentication authentication, Model model) {
        String loggedInUsername = authentication.getName();
        User userToDelete = userRepository.findById(id).orElse(null);

        if (userToDelete == null) {
            model.addAttribute("error", "Użytkownik nie znaleziony.");
            return "error";
        }

        if (userToDelete.getUsername().equals(loggedInUsername)) {
            model.addAttribute("error", "Nie możesz usunąć własnego konta.");
            return "error";
        }

        userRepository.delete(userToDelete);
        return "redirect:/admin/users";
    }

    @PostMapping("/generate-dyspo")
    public String generateDyspo(@RequestParam int month, @RequestParam int year, Model model) {
        try {
            // Logika generowania dyspozycji dla każdego dnia miesiąca
            LocalDate date = LocalDate.of(year, month, 1);
            int daysInMonth = date.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                Dyspo dyspo = new Dyspo();
                dyspo.setDate(LocalDate.of(year, month, day));
                dyspoRepository.save(dyspo);
            }
            model.addAttribute("success", "Dyspozycje zostały wygenerowane.");
        } catch (Exception e) {
            model.addAttribute("error", "Wystąpił problem podczas generowania dyspozycji.");
        }

        return "redirect:/dyspozycja";
    }
}
