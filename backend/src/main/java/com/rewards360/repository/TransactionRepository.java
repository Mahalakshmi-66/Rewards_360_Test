
package com.rewards360.repository;

import com.rewards360.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Simple filters
    List<Transaction> findTop100ByOrderByCreatedAtDesc();
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(String accountId);
    List<Transaction> findByRiskLevelOrderByCreatedAtDesc(String riskLevel);
    List<Transaction> findByStatusOrderByCreatedAtDesc(String status);
    List<Transaction> findByPaymentMethodOrderByCreatedAtDesc(String paymentMethod);

    // Ranges
    List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);
    List<Transaction> findByAmountBetweenOrderByCreatedAtDesc(BigDecimal min, BigDecimal max);

    // Text search (merchantName or description)
    @Query("""
           SELECT t FROM Transaction t
           WHERE (LOWER(t.merchantName) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%')))
           ORDER BY t.createdAt DESC
           """)
    List<Transaction> searchByText(String q);

    // Combined commonly-used queries (optional)
    @Query("""
           SELECT t FROM Transaction t
           WHERE (:accountId IS NULL OR t.accountId = :accountId)
             AND (:risk IS NULL OR t.riskLevel = :risk)
             AND (:status IS NULL OR t.status = :status)
             AND (:pm IS NULL OR t.paymentMethod = :pm)
             AND (:fromTs IS NULL OR t.createdAt >= :fromTs)
             AND (:toTs IS NULL OR t.createdAt <= :toTs)
             AND (:minAmt IS NULL OR t.amount >= :minAmt)
             AND (:maxAmt IS NULL OR t.amount <= :maxAmt)
             AND (
                 :q IS NULL OR
                 LOWER(t.merchantName) LIKE LOWER(CONCAT('%', :q, '%')) OR
                 LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%'))
             )
           ORDER BY t.createdAt DESC
           """)
    List<Transaction> findFiltered(String accountId, String risk, String status, String pm,
                                   Instant fromTs, Instant toTs,
                                   BigDecimal minAmt, BigDecimal maxAmt,
                                   String q);



    List<Transaction> findTop10ByAccountIdOrderByCreatedAtDesc(String accountId);

    // Velocity: count transactions within a window
    @Query("""
       SELECT COUNT(t.id)
       FROM Transaction t
       WHERE t.accountId = :accountId
         AND t.createdAt BETWEEN :fromTs AND :toTs
       """)
    long countByAccountIdAndCreatedAtBetween(String accountId, Instant fromTs, Instant toTs);

    // For rewards system - find transactions by user
    List<Transaction> findByUserIdOrderByDateDesc(Long userId);

    // For fraud detection - find recent transactions by account
    List<Transaction> findByAccountIdAndCreatedAtAfter(String accountId, Instant after);

}

