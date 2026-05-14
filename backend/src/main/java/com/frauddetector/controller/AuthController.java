package com.frauddetector.controller;

import com.frauddetector.dto.request.LoginRequest;
import com.frauddetector.dto.request.LogoutRequest;
import com.frauddetector.dto.request.RefreshTokenRequest;
import com.frauddetector.dto.request.RegisterRequest;
import com.frauddetector.dto.response.ApiResponse;
import com.frauddetector.dto.response.AuthResponse;
import com.frauddetector.dto.response.UserResponse;
import com.frauddetector.entity.RefreshToken;
import com.frauddetector.entity.User;
import com.frauddetector.security.JwtUtil;
import com.frauddetector.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Registration, login, token refresh, and logout operations")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "Register new user", description = "Creates a new USER-role account. Analysts and Admins are provisioned by administrators.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or email already exists")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "User registered successfully", new UserResponse(user)));
    }

    @Operation(summary = "Login", description = "Authenticates with email and password. Returns JWT access token and refresh token.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "Account is locked after too many failed attempts")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        String accessToken = jwtUtil.generateToken(user.getEmail());
        RefreshToken refreshToken = authService.createRefreshToken(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken.getToken());
        authResponse.setExpiresIn(900000L);

        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", authResponse));
    }

    @Operation(summary = "Refresh access token", description = "Exchanges a valid refresh token for a new access token.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String newAccessToken = authService.refreshToken(request);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(newAccessToken);
        authResponse.setExpiresIn(900000L);

        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed successfully", authResponse));
    }

    @Operation(summary = "Logout", description = "Invalidates the provided refresh token.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid refresh token")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out successfully", null));
    }
}