package com.frauddetector.dto.request;

import com.frauddetector.entity.User;
import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    private User.Status status;

    // Getters and Setters

    public User.Status getStatus() {
        return status;
    }

    public void setStatus(User.Status status) {
        this.status = status;
    }
}