package com.tracker.subscriptionvaluetracker.domain.subscription;

import com.tracker.subscriptionvaluetracker.common.EmojiMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UsageLogRepository usageLogRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                                UsageLogRepository usageLogRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.usageLogRepository = usageLogRepository;
    }

    public List<Subscription> getActiveSubscriptions(String userUuid) {
        // 현재 구독중인 것만 반환 (종료일이 없거나 아직 지나지 않은 것)
        return subscriptionRepository.findCurrentSubscriptions(userUuid, LocalDate.now());
    }

    public List<Subscription> getAllActiveSubscriptions(String userUuid) {
        // 종료일과 관계없이 isActive=true인 모든 구독 반환 (관리 목적)
        return subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(userUuid);
    }

    public Optional<Subscription> getSubscription(Long id, String userUuid) {
        return subscriptionRepository.findByIdAndUserUuid(id, userUuid);
    }

    public boolean isDuplicateName(String userUuid, String name) {
        return subscriptionRepository.existsByUserUuidAndNameAndIsActiveTrue(userUuid, name);
    }

    public boolean isDuplicateNameForUpdate(String userUuid, String name, Long excludeId) {
        return subscriptionRepository.existsByUserUuidAndNameAndIdNotAndIsActiveTrue(userUuid, name, excludeId);
    }

    @Transactional
    public Subscription createSubscription(String userUuid, SubscriptionForm form) {
        if (isDuplicateName(userUuid, form.getName())) {
            throw new IllegalArgumentException("이미 등록된 구독 이름입니다: " + form.getName());
        }

        Subscription subscription = new Subscription(
                userUuid,
                form.getName(),
                form.getEmojiCode(),
                form.getPeriodType(),
                form.getTotalAmount(),
                form.getMonthlyAmount(),
                form.getStartDate()
        );
        if (form.getEndDate() != null) {
            subscription.setEndDate(form.getEndDate());
        }
        if (form.getMonthlyTargetUsage() != null && form.getMonthlyTargetUsage() > 0) {
            subscription.setMonthlyTargetUsage(form.getMonthlyTargetUsage());
        }
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription updateSubscription(Long id, String userUuid, SubscriptionForm form) {
        Subscription subscription = subscriptionRepository.findByIdAndUserUuid(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        if (isDuplicateNameForUpdate(userUuid, form.getName(), id)) {
            throw new IllegalArgumentException("이미 등록된 구독 이름입니다: " + form.getName());
        }

        subscription.setName(form.getName());
        subscription.setEmojiCode(form.getEmojiCode());
        subscription.setPeriodType(form.getPeriodType());
        subscription.setTotalAmount(form.getTotalAmount());
        subscription.setMonthlyAmount(form.getMonthlyAmount());
        subscription.setStartDate(form.getStartDate());
        subscription.setEndDate(form.getEndDate());
        subscription.setMonthlyTargetUsage(
                form.getMonthlyTargetUsage() != null && form.getMonthlyTargetUsage() > 0
                        ? form.getMonthlyTargetUsage() : null
        );

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void deleteSubscription(Long id, String userUuid) {
        Subscription subscription = subscriptionRepository.findByIdAndUserUuid(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));
        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public UsageLog checkIn(Long subscriptionId, String userUuid) {
        return checkInOnDate(subscriptionId, userUuid, LocalDate.now());
    }

    @Transactional
    public UsageLog checkInOnDate(Long subscriptionId, String userUuid, LocalDate date) {
        subscriptionRepository.findByIdAndUserUuid(subscriptionId, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        // 이미 해당 날짜에 출석했는지 확인
        if (usageLogRepository.existsBySubscriptionIdAndUsedAt(subscriptionId, date)) {
            throw new IllegalStateException("해당 날짜에 이미 출석했습니다.");
        }

        UsageLog usageLog = new UsageLog(subscriptionId, date);
        return usageLogRepository.save(usageLog);
    }

    @Transactional
    public boolean toggleCheckIn(Long subscriptionId, String userUuid, LocalDate date) {
        subscriptionRepository.findByIdAndUserUuid(subscriptionId, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        Optional<UsageLog> existingLog = usageLogRepository.findBySubscriptionIdAndUsedAt(subscriptionId, date);

        if (existingLog.isPresent()) {
            // 이미 출석했으면 취소
            usageLogRepository.delete(existingLog.get());
            return false; // 출석 취소됨
        } else {
            // 출석 안했으면 출석
            usageLogRepository.save(new UsageLog(subscriptionId, date));
            return true; // 출석 완료
        }
    }

    @Transactional
    public void cancelCheckIn(Long usageLogId, String userUuid) {
        UsageLog usageLog = usageLogRepository.findById(usageLogId)
                .orElseThrow(() -> new IllegalArgumentException("출석 기록을 찾을 수 없습니다."));

        // 해당 구독이 사용자의 것인지 확인
        subscriptionRepository.findByIdAndUserUuid(usageLog.getSubscriptionId(), userUuid)
                .orElseThrow(() -> new IllegalArgumentException("권한이 없습니다."));

        usageLogRepository.delete(usageLog);
    }

    public int getMonthlyUsageCount(Long subscriptionId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
        return (int) usageLogRepository.countBySubscriptionIdAndUsedAtBetween(subscriptionId, startOfMonth, endOfMonth);
    }

    /**
     * 구독의 총 사용 횟수 (구독 시작일부터 현재까지)
     */
    public int getTotalUsageCount(Long subscriptionId) {
        return (int) usageLogRepository.countBySubscriptionId(subscriptionId);
    }

    /**
     * 월별 회당 비용 계산: 월 환산 금액 / 이번 달 사용 횟수
     */
    public BigDecimal calculateMonthlyDailyCost(Subscription subscription) {
        int monthlyUsageCount = getMonthlyUsageCount(subscription.getId());
        if (monthlyUsageCount == 0) {
            return subscription.getMonthlyAmount();
        }

        // 월 환산 금액 기준으로 회당 비용 계산
        BigDecimal monthlyAmount = subscription.getMonthlyAmount();

        // 회당 비용 = 월 환산 금액 / 이번 달 사용 횟수
        return monthlyAmount.divide(BigDecimal.valueOf(monthlyUsageCount), 0, RoundingMode.HALF_UP);
    }

    /**
     * 월별 가성비 레벨 계산 (월 환산 금액 기준)
     * - 20회 이상 사용 시 good (회당 비용 <= 월금액/20)
     * - 10~20회 사용 시 normal (회당 비용 <= 월금액/10)
     * - 10회 미만 사용 시 warning
     */
    public String getMonthlyDailyCostLevel(BigDecimal dailyCost, BigDecimal monthlyAmount) {
        BigDecimal goodThreshold = monthlyAmount.divide(BigDecimal.valueOf(20), 0, RoundingMode.HALF_UP);
        BigDecimal normalThreshold = monthlyAmount.divide(BigDecimal.valueOf(10), 0, RoundingMode.HALF_UP);

        if (dailyCost.compareTo(goodThreshold) <= 0) {
            return "good";
        } else if (dailyCost.compareTo(normalThreshold) <= 0) {
            return "normal";
        } else {
            return "warning";
        }
    }

    public boolean isCheckedInToday(Long subscriptionId) {
        return usageLogRepository.existsBySubscriptionIdAndUsedAt(subscriptionId, LocalDate.now());
    }

    public long getActiveSubscriptionCount(String userUuid) {
        return subscriptionRepository.countByUserUuidAndIsActiveTrue(userUuid);
    }

    public SubscriptionViewDto toViewDto(Subscription subscription) {
        // 이번 달 사용 횟수 기준으로 계산
        int monthlyUsageCount = getMonthlyUsageCount(subscription.getId());
        BigDecimal dailyCost = calculateMonthlyDailyCost(subscription);
        String dailyCostLevel = getMonthlyDailyCostLevel(dailyCost, subscription.getMonthlyAmount());
        boolean checkedInToday = isCheckedInToday(subscription.getId());
        String emoji = EmojiMapper.toEmoji(subscription.getEmojiCode());

        return new SubscriptionViewDto(
                subscription.getId(),
                subscription.getName(),
                subscription.getEmojiCode(),
                emoji,
                subscription.getPeriodType(),
                subscription.getTotalAmount(),
                subscription.getMonthlyAmount(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                monthlyUsageCount,  // 이번 달 사용 횟수
                dailyCost,
                dailyCostLevel,
                checkedInToday
        );
    }

    public List<SubscriptionViewDto> getSubscriptionsWithStats(String userUuid) {
        return getActiveSubscriptions(userUuid).stream()
                .map(this::toViewDto)
                .toList();
    }

    public List<UsageLog> getRecentUsageLogs(Long subscriptionId) {
        return usageLogRepository.findTop10BySubscriptionIdOrderByUsedAtDesc(subscriptionId);
    }
}
