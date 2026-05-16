package com.frauddetector.controller;

import com.frauddetector.dto.response.ApiResponse;
import com.frauddetector.dto.response.DashboardMetricsDTO;
import com.frauddetector.dto.response.DailyAnalyticsDTO;
import com.frauddetector.dto.response.MonthlyAnalyticsDTO;
import com.frauddetector.dto.response.TopFlaggedUserDTO;
import com.frauddetector.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Dashboard", description = "Real-time analytics endpoints powering the command center. Requires ANALYST or ADMIN role.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('USER', 'ANALYST', 'ADMIN')")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "Get core metrics", description = "Returns total transactions, flagged count, unresolved alerts, and critical alert count.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Metrics retrieved successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsDTO>> getMetrics() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Analytics fetched", dashboardService.getMetrics()));
    }

    @Operation(summary = "Get daily analytics", description = "Returns total vs flagged transaction counts grouped by day, for the last 30 days.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Daily analytics retrieved")
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailyAnalyticsDTO>>> getDailyAnalytics() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Daily analytics fetched", dashboardService.getDailyAnalytics()));
    }

    @Operation(summary = "Get monthly analytics", description = "Returns total vs flagged transaction counts grouped by month, for the last 12 months.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Monthly analytics retrieved")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyAnalyticsDTO>>> getMonthlyAnalytics() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Monthly analytics fetched", dashboardService.getMonthlyAnalytics()));
    }

    @Operation(summary = "Get risk distribution", description = "Returns alert counts broken down by severity: LOW, MEDIUM, HIGH, CRITICAL.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Risk distribution retrieved")
    @GetMapping("/risk-distribution")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getRiskDistribution() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Risk distribution fetched", dashboardService.getRiskDistribution()));
    }

    @Operation(summary = "Get top flagged users", description = "Returns the top 5 users with the highest number of flagged transactions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Top flagged users retrieved")
    @GetMapping("/top-flagged-users")
    public ResponseEntity<ApiResponse<List<TopFlaggedUserDTO>>> getTopFlaggedUsers() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Top flagged users fetched", dashboardService.getTopFlaggedUsers()));
    }
}
