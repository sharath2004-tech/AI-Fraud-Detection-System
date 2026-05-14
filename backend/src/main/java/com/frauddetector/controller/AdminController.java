package com.frauddetector.controller;

import com.frauddetector.dto.request.AddBlacklistRequest;
import com.frauddetector.dto.request.UpdateUserRoleRequest;
import com.frauddetector.dto.request.UpdateUserStatusRequest;
import com.frauddetector.dto.response.UserResponse;
import com.frauddetector.entity.AuditLog;
import com.frauddetector.entity.BlacklistedAccount;
import com.frauddetector.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Administration", description = "System management APIs for user provisioning, audit logs, and blacklist management. ADMIN role required.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Operation(summary = "Get all users", description = "Returns paginated user list. Optionally filter by role and status.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @GetMapping("/users")
    public ResponseEntity<com.frauddetector.dto.response.ApiResponse<Page<UserResponse>>> getAllUsers(
            Pageable pageable,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        Page<UserResponse> users = adminService.getAllUsers(pageable, role, status);
        return ResponseEntity.ok(new com.frauddetector.dto.response.ApiResponse<>(true, "Users fetched successfully", users));
    }

    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/users/{id}")
    public ResponseEntity<com.frauddetector.dto.response.ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(new com.frauddetector.dto.response.ApiResponse<>(true, "User fetched successfully", adminService.getUserById(id)));
    }

    @Operation(summary = "Update user role", description = "Changes role to ADMIN, ANALYST, or USER. Triggers audit log.")
    @ApiResponse(responseCode = "200", description = "Role updated")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/users/{id}/role")
    public ResponseEntity<com.frauddetector.dto.response.ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRoleRequest request) {
        return ResponseEntity.ok(new com.frauddetector.dto.response.ApiResponse<>(true, "User role updated successfully", adminService.updateUserRole(id, request)));
    }

    @Operation(summary = "Update user status", description = "Sets user status to ACTIVE, INACTIVE, or LOCKED. Triggers audit log.")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @PutMapping("/users/{id}/status")
    public ResponseEntity<com.frauddetector.dto.response.ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserStatusRequest request) {
        return ResponseEntity.ok(new com.frauddetector.dto.response.ApiResponse<>(true, "User status updated successfully", adminService.updateUserStatus(id, request)));
    }

    @Operation(summary = "Get audit logs", description = "Returns filtered, paginated audit trail. Optionally filter by userId, startDate, and endDate.")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved")
    @GetMapping("/audit-logs")
    public ResponseEntity<com.frauddetector.dto.response.ApiResponse<Page<AuditLog>>> getAuditLogs(
            Pageable pageable,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(new com.frauddetector.dto.response.ApiResponse<>(true, "Audit logs fetched successfully", adminService.getAuditLogs(pageable, userId, startDate, endDate)));
    }

    @Operation(summary = "Get blacklist", description = "Returns paginated list of blacklisted account numbers.")
    @ApiResponse(responseCode = "200", description = "Blacklist retrieved")
    @GetMapping("/blacklist")
    public ResponseEntity<com.frauddetector.dto.response.ApiResponse<Page<BlacklistedAccount>>> getBlacklist(Pageable pageable) {
        return ResponseEntity.ok(new com.frauddetector.dto.response.ApiResponse<>(true, "Blacklist fetched successfully", adminService.getBlacklist(pageable)));
    }

    @Operation(summary = "Add to blacklist", description = "Adds an account number to the global fraud blacklist. Triggers audit log.")
    @ApiResponse(responseCode = "200", description = "Account blacklisted")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @PostMapping("/blacklist")
    public ResponseEntity<com.frauddetector.dto.response.ApiResponse<BlacklistedAccount>> addToBlacklist(
            @RequestBody @Valid AddBlacklistRequest request) {
        return ResponseEntity.ok(new com.frauddetector.dto.response.ApiResponse<>(true, "Account blacklisted successfully", adminService.addToBlacklist(request)));
    }

    @Operation(summary = "Remove from blacklist", description = "Removes an account number from the blacklist by its record ID. Triggers audit log.")
    @ApiResponse(responseCode = "200", description = "Account removed from blacklist")
    @ApiResponse(responseCode = "404", description = "Blacklist entry not found")
    @DeleteMapping("/blacklist/{id}")
    public ResponseEntity<com.frauddetector.dto.response.ApiResponse<Void>> removeFromBlacklist(@PathVariable Long id) {
        adminService.removeFromBlacklist(id);
        return ResponseEntity.ok(new com.frauddetector.dto.response.ApiResponse<>(true, "Account removed from blacklist", null));
    }
}
