package com.rewards360.service;

import com.rewards360.model.Alert;
import com.rewards360.model.Transaction;
import com.rewards360.model.TransactionAnomaly;

import java.util.List;
import java.util.Optional;

public interface AlertService {
    List<Alert> list(String status, String severity);
    Optional<Alert> get(Long id);
    Alert create(Alert alert);
    Alert escalateFromAnomaly(TransactionAnomaly anomaly, Transaction transaction);
    Optional<Alert> acknowledge(Long id, String userId, String username);
    Optional<Alert> close(Long id, String userId, String username, String resolution);
}
