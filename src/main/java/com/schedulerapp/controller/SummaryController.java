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
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        model.addAttribute("isAdmin", isAdmin);

        List<User> nonAdminUsers = userRepository.findAll().stream()
                .filter(user -> !"ROLE_ADMIN".equals(user.getRole()))
                .toList();

        int totalUsers = nonAdminUsers.size();
        long usersWithCompleted = nonAdminUsers.stream()
                .filter(user -> {
                    UserDyspoDetails details = userDyspoDetailsRepository.findByUser(user).orElse(null);
                    return details != null && Boolean.TRUE.equals(details.getCompleted());
                }).count();

        double dyspoPercentage = totalUsers > 0 ? (usersWithCompleted * 100.0 / totalUsers) : 0;
        model.addAttribute("dyspoPercentage", dyspoPercentage);

        if (isAdmin) {
            model.addAttribute("users", nonAdminUsers);

            if (selectedUserId != null) {
                User selectedUser = userRepository.findById(selectedUserId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                populateUserDetails(selectedUser, model);
            }

            Map<LocalDate, Long> dailyDyspoCounts = userDyspoRepository.findAll().stream()
                    .filter(dyspo -> dyspo.getDyspo() != null && dyspo.getDyspo().getDate() != null)
                    .collect(Collectors.groupingBy(dyspo -> dyspo.getDyspo().getDate(), Collectors.counting()));

            List<LocalDate> allDates = generateCalendarDates(YearMonth.now());
            Map<LocalDate, Long> calendarDyspoCounts = allDates.stream()
                    .collect(Collectors.toMap(date -> date, date -> dailyDyspoCounts.getOrDefault(date, 0L)));

            model.addAttribute("dailyDyspoCounts", calendarDyspoCounts);
        } else {
            populateUserDetails(currentUser, model);

            Set<LocalDate> userDyspoDates = userDyspoRepository.findByUser(currentUser).stream()
                    .filter(dyspo -> dyspo.getDyspo() != null && dyspo.getDyspo().getDate() != null)
                    .map(dyspo -> dyspo.getDyspo().getDate())
                    .collect(Collectors.toSet());

            List<LocalDate> allDates = generateCalendarDates(YearMonth.now());
            List<LocalDate> userDyspoCalendar = allDates.stream()
                    .filter(userDyspoDates::contains)
                    .toList();

            model.addAttribute("userDyspoCalendar", userDyspoCalendar);
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

    private List<LocalDate> generateCalendarDates(YearMonth yearMonth) {
        return yearMonth.atDay(1).datesUntil(yearMonth.atEndOfMonth().plusDays(1)).toList();
    }
}
