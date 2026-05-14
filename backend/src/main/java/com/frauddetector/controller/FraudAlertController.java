package com.frauddetector.controller;

import com.frauddetector.dto.request.AssignAlertRequest;
import com.frauddetector.dto.request.EscalateAlertRequest;
import com.frauddetector.dto.request.ResolveAlertRequest;
import com.frauddetector.entity.FraudAlert;
import com.frauddetector.service.FraudAlertService;
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

@Tag(name = "Fraud Alerts", description = "APIs for managing and investigating fraud alerts. Accessible to ANALYST and ADMIN roles.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/fraud-alerts")
@PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
public class FraudAlertController {

    @Autowired
    private FraudAlertService fraudAlertService;

    @Operation(summary = "Get all fraud alerts", description = "Returns a paginated, filterable list of fraud alerts.")
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ANALYST or ADMIN role")
    @GetMapping
    public ResponseEntity<Page<FraudAlert>> getAllAlerts(
            Pageable pageable,
            @RequestParam(required = false) FraudAlert.AlertStatus status,
            @RequestParam(required = false) FraudAlert.Severity severity,
            @RequestParam(required = false) Long analystId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Page<FraudAlert> alerts = fraudAlertService.getAllAlerts(pageable, status, severity, analystId, startDate, endDate);
        return ResponseEntity.ok(alerts);
    }

    @Operation(summary = "Get alert by ID", description = "Returns a single fraud alert by its ID.")
    @ApiResponse(responseCode = "200", description = "Alert retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Alert not found")
    @GetMapping("/{id}")
    public ResponseEntity<FraudAlert> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(fraudAlertService.getAlertById(id));
    }

    @Operation(summary = "Assign alert to analyst", description = "Assigns the alert to a specific analyst and sets status to UNDER_REVIEW.")
    @ApiResponse(responseCode = "200", description = "Alert assigned successfully")
    @PutMapping("/{id}/assign")
    public ResponseEntity<FraudAlert> assignAlert(@PathVariable Long id, @Valid @RequestBody AssignAlertRequest request) {
        return ResponseEntity.ok(fraudAlertService.assignAlert(id, request));
    }

    @Operation(summary = "Resolve alert", description = "Marks an alert as RESOLVED with optional notes.")
    @ApiResponse(responseCode = "200", description = "Alert resolved successfully")
    @PutMapping("/{id}/resolve")
    public ResponseEntity<FraudAlert> resolveAlert(@PathVariable Long id, @Valid @RequestBody ResolveAlertRequest request) {
        return ResponseEntity.ok(fraudAlertService.resolveAlert(id, request));
    }

    @Operation(summary = "Mark as false positive", description = "Marks an alert as FALSE_POSITIVE, removing it from active investigation.")
    @ApiResponse(responseCode = "200", description = "Alert marked as false positive")
    @PutMapping("/{id}/false-positive")
    public ResponseEntity<FraudAlert> markFalsePositive(@PathVariable Long id, @Valid @RequestBody ResolveAlertRequest request) {
        return ResponseEntity.ok(fraudAlertService.markFalsePositive(id, request));
    }

    @Operation(summary = "Escalate alert", description = "Escalates a high-severity alert, flagging it for senior review.")
    @ApiResponse(responseCode = "200", description = "Alert escalated successfully")
    @PutMapping("/{id}/escalate")
    public ResponseEntity<FraudAlert> escalateAlert(@PathVariable Long id, @Valid @RequestBody EscalateAlertRequest request) {
        return ResponseEntity.ok(fraudAlertService.escalateAlert(id, request));
    }
}
