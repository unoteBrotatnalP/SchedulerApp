package com.schedulerapp.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class ArchivedUserDyspo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "archived_dyspo_id", nullable = false)
    private ArchivedDyspo archivedDyspo;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalTime startHour;

    // Gettery i settery

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArchivedDyspo getArchivedDyspo() {
        return archivedDyspo;
    }

    public void setArchivedDyspo(ArchivedDyspo archivedDyspo) {
        this.archivedDyspo = archivedDyspo;
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
