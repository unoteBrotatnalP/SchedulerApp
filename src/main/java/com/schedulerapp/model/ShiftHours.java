package com.schedulerapp.model;

import jakarta.persistence.*;

@Entity
public class ShiftHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hour; // Przechowuje godzinę w formacie HH:mm

    @Enumerated(EnumType.STRING)
    private ShiftType type; // Wartości: WEEKDAY lub WEEKEND

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHour() { return hour; }
    public void setHour(String hour) { this.hour = hour; }

    public ShiftType getType() { return type; }
    public void setType(ShiftType type) { this.type = type; }
}
