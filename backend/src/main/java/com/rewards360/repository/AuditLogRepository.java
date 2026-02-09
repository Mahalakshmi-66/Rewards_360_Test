
package com.rewards360.repository;

import com.rewards360.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Simple lists
    List<AuditLog> findTop200ByOrderByCreatedAtDesc();
    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);

    // Flexible multi-filter query (portable JPQL using function for DATE extraction)
    @Query("""
           SELECT a FROM AuditLog a
           WHERE (:userId IS NULL OR a.userId = :userId)
             AND (:username IS NULL OR LOWER(a.username) = LOWER(:username))
             AND (:action IS NULL OR a.action = :action)
             AND (:entityType IS NULL OR a.entityType = :entityType)
             AND (:entityId IS NULL OR a.entityId = :entityId)
             AND (:fromTs IS NULL OR a.createdAt >= :fromTs)
             AND (:toTs IS NULL OR a.createdAt <= :toTs)
           ORDER BY a.createdAt DESC
           """)
    List<AuditLog> findFiltered(String userId,
                                String username,
                                String action,
                                String entityType,
                                String entityId,
                                Instant fromTs,
                                Instant toTs);

    // Daily trend counts per action between range
    // Note: function('date', a.createdAt) works with Hibernate for H2/Postgres/MySQL in most cases.
    @Query("""
           SELECT function('date', a.createdAt) as day, a.action as action, COUNT(a.id) as cnt
           FROM AuditLog a
           WHERE (:fromTs IS NULL OR a.createdAt >= :fromTs)
             AND (:toTs IS NULL OR a.createdAt <= :toTs)
             AND (:action IS NULL OR a.action = :action)
           GROUP BY function('date', a.createdAt), a.action
           ORDER BY day ASC, action ASC
           """)
    List<Object[]> dailyCounts(Instant fromTs, Instant toTs, String action);

    // Totals by action in range
    @Query("""
           SELECT a.action as action, COUNT(a.id) as cnt
           FROM AuditLog a
           WHERE (:fromTs IS NULL OR a.createdAt >= :fromTs)
             AND (:toTs IS NULL OR a.createdAt <= :toTs)
           GROUP BY a.action
           ORDER BY cnt DESC
           """)
    List<Object[]> totalsByAction(Instant fromTs, Instant toTs);

    // Totals by user in range
    @Query("""
           SELECT a.username as username, COUNT(a.id) as cnt
           FROM AuditLog a
           WHERE (:fromTs IS NULL OR a.createdAt >= :fromTs)
             AND (:toTs IS NULL OR a.createdAt <= :toTs)
           GROUP BY a.username
           ORDER BY cnt DESC
           """)
    List<Object[]> totalsByUser(Instant fromTs, Instant toTs);
}
