package com.tracker.subscriptionvaluetracker.domain.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(String userUuid);

    List<Subscription> findByUserUuidOrderByCreatedAtDesc(String userUuid);

    Optional<Subscription> findByIdAndUserUuid(Long id, String userUuid);

    long countByUserUuidAndIsActiveTrue(String userUuid);
}
