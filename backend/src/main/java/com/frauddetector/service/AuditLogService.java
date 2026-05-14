package com.frauddetector.service;

import com.frauddetector.entity.AuditLog;
import com.frauddetector.entity.User;
import com.frauddetector.repository.AuditLogRepository;
import com.frauddetector.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void log(Long userId, String action, String entityType, Long entityId, String ipAddress) {
        AuditLog auditLog = new AuditLog();
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            auditLog.setUser(user);
        }
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setIpAddress(ipAddress);
        auditLogRepository.save(auditLog);
    }
}
