package com.schedulerapp.model;

public enum ShiftChange {
    NONE("Brak"),
    OPENING("Otwarcie"),
    CLOSING("Zamknięcie");

    private final String displayName;

    ShiftChange(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
