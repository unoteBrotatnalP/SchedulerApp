package com.schedulerapp.controller;

import com.schedulerapp.model.Dyspo;
import com.schedulerapp.model.User;
import com.schedulerapp.model.UserDyspo;
import com.schedulerapp.repository.DyspoRepository;
import com.schedulerapp.repository.UserDyspoRepository;
import com.schedulerapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/dyspozycja")
    public String getDyspozycja(Model model, Authentication authentication) {
        List<Dyspo> dyspoList = dyspoRepository.findAll();
        model.addAttribute("dyspoList", dyspoList);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElse(null);

        Map<Long, UserDyspo> userDyspoMap = new HashMap<>();
        if (user != null) {
            List<UserDyspo> userDyspoList = userDyspoRepository.findByUser(user);
            for (UserDyspo ud : userDyspoList) {
                if (ud.getDyspo() != null) {
                    userDyspoMap.put(ud.getDyspo().getId(), ud);
                }
            }
        }
        model.addAttribute("userDyspoMap", userDyspoMap);

        return "dyspozycja";
    }

    @PostMapping("/dyspo/add")
    public String addUserToDyspo(@RequestParam Long dyspoId, Authentication authentication) {
        Optional<Dyspo> optionalDyspo = dyspoRepository.findById(dyspoId);
        if (optionalDyspo.isPresent()) {
            Dyspo dyspo = optionalDyspo.get();
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (userDyspoRepository.findByDyspoAndUser(dyspo, user).isEmpty()) {
                UserDyspo userDyspo = new UserDyspo();
                userDyspo.setDyspo(dyspo);
                userDyspo.setUser(user);
                userDyspoRepository.save(userDyspo);
            }
        }
        return "redirect:/dyspozycja";
    }

    @PostMapping("/dyspo/remove")
    public String removeUserFromDyspo(@RequestParam Long dyspoId, Authentication authentication) {
        Optional<Dyspo> optionalDyspo = dyspoRepository.findById(dyspoId);
        if (optionalDyspo.isPresent()) {
            Dyspo dyspo = optionalDyspo.get();
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            userDyspoRepository.findByDyspoAndUser(dyspo, user)
                    .ifPresent(userDyspoRepository::delete);
        }
        return "redirect:/dyspozycja";
    }

    @PostMapping("/dyspo/add-hour")
    public String setStartHour(@RequestParam Long dyspoId, @RequestParam String startHour, Authentication authentication) {
        Optional<Dyspo> optionalDyspo = dyspoRepository.findById(dyspoId);
        if (optionalDyspo.isPresent()) {
            Dyspo dyspo = optionalDyspo.get();
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
        }
        return "redirect:/dyspozycja";
    }

    @PostMapping("/admin/clear-dyspo")
    public String clearDyspo() {
        userDyspoRepository.deleteAll();
        dyspoRepository.deleteAll();
        return "redirect:/dyspozycja";
    }
}
