package com.schedulerapp.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class UserDyspo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dyspo_id", nullable = false)
    private Dyspo dyspo;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalTime startHour; // Godzina startu przypisana do użytkownika

    // Gettery i settery

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dyspo getDyspo() {
        return dyspo;
    }

    public void setDyspo(Dyspo dyspo) {
        this.dyspo = dyspo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalTime getStartHour() {
        return startHour;
    }

    public void setStartHour(LocalTime startHour) {
        this.startHour = startHour;
    }
}
