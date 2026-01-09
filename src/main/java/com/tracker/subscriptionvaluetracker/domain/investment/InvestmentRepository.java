package com.tracker.subscriptionvaluetracker.domain.investment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(String userUuid);

    Optional<Investment> findByIdAndUserUuid(Long id, String userUuid);

    long countByUserUuidAndIsActiveTrue(String userUuid);
}
