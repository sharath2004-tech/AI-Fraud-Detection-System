package com.frauddetector.dto.request;

import jakarta.validation.constraints.Size;

public class ResolveAlertRequest {

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;

    private boolean falsePositive = false;

    // Getters and Setters

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isFalsePositive() {
        return falsePositive;
    }

    public void setFalsePositive(boolean falsePositive) {
        this.falsePositive = falsePositive;
    }
}