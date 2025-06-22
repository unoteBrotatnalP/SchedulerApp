package com.schedulerapp.repository;

import com.schedulerapp.model.ShiftHours;
import com.schedulerapp.model.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftHoursRepository extends JpaRepository<ShiftHours, Long> {
    List<ShiftHours> findByType(ShiftType type); // Pobiera zmiany według typu
}
