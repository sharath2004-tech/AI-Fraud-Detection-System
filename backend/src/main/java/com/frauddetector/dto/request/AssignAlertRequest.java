package com.frauddetector.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignAlertRequest {

    @NotNull(message = "Analyst ID is required")
    private Long analystId;

    // Getters and Setters

    public Long getAnalystId() {
        return analystId;
    }

    public void setAnalystId(Long analystId) {
        this.analystId = analystId;
    }
}