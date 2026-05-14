package com.frauddetector.service;

import com.frauddetector.dto.request.AddBlacklistRequest;
import com.frauddetector.dto.request.UpdateUserRoleRequest;
import com.frauddetector.dto.request.UpdateUserStatusRequest;
import com.frauddetector.dto.response.UserResponse;
import com.frauddetector.entity.AuditLog;
import com.frauddetector.entity.BlacklistedAccount;
import com.frauddetector.entity.User;
import com.frauddetector.exception.ResourceNotFoundException;
import com.frauddetector.repository.AuditLogRepository;
import com.frauddetector.repository.BlacklistedAccountRepository;
import com.frauddetector.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private BlacklistedAccountRepository blacklistedAccountRepository;

    @Autowired
    private AuditLogService auditLogService;

    public Page<UserResponse> getAllUsers(Pageable pageable, String role, String status) {
        return userRepository.getFilteredUsers(role, status, pageable).map(UserResponse::new);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserResponse(user);
    }

    // FIX 7: @PreAuthorize removed — AdminController already has @PreAuthorize("hasRole('ADMIN')") at class level
    @Transactional
    public UserResponse updateUserRole(Long id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(request.getRole());
        user = userRepository.save(user);

        // Audit Log
        auditLogService.log(null, "ROLE_CHANGED", "User", id, "System");

        return new UserResponse(user);
    }

    // FIX 7: @PreAuthorize removed — AdminController already has @PreAuthorize("hasRole('ADMIN')") at class level
    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(request.getStatus());
        user = userRepository.save(user);

        // Audit Log
        auditLogService.log(null, "STATUS_CHANGED", "User", id, "System");

        return new UserResponse(user);
    }

    public Page<AuditLog> getAuditLogs(Pageable pageable, Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.getFilteredLogs(userId, startDate, endDate, pageable);
    }

    // FIX 7: @PreAuthorize removed — AdminController already has @PreAuthorize("hasRole('ADMIN')") at class level
    @Transactional
    public BlacklistedAccount addToBlacklist(AddBlacklistRequest request) {
        BlacklistedAccount account = new BlacklistedAccount();
        account.setAccountNumber(request.getAccountNumber());
        account.setReason(request.getReason());
        account = blacklistedAccountRepository.save(account);

        // Audit Log
        auditLogService.log(null, "ACCOUNT_BLACKLISTED", "BlacklistedAccount", account.getId(), "System");

        return account;
    }

    // FIX 7: @PreAuthorize removed — AdminController already has @PreAuthorize("hasRole('ADMIN')") at class level
    @Transactional
    public void removeFromBlacklist(Long id) {
        BlacklistedAccount account = blacklistedAccountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Blacklisted account not found"));
        blacklistedAccountRepository.delete(account);

        // Audit Log
        auditLogService.log(null, "BLACKLIST_REMOVED", "BlacklistedAccount", id, "System");
    }

    public Page<BlacklistedAccount> getBlacklist(Pageable pageable) {
        return blacklistedAccountRepository.findAll(pageable);
    }
}
