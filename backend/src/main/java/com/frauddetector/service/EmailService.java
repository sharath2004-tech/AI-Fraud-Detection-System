package com.frauddetector.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.frauddetector.entity.FraudAlert;
import com.frauddetector.entity.User;
import com.frauddetector.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.util.List;
import java.util.Arrays;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private UserRepository userRepository;

    // FIX 9: entire method body wrapped in try-catch so a missing/invalid SMTP
    // config never propagates and crashes the calling transaction.
    public void sendFraudAlertNotification(FraudAlert alert) {
        try {
            List<User.Role> targetRoles;
            if (alert.getSeverity() == FraudAlert.Severity.CRITICAL) {
                targetRoles = Arrays.asList(User.Role.ANALYST, User.Role.ADMIN);
            } else if (alert.getSeverity() == FraudAlert.Severity.HIGH) {
                targetRoles = Arrays.asList(User.Role.ANALYST);
            } else {
                return;
            }

            List<User> recipients = userRepository.findByRoleIn(targetRoles);
            if (recipients.isEmpty()) return;

            String[] toAddresses = recipients.stream().map(User::getEmail).toArray(String[]::new);

            // FIX 9: createMimeMessage() is now inside try so any MailException is caught here
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toAddresses);
            helper.setSubject(String.format("[FRAUD ALERT - %s] Transaction #%d flagged",
                    alert.getSeverity(), alert.getTransaction().getId()));

            String htmlBody = String.format(
                "<h3>Security Alert: Suspicious Transaction Detected</h3>" +
                "<p><strong>Transaction ID:</strong> #%d</p>" +
                "<p><strong>Amount:</strong> %s %s</p>" +
                "<p><strong>Severity:</strong> %s</p>" +
                "<p><strong>Risk Score:</strong> %d</p>" +
                "<p><strong>Timestamp:</strong> %s</p>" +
                "<br>" +
                "<p>Please log in to the dashboard to investigate immediately.</p>",
                alert.getTransaction().getId(),
                alert.getTransaction().getAmount(),
                alert.getTransaction().getCurrency(),
                alert.getSeverity(),
                alert.getRiskScore(),
                alert.getCreatedAt()
            );

            helper.setText(htmlBody, true);
            emailSender.send(message);
            logger.info("Sent fraud alert email notification to {} recipients", toAddresses.length);

        } catch (Exception e) {
            // Warn but never let email failure bubble up and break the transaction
            logger.warn("Failed to send fraud alert email (SMTP may not be configured): {}", e.getMessage());
        }
    }
}
