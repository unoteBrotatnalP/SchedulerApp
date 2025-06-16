package com.schedulerapp.model;

import jakarta.persistence.*;

@Entity
public class ArchivedUserDyspoDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer shiftCount;

    @Enumerated(EnumType.STRING)
    private Preference preference;

    private Boolean completed;

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getShiftCount() { return shiftCount; }
    public void setShiftCount(Integer shiftCount) { this.shiftCount = shiftCount; }

    public Preference getPreference() { return preference; }
    public void setPreference(Preference preference) { this.preference = preference; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
