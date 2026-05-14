package com.frauddetector.dto.request;

import com.frauddetector.entity.User;
import jakarta.validation.constraints.NotNull;

public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private User.Role role;

    // Getters and Setters

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }
}