package com.tracker.subscriptionvaluetracker.domain.subscription;

import com.tracker.subscriptionvaluetracker.common.EmojiMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CalendarService {

    private final SubscriptionRepository subscriptionRepository;
    private final UsageLogRepository usageLogRepository;

    public CalendarService(SubscriptionRepository subscriptionRepository,
                          UsageLogRepository usageLogRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.usageLogRepository = usageLogRepository;
    }

    public List<CalendarDayDto> getCalendarDays(String userUuid, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
        LocalDate today = LocalDate.now();

        // 달력 시작일 (해당 월 1일이 속한 주의 일요일)
        LocalDate calendarStart = firstDayOfMonth.with(DayOfWeek.SUNDAY);
        if (calendarStart.isAfter(firstDayOfMonth)) {
            calendarStart = calendarStart.minusWeeks(1);
        }

        // 달력 종료일 (해당 월 마지막일이 속한 주의 토요일)
        LocalDate calendarEnd = lastDayOfMonth.with(DayOfWeek.SATURDAY);

        // 사용자의 활성 구독 목록 (종료일이 지나지 않은 현재 구독만)
        List<Subscription> subscriptions = subscriptionRepository
                .findCurrentSubscriptions(userUuid, today);

        if (subscriptions.isEmpty()) {
            return buildEmptyCalendar(calendarStart, calendarEnd, yearMonth, today);
        }

        // 구독 ID 목록
        List<Long> subscriptionIds = subscriptions.stream()
                .map(Subscription::getId)
                .toList();

        // 해당 월의 출석 기록 조회 (캘린더 표시용)
        List<UsageLog> usageLogs = usageLogRepository.findBySubscriptionIdsAndDateRange(
                subscriptionIds, firstDayOfMonth, lastDayOfMonth);

        // 구독별 해당 월 사용 횟수 계산 (월별 기준)
        Map<Long, Long> monthlyUsageCountBySubscription = new HashMap<>();
        for (Long subId : subscriptionIds) {
            long monthlyCount = usageLogRepository.countBySubscriptionIdAndUsedAtBetween(
                    subId, firstDayOfMonth, lastDayOfMonth);
            monthlyUsageCountBySubscription.put(subId, monthlyCount);
        }

        // 구독 ID -> 구독 정보 맵
        Map<Long, Subscription> subscriptionMap = subscriptions.stream()
                .collect(Collectors.toMap(Subscription::getId, s -> s));

        // 날짜별 출석 기록 그룹화
        Map<LocalDate, List<UsageLog>> usagesByDate = usageLogs.stream()
                .collect(Collectors.groupingBy(UsageLog::getUsedAt));

        // 달력 일자 생성
        List<CalendarDayDto> calendarDays = new ArrayList<>();
        LocalDate current = calendarStart;

        while (!current.isAfter(calendarEnd)) {
            boolean isCurrentMonth = current.getMonth() == yearMonth.getMonth();
            boolean isToday = current.equals(today);

            List<CalendarDayDto.UsageEntry> usageEntries = new ArrayList<>();

            if (isCurrentMonth) {
                List<UsageLog> dayUsages = usagesByDate.getOrDefault(current, Collections.emptyList());

                for (UsageLog usage : dayUsages) {
                    Subscription sub = subscriptionMap.get(usage.getSubscriptionId());
                    if (sub != null) {
                        long monthlyUsageCount = monthlyUsageCountBySubscription.getOrDefault(sub.getId(), 1L);
                        BigDecimal dailyCost = calculateMonthlyCostPerUse(sub, monthlyUsageCount);
                        String costLevel = getMonthlyCostLevel(dailyCost, sub.getMonthlyAmount());
                        String emoji = EmojiMapper.toEmoji(sub.getEmojiCode());

                        usageEntries.add(new CalendarDayDto.UsageEntry(
                                sub.getId(),
                                sub.getName(),
                                emoji,
                                dailyCost,
                                costLevel
                        ));
                    }
                }
            }

            calendarDays.add(new CalendarDayDto(
                    current,
                    current.getDayOfMonth(),
                    isCurrentMonth,
                    isToday,
                    usageEntries
            ));

            current = current.plusDays(1);
        }

        return calendarDays;
    }

    private List<CalendarDayDto> buildEmptyCalendar(LocalDate start, LocalDate end,
                                                     YearMonth yearMonth, LocalDate today) {
        List<CalendarDayDto> days = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            days.add(new CalendarDayDto(
                    current,
                    current.getDayOfMonth(),
                    current.getMonth() == yearMonth.getMonth(),
                    current.equals(today),
                    Collections.emptyList()
            ));
            current = current.plusDays(1);
        }

        return days;
    }

    /**
     * 월별 회당 비용 계산: 월 환산 금액 / 해당 월 사용 횟수
     */
    private BigDecimal calculateMonthlyCostPerUse(Subscription subscription, long monthlyUsageCount) {
        if (monthlyUsageCount == 0) {
            return subscription.getMonthlyAmount();
        }

        // 월 환산 금액 기준으로 회당 비용 계산
        BigDecimal monthlyAmount = subscription.getMonthlyAmount();

        // 회당 비용 = 월 환산 금액 / 해당 월 사용 횟수
        return monthlyAmount.divide(BigDecimal.valueOf(monthlyUsageCount), 0, RoundingMode.HALF_UP);
    }

    /**
     * 월별 가성비 레벨 계산 (월 환산 금액 기준)
     * - 20회 이상 사용 시 good (회당 비용 <= 월금액/20)
     * - 10~20회 사용 시 normal (회당 비용 <= 월금액/10)
     * - 10회 미만 사용 시 warning
     */
    private String getMonthlyCostLevel(BigDecimal dailyCost, BigDecimal monthlyAmount) {
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

    public List<SubscriptionViewDto> getSubscriptionsForLegend(String userUuid) {
        return subscriptionRepository.findCurrentSubscriptions(userUuid, LocalDate.now())
                .stream()
                .map(sub -> new SubscriptionViewDto(
                        sub.getId(),
                        sub.getName(),
                        sub.getEmojiCode(),
                        EmojiMapper.toEmoji(sub.getEmojiCode()),
                        sub.getPeriodType(),
                        sub.getTotalAmount(),
                        sub.getMonthlyAmount(),
                        sub.getStartDate(),
                        sub.getEndDate(),
                        0, BigDecimal.ZERO, "normal", false
                ))
                .toList();
    }

    /**
     * 각 구독의 진행률 정보를 계산
     */
    public List<SubscriptionProgressDto> getSubscriptionProgress(String userUuid) {
        LocalDate today = LocalDate.now();
        List<Subscription> subscriptions = subscriptionRepository.findCurrentSubscriptions(userUuid, today);

        return subscriptions.stream()
                .map(sub -> calculateProgress(sub, today))
                .toList();
    }

    private SubscriptionProgressDto calculateProgress(Subscription subscription, LocalDate today) {
        LocalDate startDate = subscription.getStartDate();
        LocalDate endDate = subscription.getEndDate();

        // 종료일이 없으면 시작일 + 1년으로 가정
        if (endDate == null) {
            endDate = startDate.plusYears(1);
        }

        // 전체 기간 (월)
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        int totalMonths = (int) Math.max(1, java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate));

        // 경과 기간
        long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, today);
        if (elapsedDays < 0) elapsedDays = 0;
        if (elapsedDays > totalDays) elapsedDays = totalDays;

        int elapsedMonths = (int) java.time.temporal.ChronoUnit.MONTHS.between(startDate, today);
        if (elapsedMonths < 0) elapsedMonths = 0;
        if (elapsedMonths > totalMonths) elapsedMonths = totalMonths;

        // 기간 진행률 (%)
        int periodProgress = totalDays > 0 ? (int) (elapsedDays * 100 / totalDays) : 0;
        if (periodProgress > 100) periodProgress = 100;

        // 월 목표 사용 횟수
        int monthlyTarget = subscription.getCalculatedMonthlyTarget();

        // 목표 총 사용 횟수
        int targetTotalUsage = monthlyTarget * totalMonths;

        // 현재 총 사용 횟수
        int currentTotalUsage = (int) usageLogRepository.countBySubscriptionId(subscription.getId());

        // 사용 진행률 (%)
        int usageProgress = targetTotalUsage > 0 ? (int) (currentTotalUsage * 100 / targetTotalUsage) : 0;
        if (usageProgress > 100) usageProgress = 100;

        // 상태 결정 (사용률이 기간 대비 낮으면 warning)
        String status;
        String statusMessage;

        if (usageProgress >= periodProgress) {
            status = "good";
            statusMessage = "잘 쓰는 중";
        } else if (usageProgress >= periodProgress - 20) {
            status = "normal";
            statusMessage = "조금 더 사용하면 좋아요";
        } else {
            status = "warning";
            statusMessage = "더 가야 본전!";
        }

        return new SubscriptionProgressDto(
                subscription.getId(),
                subscription.getName(),
                EmojiMapper.toEmoji(subscription.getEmojiCode()),
                totalMonths,
                elapsedMonths,
                periodProgress,
                targetTotalUsage,
                currentTotalUsage,
                usageProgress,
                status,
                statusMessage,
                monthlyTarget
        );
    }
}
