package com.rewards360.service;

import com.rewards360.model.AuditLog;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuditLogService {
    List<AuditLog> list(String userId, String action, String entityType, Instant from, Instant to);
    Optional<AuditLog> get(Long id);
    AuditLog create(AuditLog log);
    AuditLog recordSimple(String userId, String username, String action, String entityType, String entityId, String details);
}
