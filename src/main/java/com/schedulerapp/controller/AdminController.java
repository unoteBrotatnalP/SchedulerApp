package com.schedulerapp.controller;

import com.schedulerapp.model.User;
import com.schedulerapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "user-list";
    }

    @GetMapping("/admin/add-user")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "add-user";
    }

    @PostMapping("/admin/add-user")
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

    @GetMapping("/admin/edit-user/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Użytkownik nie znaleziony.");
            return "error";
        }
        model.addAttribute("user", user);
        return "edit-user"; // nowy widok formularza edycji
    }

    @PostMapping("/admin/edit-user/{id}")
    public String editUser(@PathVariable Long id, User updatedUser, Model model) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            model.addAttribute("error", "Użytkownik nie znaleziony.");
            return "error";
        }

        // Sprawdź, czy nazwa użytkownika nie koliduje z innym użytkownikiem
        userRepository.findByUsername(updatedUser.getUsername())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    model.addAttribute("error", "Nazwa użytkownika jest już zajęta.");
                    model.addAttribute("user", updatedUser);
                });

        // Aktualizuj pola
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmploymentDate(updatedUser.getEmploymentDate());
        existingUser.setJobTitle(updatedUser.getJobTitle());

        // Jeśli hasło zostało podane (niepuste), zakoduj i ustaw nowe
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userRepository.save(existingUser);

        return "redirect:/admin/users";
    }

    @GetMapping("/admin/delete-user/{id}")
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
}
