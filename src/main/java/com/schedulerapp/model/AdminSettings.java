package com.schedulerapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_settings")
public class AdminSettings {

    @Id
    private Long id;

    private boolean dyspoLocked;

    // Gettery i Settery

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isDyspoLocked() {
        return dyspoLocked;
    }

    public void setDyspoLocked(boolean dyspoLocked) {
        this.dyspoLocked = dyspoLocked;
    }
}
