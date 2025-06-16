package com.schedulerapp.controller;

import com.schedulerapp.dto.DyspoArchiveSummary;
import com.schedulerapp.model.ArchivedDyspo;
import com.schedulerapp.model.ArchivedUserDyspoDetails;
import com.schedulerapp.repository.ArchivedDyspoRepository;
import com.schedulerapp.repository.ArchivedUserDyspoDetailsRepository;
import com.schedulerapp.repository.ArchivedUserDyspoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ArchivedDyspoController {

    @Autowired
    private ArchivedDyspoRepository archivedDyspoRepository;

    @Autowired
    private ArchivedUserDyspoDetailsRepository archivedUserDyspoDetailsRepository;

    @Autowired
    private ArchivedUserDyspoRepository archivedUserDyspoRepository;

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

    @Transactional
    @PostMapping("/archiwum/delete")
    public String deleteArchive(@RequestParam int year, @RequestParam int month, RedirectAttributes redirectAttributes, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return "redirect:/access-denied";
        }

        // Usuń wpisy z ArchivedUserDyspoDetails powiązane z userami, których dyspozycje są z danego roku i miesiąca
        archivedUserDyspoDetailsRepository.deleteByYearAndMonth(year, month);

        // Usuń wpisy z ArchivedUserDyspo powiązane z ArchivedDyspo z danego roku i miesiąca
        archivedUserDyspoRepository.deleteByYearAndMonth(year, month);

        // Usuń wpisy z ArchivedDyspo z danego roku i miesiąca
        archivedDyspoRepository.deleteByYearAndMonth(year, month);

        redirectAttributes.addFlashAttribute("success", "Archiwum " + year + "." + String.format("%02d", month) + " zostało usunięte.");
        return "redirect:/archiwum";
    }

}
