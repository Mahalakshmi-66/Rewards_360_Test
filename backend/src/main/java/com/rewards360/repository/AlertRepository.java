
package com.rewards360.repository;

import com.rewards360.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatusOrderByCreatedAtDesc(String status);
    List<Alert> findBySeverityOrderByCreatedAtDesc(String severity);
    long countByStatus(String status);
    long countBySeverity(String severity);
}
