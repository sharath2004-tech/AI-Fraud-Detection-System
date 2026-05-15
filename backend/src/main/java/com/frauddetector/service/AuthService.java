package com.frauddetector.service;

import com.frauddetector.dto.request.LoginRequest;
import com.frauddetector.dto.request.RegisterRequest;
import com.frauddetector.dto.request.RefreshTokenRequest;
import com.frauddetector.dto.request.LogoutRequest;
import com.frauddetector.entity.RefreshToken;
import com.frauddetector.entity.User;
import com.frauddetector.exception.AccountLockedException;
import com.frauddetector.exception.DuplicateResourceException;
import com.frauddetector.exception.InvalidCredentialsException;
import com.frauddetector.repository.RefreshTokenRepository;
import com.frauddetector.repository.UserRepository;
import com.frauddetector.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${rate.limit.max.attempts}")
    private int maxFailedAttempts;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);

        return userRepository.save(user);
    }

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public User login(LoginRequest request) {
        // Check for account lock before attempting authentication
        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existingUser != null) {
            if (existingUser.getStatus() == User.Status.LOCKED) {
                if (existingUser.getLockedUntil() != null && existingUser.getLockedUntil().isAfter(LocalDateTime.now())) {
                    throw new AccountLockedException("Account is locked until " + existingUser.getLockedUntil());
                } else {
                    // Lock expired — auto-unlock
                    existingUser.setStatus(User.Status.ACTIVE);
                    existingUser.setFailedLoginAttempts(0);
                    existingUser.setLockedUntil(null);
                    userRepository.save(existingUser);
                }
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);

            // Audit
            auditLogService.log(user.getId(), "USER_LOGIN", "User", user.getId(), null);

            return user;
        } catch (Exception e) {
            handleFailedLogin(request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    private void handleFailedLogin(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= maxFailedAttempts) {
                user.setStatus(User.Status.LOCKED);
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            userRepository.save(user);
        }
    }

    @Transactional
    public String refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidCredentialsException("Refresh token expired");
        }

        return jwtUtil.generateToken(refreshToken.getUser().getEmail());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));
        
        // Audit
        auditLogService.log(refreshToken.getUser().getId(), "USER_LOGOUT", "User", refreshToken.getUser().getId(), null);

        refreshTokenRepository.delete(refreshToken);
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtUtil.generateRefreshToken(user.getEmail()));
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        return refreshTokenRepository.save(refreshToken);
    }
}