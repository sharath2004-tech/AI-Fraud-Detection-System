package com.frauddetector.config;

import com.frauddetector.entity.BlacklistedAccount;
import com.frauddetector.entity.FraudAlert;
import com.frauddetector.entity.Transaction;
import com.frauddetector.entity.User;
import com.frauddetector.repository.BlacklistedAccountRepository;
import com.frauddetector.repository.FraudAlertRepository;
import com.frauddetector.repository.TransactionRepository;
import com.frauddetector.repository.UserRepository;
import com.frauddetector.service.FraudContext;
import com.frauddetector.service.FraudScoringService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Order(1)
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlacklistedAccountRepository blacklistedAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FraudScoringService fraudScoringService;

    private final Random random = new Random();

    private final String[] locations = {"Mumbai", "Delhi", "Bangalore", "Chennai", "Hyderabad", "Pune", "Kolkata", "Ahmedabad"};
    private final String[] transactionTypes = {"TRANSFER", "DEBIT", "CREDIT"};
    private final String[] currencies = {"INR"};

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            logger.info("Database already seeded. Skipping.");
            return;
        }

        logger.info("Seeding realistic data...");

        // 1. Seed Users
        List<User> users = seedUsers();

        // 2. Seed Blacklist
        seedBlacklist();

        // 3. Seed Transactions & Alerts
        seedTransactions(users);

        logger.info("Database seeding completed successfully.");
    }

    private List<User> seedUsers() {
        List<User> userList = new ArrayList<>();

        // Admin
        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail("admin@fraudmonitor.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
        userList.add(admin);

        // Analysts
        for (int i = 1; i <= 3; i++) {
            User analyst = new User();
            analyst.setName("Security Analyst " + i);
            analyst.setEmail("analyst" + i + "@fraudmonitor.com");
            analyst.setPassword(passwordEncoder.encode("Analyst@123"));
            analyst.setRole(User.Role.ANALYST);
            userRepository.save(analyst);
            userList.add(analyst);
        }

        // Regular Users
        String[] names = {"Amit Patel", "Priya Sharma", "Rahul Verma", "Sneha Iyer", "Vikram Singh", 
                          "Neha Gupta", "Rohan Desai", "Kavita Reddy", "Arjun Kapoor", "Pooja Joshi"};
        
        List<User> regularUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setName(names[i]);
            user.setEmail(names[i].toLowerCase().replace(" ", ".") + "@example.com");
            user.setPassword(passwordEncoder.encode("User@123"));
            user.setRole(User.Role.USER);
            userRepository.save(user);
            regularUsers.add(user);
        }

        return regularUsers; // We'll only generate transactions for regular users
    }

    private void seedBlacklist() {
        String[] reasons = {
            "Known money laundering account",
            "Multiple fraud reports from partner banks",
            "Terrorist financing watch list",
            "History of chargeback fraud",
            "Compromised account flagged by cyber cell"
        };

        for (int i = 1; i <= 5; i++) {
            BlacklistedAccount ba = new BlacklistedAccount();
            ba.setAccountNumber("999900000" + i);
            ba.setReason(reasons[i-1]);
            blacklistedAccountRepository.save(ba);
        }
    }

    private void seedTransactions(List<User> users) {
        // Pool of device IDs per user mappings
        Map<Long, List<String>> userDevices = new HashMap<>();
        for (User u : users) {
            List<String> devices = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                devices.add(UUID.randomUUID().toString());
            }
            userDevices.put(u.getId(), devices);
        }

        // Generate 300 normal, 150 suspicious, 50 critical
        for (int i = 0; i < 500; i++) {
            User user = users.get(random.nextInt(users.size()));
            Transaction tx = new Transaction();
            tx.setUser(user);
            tx.setTransactionType(Transaction.TransactionType.valueOf(transactionTypes[random.nextInt(transactionTypes.length)]));
            tx.setCurrency("INR");
            tx.setSenderAccount(generateAccount());
            tx.setLocation(locations[random.nextInt(locations.length)]);
            tx.setIpAddress("192.168.1." + random.nextInt(255));
            
            LocalDateTime timestamp = LocalDateTime.now().minusDays(random.nextInt(60))
                                                       .minusHours(random.nextInt(23))
                                                       .minusMinutes(random.nextInt(59));
            
            if (i < 300) {
                // NORMAL
                tx.setAmount(BigDecimal.valueOf(500 + random.nextInt(49500))); // 500 - 50,000
                timestamp = timestamp.withHour(8 + random.nextInt(14)); // Daytime 8am - 10pm
                tx.setDeviceId(userDevices.get(user.getId()).get(random.nextInt(5))); // Known device
                tx.setInternational(false);
                tx.setReceiverAccount(generateAccount());
            } else if (i < 450) {
                // SUSPICIOUS
                tx.setAmount(BigDecimal.valueOf(50000 + random.nextInt(150000))); // 50,000 - 2,00,000
                if (random.nextBoolean()) {
                    timestamp = timestamp.withHour(random.nextInt(6)); // Night
                }
                if (random.nextDouble() > 0.7) {
                    tx.setDeviceId(UUID.randomUUID().toString()); // New device
                } else {
                    tx.setDeviceId(userDevices.get(user.getId()).get(random.nextInt(10)));
                }
                tx.setInternational(random.nextDouble() > 0.8);
                tx.setReceiverAccount(generateAccount());
            } else {
                // CRITICAL
                tx.setAmount(BigDecimal.valueOf(300000 + random.nextInt(700000))); // > 3,00,000
                timestamp = timestamp.withHour(random.nextInt(5)); // Night
                tx.setDeviceId(UUID.randomUUID().toString()); // New device
                if (random.nextBoolean()) {
                    tx.setReceiverAccount("999900000" + (1 + random.nextInt(5))); // Blacklisted
                } else {
                    tx.setReceiverAccount(generateAccount());
                }
                tx.setInternational(true);
            }

            tx.setCreatedAt(timestamp);
            
            // Build Context (simplified for seeding to avoid complex looping lookups)
            FraudContext context = new FraudContext();
            context.setNewDevice(!userDevices.get(user.getId()).contains(tx.getDeviceId()));
            context.setBlacklisted(tx.getReceiverAccount().startsWith("999900000"));
            context.setHasRecentFailedLogins(false);
            context.setRecentTransactionCount(0); // Simplify velocity for seed script

            // Calculate Score
            int score = fraudScoringService.calculateRiskScore(tx, context);

            // Determine Status
            FraudAlert.Severity severity;
            Transaction.TransactionStatus status = Transaction.TransactionStatus.COMPLETED;
            boolean escalated = false;

            if (score >= 81) {
                severity = FraudAlert.Severity.CRITICAL;
                status = Transaction.TransactionStatus.FLAGGED;
                escalated = true;
            } else if (score >= 51) {
                severity = FraudAlert.Severity.HIGH;
                status = Transaction.TransactionStatus.FLAGGED;
            } else if (score >= 21) {
                severity = FraudAlert.Severity.MEDIUM;
            } else {
                severity = FraudAlert.Severity.LOW;
            }

            tx.setStatus(status);
            tx = transactionRepository.save(tx);

            if (score >= 21) {
                FraudAlert alert = new FraudAlert();
                alert.setTransaction(tx);
                alert.setRiskScore(score);
                alert.setSeverity(severity);
                alert.setEscalated(escalated);
                alert.setCreatedAt(timestamp);
                fraudAlertRepository.save(alert);
            }
        }
    }

    private String generateAccount() {
        return String.format("%010d", random.nextInt(1000000000));
    }
}
