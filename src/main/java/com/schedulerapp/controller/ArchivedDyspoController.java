package com.schedulerapp.controller;

import com.schedulerapp.dto.DyspoArchiveSummary;
import com.schedulerapp.model.ArchivedDyspo;
import com.schedulerapp.repository.ArchivedDyspoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ArchivedDyspoController {

    @Autowired
    private ArchivedDyspoRepository archivedDyspoRepository;

    @GetMapping("/archiwum")
    public String getArchivedDyspoSummary(Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return "redirect:/access-denied";
        }

        List<ArchivedDyspo> archivedList = archivedDyspoRepository.findAll();

        // Grupowanie po roku i miesiącu
        Map<String, DyspoArchiveSummary> summaryMap = new HashMap<>();

        for (ArchivedDyspo dyspo : archivedList) {
            int year = dyspo.getDate().getYear();
            int month = dyspo.getDate().getMonthValue();
            String key = year + "-" + month;

            DyspoArchiveSummary summary = summaryMap.get(key);
            if (summary == null) {
                summary = new DyspoArchiveSummary(year, month, 0);
                summaryMap.put(key, summary);
            }
            summary.setCount(summary.getCount() + 1);
        }

        // Przekazujemy listę podsumowań do widoku
        model.addAttribute("archiveSummaryList", summaryMap.values());

        return "archiwum";
    }

}
