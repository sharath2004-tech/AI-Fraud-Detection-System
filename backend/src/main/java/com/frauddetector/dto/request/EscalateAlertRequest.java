package com.frauddetector.dto.request;

import jakarta.validation.constraints.Size;

public class EscalateAlertRequest {

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;

    // Getters and Setters

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}