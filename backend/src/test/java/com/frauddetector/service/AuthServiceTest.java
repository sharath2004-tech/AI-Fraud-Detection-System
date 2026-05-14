package com.frauddetector.service;

import com.frauddetector.dto.request.LoginRequest;
import com.frauddetector.dto.request.LogoutRequest;
import com.frauddetector.dto.request.RefreshTokenRequest;
import com.frauddetector.dto.request.RegisterRequest;
import com.frauddetector.entity.RefreshToken;
import com.frauddetector.entity.User;
import com.frauddetector.exception.DuplicateResourceException;
import com.frauddetector.exception.InvalidCredentialsException;
import com.frauddetector.repository.RefreshTokenRepository;
import com.frauddetector.repository.UserRepository;
import com.frauddetector.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void setMaxAttempts(int value) {
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", value);
    }

    private User activeUser(String email) {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("encoded-pass");
        user.setRole(User.Role.USER);
        user.setStatus(User.Status.ACTIVE);
        user.setFailedLoginAttempts(0);
        return user;
    }

    private RegisterRequest registerRequest(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail(email);
        req.setPassword("Password@123");
        req.setRole(User.Role.USER);
        return req;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    // ─── Register ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register(): new email saves user and returns it")
    void testRegister_success() {
        String email = "new@example.com";
        RegisterRequest req = registerRequest(email);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.register(req);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals(email, result.getEmail());
        assertEquals("encoded-pass", result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register(): duplicate email throws DuplicateResourceException")
    void testRegister_duplicateEmail_throwsException() {
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
            () -> authService.register(registerRequest(email)),
            "Should throw when email already exists");

        verify(userRepository, never()).save(any());
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login(): valid credentials resets failedLoginAttempts to 0")
    void testLogin_success_resetsFailedAttempts() {
        User user = activeUser("user@example.com");
        user.setFailedLoginAttempts(2); // Had some prior failures

        when(authenticationManager.authenticate(any())).thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.login(loginRequest("user@example.com", "Password@123"));

        assertNotNull(result);
        assertEquals(0, result.getFailedLoginAttempts(), "Successful login must reset failed attempts counter");
        assertNull(result.getLockedUntil(), "Successful login must clear the lock");
        verify(auditLogService).log(any(), eq("USER_LOGIN"), eq("User"), any(), any());
    }

    @Test
    @DisplayName("login(): wrong password increments failedLoginAttempts by 1")
    void testLogin_wrongPassword_incrementsFailedAttempts() {
        setMaxAttempts(5);
        User user = activeUser("user@example.com");
        user.setFailedLoginAttempts(1);

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(InvalidCredentialsException.class,
            () -> authService.login(loginRequest("user@example.com", "wrong-pass")));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getFailedLoginAttempts(), "Failed attempts should increment to 2");
        assertNotEquals(User.Status.LOCKED, captor.getValue().getStatus(), "Should not lock before reaching max attempts");
    }

    @Test
    @DisplayName("login(): exceeding maxFailedAttempts locks the account")
    void testLogin_exceededAttempts_locksAccount() {
        setMaxAttempts(5);
        User user = activeUser("user@example.com");
        user.setFailedLoginAttempts(4); // One more failure should lock

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(InvalidCredentialsException.class,
            () -> authService.login(loginRequest("user@example.com", "wrong-pass")));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(User.Status.LOCKED, saved.getStatus(), "Account must be LOCKED after exceeding max attempts");
        assertNotNull(saved.getLockedUntil(), "lockedUntil timestamp must be set");
        assertTrue(saved.getLockedUntil().isAfter(LocalDateTime.now()), "lockedUntil must be in the future");
    }

    // ─── Refresh Token ───────────────────────────────────────────────────────

    @Test
    @DisplayName("refreshToken(): valid, non-expired token returns a new access token")
    void testRefreshToken_validToken_returnsNewAccessToken() {
        User user = activeUser("user@example.com");

        RefreshToken stored = new RefreshToken();
        stored.setToken("valid-refresh-token");
        stored.setUser(user);
        stored.setExpiryDate(LocalDateTime.now().plusDays(6));

        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("valid-refresh-token");

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(stored));
        when(jwtUtil.generateToken("user@example.com")).thenReturn("new-access-token");

        String result = authService.refreshToken(req);

        assertEquals("new-access-token", result);
        verify(jwtUtil).generateToken("user@example.com");
    }

    @Test
    @DisplayName("refreshToken(): expired token deletes it and throws InvalidCredentialsException")
    void testRefreshToken_expiredToken_throwsException() {
        User user = activeUser("user@example.com");

        RefreshToken expired = new RefreshToken();
        expired.setToken("expired-token");
        expired.setUser(user);
        expired.setExpiryDate(LocalDateTime.now().minusDays(1)); // Already expired

        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("expired-token");

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThrows(InvalidCredentialsException.class,
            () -> authService.refreshToken(req),
            "Expired refresh token should throw InvalidCredentialsException");

        verify(refreshTokenRepository).delete(expired);
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("refreshToken(): unknown token throws InvalidCredentialsException")
    void testRefreshToken_unknownToken_throwsException() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("unknown-token");

        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
            () -> authService.refreshToken(req),
            "Unknown refresh token should throw InvalidCredentialsException");
    }

    // ─── Logout ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("logout(): valid refresh token is deleted and audit log is written")
    void testLogout_validToken_deletesRefreshToken() {
        User user = activeUser("user@example.com");

        RefreshToken stored = new RefreshToken();
        stored.setToken("logout-token");
        stored.setUser(user);
        stored.setExpiryDate(LocalDateTime.now().plusDays(6));

        LogoutRequest req = new LogoutRequest();
        req.setRefreshToken("logout-token");

        when(refreshTokenRepository.findByToken("logout-token")).thenReturn(Optional.of(stored));

        authService.logout(req);

        verify(refreshTokenRepository).delete(stored);
        verify(auditLogService).log(eq(1L), eq("USER_LOGOUT"), eq("User"), eq(1L), any());
    }

    @Test
    @DisplayName("logout(): unknown token throws InvalidCredentialsException")
    void testLogout_unknownToken_throwsException() {
        LogoutRequest req = new LogoutRequest();
        req.setRefreshToken("bad-token");

        when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
            () -> authService.logout(req),
            "Logout with unknown token should throw");

        verify(refreshTokenRepository, never()).delete(any());
    }
}
