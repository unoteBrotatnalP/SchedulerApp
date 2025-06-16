package com.schedulerapp.controller;

import com.schedulerapp.model.Dyspo;
import com.schedulerapp.model.User;
import com.schedulerapp.model.UserDyspo;
import com.schedulerapp.model.UserDyspoDetails;
import com.schedulerapp.repository.DyspoRepository;
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
import java.util.LinkedHashMap;

@Controller
public class SummaryController {

    @Autowired
    private DyspoRepository dyspoRepository;

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

        int dyspoPercentage = totalUsers > 0 ? (int) Math.round(usersWithCompleted * 100.0 / totalUsers) : 0;

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

            List<LocalDate> allDates = generateCalendarDatesFromDyspo();

            Map<LocalDate, Long> calendarDyspoCounts = allDates.stream()
                    .collect(Collectors.toMap(
                            date -> date,
                            date -> dailyDyspoCounts.getOrDefault(date, 0L),
                            (e1, e2) -> e1,
                            LinkedHashMap::new  // <-- tutaj używamy LinkedHashMap, by zachować kolejność
                    ));

            model.addAttribute("dailyDyspoCounts", calendarDyspoCounts);
        } else {


            populateUserDetails(currentUser, model);

            Set<LocalDate> userDyspoDates = userDyspoRepository.findByUser(currentUser).stream()
                    .filter(dyspo -> dyspo.getDyspo() != null && dyspo.getDyspo().getDate() != null)
                    .map(dyspo -> dyspo.getDyspo().getDate())
                    .collect(Collectors.toSet());

            Map<LocalDate, Boolean> userDyspoCalendar = generateCalendarDatesFromDyspo().stream()
                    .collect(Collectors.toMap(date -> date, userDyspoDates::contains));

            model.addAttribute("userDyspoCalendar", userDyspoCalendar);
        }

        int startDayOffset = calculateStartDayOffsetFromDyspo();
        model.addAttribute("startDayOffset", startDayOffset);

        return "summary";
    }

    private void populateUserDetails(User user, Model model) {
        List<UserDyspo> dyspoList = userDyspoRepository.findByUser(user);
        UserDyspoDetails details = userDyspoDetailsRepository.findByUser(user).orElse(null);

        model.addAttribute("user", user);
        model.addAttribute("userDyspoList", dyspoList);
        model.addAttribute("userDetails", details);
    }

    private List<LocalDate> generateCalendarDatesFromDyspo() {
        // Retrieve all dates from the Dyspo table
        List<LocalDate> dyspoDates = dyspoRepository.findAll().stream()
                .map(Dyspo::getDate)
                .distinct()
                .sorted()
                .toList();

        if (dyspoDates.isEmpty()) {
            return List.of(); // Return an empty list if there are no dates
        }

        // Determine the minimum and maximum dates
        LocalDate minDate = dyspoDates.get(0);
        LocalDate maxDate = dyspoDates.get(dyspoDates.size() - 1);

        // Use the year and month of the minimum date for the calendar
        YearMonth yearMonth = YearMonth.of(minDate.getYear(), minDate.getMonth());

        // Generate calendar dates for the month and year
        return yearMonth.atDay(1).datesUntil(yearMonth.atEndOfMonth().plusDays(1)).toList();
    }

    private int calculateStartDayOffsetFromDyspo() {
        // Retrieve the earliest date from the Dyspo table
        LocalDate earliestDate = dyspoRepository.findAll().stream()
                .map(Dyspo::getDate)
                .min(LocalDate::compareTo)
                .orElse(null); // Return null if no dates exist

        if (earliestDate == null) {
            return 0; // Default offset if no dates exist
        }

        // Calculate the offset for the first day of the month of the earliest date
        LocalDate firstDayOfMonth = earliestDate.withDayOfMonth(1);
        int offset = (firstDayOfMonth.getDayOfWeek().getValue() % 7) - 1;

        return offset < 0 ? 6 : offset;
    }

    private int calculateStartDayOffset(YearMonth yearMonth) {
        LocalDate firstDay = yearMonth.atDay(1);
        return (firstDay.getDayOfWeek().getValue() % 7) - 1 < 0 ? 6 : (firstDay.getDayOfWeek().getValue() % 7) - 1;
    }

}