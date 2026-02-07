package com.rewards360.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rewards360.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByDateDesc(Long userId);
}
