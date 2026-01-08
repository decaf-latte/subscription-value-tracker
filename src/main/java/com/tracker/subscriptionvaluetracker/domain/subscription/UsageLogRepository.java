package com.tracker.subscriptionvaluetracker.domain.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {

    List<UsageLog> findBySubscriptionIdOrderByUsedAtDesc(Long subscriptionId);

    List<UsageLog> findBySubscriptionIdAndUsedAtBetween(Long subscriptionId, LocalDate startDate, LocalDate endDate);

    Optional<UsageLog> findBySubscriptionIdAndUsedAt(Long subscriptionId, LocalDate usedAt);

    long countBySubscriptionIdAndUsedAtBetween(Long subscriptionId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT u FROM UsageLog u WHERE u.subscriptionId IN :subscriptionIds AND u.usedAt BETWEEN :startDate AND :endDate ORDER BY u.usedAt")
    List<UsageLog> findBySubscriptionIdsAndDateRange(
            @Param("subscriptionIds") List<Long> subscriptionIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    boolean existsBySubscriptionIdAndUsedAt(Long subscriptionId, LocalDate usedAt);
}
