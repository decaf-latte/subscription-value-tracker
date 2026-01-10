package com.tracker.subscriptionvaluetracker.domain.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(String userUuid);

    List<Subscription> findByUserUuidOrderByCreatedAtDesc(String userUuid);

    Optional<Subscription> findByIdAndUserUuid(Long id, String userUuid);

    long countByUserUuidAndIsActiveTrue(String userUuid);

    boolean existsByUserUuidAndNameAndIsActiveTrue(String userUuid, String name);

    boolean existsByUserUuidAndNameAndIdNotAndIsActiveTrue(String userUuid, String name, Long id);

    // 현재 활성 상태이고, 종료일이 없거나 아직 지나지 않은 구독만 조회
    @Query("SELECT s FROM Subscription s WHERE s.userUuid = :userUuid AND s.isActive = true " +
           "AND (s.endDate IS NULL OR s.endDate >= :today) ORDER BY s.createdAt DESC")
    List<Subscription> findCurrentSubscriptions(@Param("userUuid") String userUuid, @Param("today") LocalDate today);

    // 현재 활성 상태이고, 종료일이 없거나 아직 지나지 않은 구독 수 카운트
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.userUuid = :userUuid AND s.isActive = true " +
           "AND (s.endDate IS NULL OR s.endDate >= :today)")
    long countCurrentSubscriptions(@Param("userUuid") String userUuid, @Param("today") LocalDate today);
}
