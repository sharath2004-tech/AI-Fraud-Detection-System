package com.frauddetector.controller;

import com.frauddetector.dto.request.TransactionRequest;
import com.frauddetector.entity.Transaction;
import com.frauddetector.service.TransactionService;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Transactions", description = "APIs for creating, querying, and bulk-uploading financial transactions")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Operation(summary = "Create transaction", description = "Submits a new transaction for fraud scoring. Automatically creates a FraudAlert if risk score ≥ 21.")
    @ApiResponse(responseCode = "201", description = "Transaction created and scored successfully")
    @ApiResponse(responseCode = "400", description = "Validation error or same sender/receiver account")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(request);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all transactions", description = "Returns paginated, filterable transaction history. Requires ANALYST or ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<Page<Transaction>> getAllTransactions(
            Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Transaction.TransactionStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {

        Page<Transaction> transactions = transactionService.getAllTransactions(
                pageable, startDate, endDate, status, userId, minAmount, maxAmount);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Get my transactions", description = "Returns paginated transaction history for the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Page<Transaction>> getMyTransactions(Pageable pageable) {
        return ResponseEntity.ok(transactionService.getMyTransactions(pageable));
    }

    @Operation(summary = "Get transaction by ID", description = "Returns a single transaction by its ID.")
    @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @Operation(summary = "Bulk CSV upload", description = "Processes a CSV file and creates multiple transactions. Each row is scored individually. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "CSV processed — returns count of successfully imported rows")
    @ApiResponse(responseCode = "400", description = "Empty file")
    @ApiResponse(responseCode = "500", description = "Error parsing CSV")
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadTransactionsCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a CSV file to upload.");
        }

        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream()))
                .withSkipLines(1)
                .build()) {

            List<String[]> allData = csvReader.readAll();
            int successCount = 0;

            for (String[] row : allData) {
                try {
                    if (row.length < 9) continue;

                    TransactionRequest req = new TransactionRequest();
                    req.setAmount(new BigDecimal(row[0]));
                    req.setTransactionType(Transaction.TransactionType.valueOf(row[1].toUpperCase()));
                    req.setCurrency(row[2]);
                    req.setSenderAccount(row[3]);
                    req.setReceiverAccount(row[4]);
                    req.setLocation(row[5]);
                    req.setIpAddress(row[6]);
                    req.setDeviceId(row[7]);
                    req.setInternational(Boolean.parseBoolean(row[8]));

                    transactionService.createTransaction(req);
                    successCount++;
                } catch (Exception e) {
                    // Skip malformed rows silently
                }
            }
            return ResponseEntity.ok("Successfully uploaded " + successCount + " transactions.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CSV: " + e.getMessage());
        }
    }
}
