package com.frauddetector.repository;

import com.frauddetector.entity.Transaction;
import com.frauddetector.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUser(User user, Pageable pageable);

    long countByStatus(Transaction.TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:userId IS NULL OR t.user.id = :userId) AND " +
           "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR t.amount <= :maxAmount)")
    Page<Transaction> findFilteredTransactions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") Transaction.TransactionStatus status,
            @Param("userId") Long userId,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    List<Transaction> findByUserAndCreatedAtAfter(User user, LocalDateTime since);
    List<Transaction> findByDeviceIdAndCreatedAtAfter(String deviceId, LocalDateTime since);

    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as total, SUM(CASE WHEN status = 'FLAGGED' THEN 1 ELSE 0 END) as flagged FROM transactions WHERE created_at >= :startDate GROUP BY DATE(created_at) ORDER BY date ASC", nativeQuery = true)
    List<Object[]> getDailyAnalyticsNative(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as total, SUM(CASE WHEN status = 'FLAGGED' THEN 1 ELSE 0 END) as flagged FROM transactions WHERE created_at >= :startDate GROUP BY DATE_FORMAT(created_at, '%Y-%m') ORDER BY month ASC", nativeQuery = true)
    List<Object[]> getMonthlyAnalyticsNative(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT new com.frauddetector.dto.response.TopFlaggedUserDTO(u.id, u.name, u.email, COUNT(t)) " +
           "FROM Transaction t JOIN t.user u WHERE t.status = 'FLAGGED' " +
           "GROUP BY u.id, u.name, u.email ORDER BY COUNT(t) DESC")
    List<com.frauddetector.dto.response.TopFlaggedUserDTO> getTopFlaggedUsers(Pageable pageable);
}