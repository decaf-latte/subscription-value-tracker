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

        // 사용자의 활성 구독 목록
        List<Subscription> subscriptions = subscriptionRepository
                .findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(userUuid);

        if (subscriptions.isEmpty()) {
            return buildEmptyCalendar(calendarStart, calendarEnd, yearMonth, today);
        }

        // 구독 ID 목록
        List<Long> subscriptionIds = subscriptions.stream()
                .map(Subscription::getId)
                .toList();

        // 해당 월의 모든 출석 기록 조회
        List<UsageLog> usageLogs = usageLogRepository.findBySubscriptionIdsAndDateRange(
                subscriptionIds, firstDayOfMonth, lastDayOfMonth);

        // 구독별 월간 사용 횟수 계산
        Map<Long, Long> usageCountBySubscription = usageLogs.stream()
                .collect(Collectors.groupingBy(UsageLog::getSubscriptionId, Collectors.counting()));

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
                        long usageCount = usageCountBySubscription.getOrDefault(sub.getId(), 1L);
                        BigDecimal dailyCost = calculateDailyCost(sub.getMonthlyAmount(), usageCount);
                        String costLevel = getDailyCostLevel(dailyCost, sub.getMonthlyAmount());
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

    private BigDecimal calculateDailyCost(BigDecimal monthlyAmount, long usageCount) {
        if (usageCount == 0) {
            return monthlyAmount;
        }
        return monthlyAmount.divide(BigDecimal.valueOf(usageCount), 0, RoundingMode.HALF_UP);
    }

    private String getDailyCostLevel(BigDecimal dailyCost, BigDecimal monthlyAmount) {
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
        return subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(userUuid)
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
}
