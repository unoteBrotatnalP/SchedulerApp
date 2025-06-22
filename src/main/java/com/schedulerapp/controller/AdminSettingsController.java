package com.schedulerapp.controller;

import com.schedulerapp.model.ShiftChange;
import com.schedulerapp.model.ShiftHours;
import com.schedulerapp.model.ShiftType;
import com.schedulerapp.repository.ShiftHoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/adminsettings")
public class AdminSettingsController {

    @Autowired
    private ShiftHoursRepository shiftHoursRepository;

    @GetMapping
    public String getShiftHours(Model model) {
        model.addAttribute("weekdayShifts", shiftHoursRepository.findByType(ShiftType.WEEKDAY));
        model.addAttribute("weekendShifts", shiftHoursRepository.findByType(ShiftType.WEEKEND));
        Map<String, String> shiftChangeOptions = Map.of(
                "NONE", "Brak",
                "OPENING", "Otwarcie",
                "CLOSING", "Zamknięcie"
        );
        model.addAttribute("shiftChangeOptions", shiftChangeOptions);
        return "adminsettings";
    }

    @PostMapping("/add")
    public String addShift(@RequestParam String hour, @RequestParam String type, @RequestParam String shiftChange) {
        ShiftHours shift = new ShiftHours();
        shift.setHour(hour);
        shift.setType(ShiftType.valueOf(type.toUpperCase()));
        shift.setShiftChange(ShiftChange.valueOf(shiftChange.toUpperCase()));
        shiftHoursRepository.save(shift);
        return "redirect:/adminsettings";
    }

    @PostMapping("/delete")
    public String deleteShift(@RequestParam Long id) {
        shiftHoursRepository.deleteById(id);
        return "redirect:/adminsettings";
    }

    @PostMapping("/edit")
    public String editShift(@RequestParam Long id, @RequestParam String hour, @RequestParam String shiftChange) {
        ShiftHours shift = shiftHoursRepository.findById(id).orElseThrow();
        shift.setHour(hour);
        shift.setShiftChange(ShiftChange.valueOf(shiftChange.toUpperCase()));
        shiftHoursRepository.save(shift);
        return "redirect:/adminsettings";
    }
}
