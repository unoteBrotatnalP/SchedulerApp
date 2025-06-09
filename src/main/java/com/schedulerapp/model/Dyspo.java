package com.schedulerapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Dyspo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @OneToMany(mappedBy = "dyspo", cascade = CascadeType.ALL)
    private Set<UserDyspo> userDyspoEntries = new HashSet<>();

    // Gettery i settery

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Set<UserDyspo> getUserDyspoEntries() {
        return userDyspoEntries;
    }

    public void setUserDyspoEntries(Set<UserDyspo> userDyspoEntries) {
        this.userDyspoEntries = userDyspoEntries;
    }
}
