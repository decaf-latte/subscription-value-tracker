package com.tracker.subscriptionvaluetracker.web;

import com.tracker.subscriptionvaluetracker.domain.investment.InvestmentRepository;
import com.tracker.subscriptionvaluetracker.domain.investment.InvestmentUsageRepository;
import com.tracker.subscriptionvaluetracker.domain.subscription.Subscription;
import com.tracker.subscriptionvaluetracker.domain.subscription.SubscriptionRepository;
import com.tracker.subscriptionvaluetracker.domain.subscription.UsageLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private final SubscriptionRepository subscriptionRepository;
    private final UsageLogRepository usageLogRepository;
    private final InvestmentRepository investmentRepository;
    private final InvestmentUsageRepository investmentUsageRepository;

    public StatisticsService(SubscriptionRepository subscriptionRepository,
                             UsageLogRepository usageLogRepository,
                             InvestmentRepository investmentRepository,
                             InvestmentUsageRepository investmentUsageRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.usageLogRepository = usageLogRepository;
        this.investmentRepository = investmentRepository;
        this.investmentUsageRepository = investmentUsageRepository;
    }

    /**
     * 최근 6개월 월별 사용 횟수 (전체 합계)
     */
    public Map<String, Object> getMonthlyUsageStats(String userUuid) {
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();

        // 최근 6개월
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();

            labels.add(month.getMonthValue() + "월");

            // 해당 월의 총 사용 횟수
            List<Long> subscriptionIds = subscriptionRepository
                    .findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(userUuid)
                    .stream()
                    .map(Subscription::getId)
                    .toList();

            long totalUsage = 0;
            for (Long subId : subscriptionIds) {
                totalUsage += usageLogRepository.countBySubscriptionIdAndUsedAtBetween(subId, startOfMonth, endOfMonth);
            }
            data.add(totalUsage);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }

    /**
     * 최근 6개월 구독별 월별 사용 횟수
     */
    public Map<String, Object> getMonthlyUsageBySubscription(String userUuid) {
        List<String> labels = new ArrayList<>();
        List<Map<String, Object>> datasets = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();

        // 월 라벨 생성 (최근 6개월)
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            labels.add(month.getMonthValue() + "월");
        }

        // 구독 목록
        List<Subscription> subscriptions = subscriptionRepository
                .findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(userUuid);

        // 각 구독별 데이터셋 생성
        for (Subscription sub : subscriptions) {
            List<Long> monthlyData = new ArrayList<>();

            for (int i = 5; i >= 0; i--) {
                YearMonth month = currentMonth.minusMonths(i);
                LocalDate startOfMonth = month.atDay(1);
                LocalDate endOfMonth = month.atEndOfMonth();

                long count = usageLogRepository.countBySubscriptionIdAndUsedAtBetween(
                        sub.getId(), startOfMonth, endOfMonth);
                monthlyData.add(count);
            }

            Map<String, Object> dataset = new HashMap<>();
            dataset.put("label", sub.getName());
            dataset.put("data", monthlyData);
            datasets.add(dataset);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("datasets", datasets);
        return result;
    }

    /**
     * 구독별 월 비용 비교 (현재 구독중인 것만)
     */
    public Map<String, Object> getSubscriptionCostComparison(String userUuid) {
        LocalDate today = LocalDate.now();
        List<Subscription> subscriptions = subscriptionRepository
                .findCurrentSubscriptions(userUuid, today);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (Subscription sub : subscriptions) {
            labels.add(sub.getName());
            data.add(sub.getMonthlyAmount());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }

    /**
     * 최근 6개월 투자 절약액 추이
     */
    public Map<String, Object> getInvestmentSavingsStats(String userUuid) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();

        List<Long> investmentIds = investmentRepository
                .findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(userUuid)
                .stream()
                .map(inv -> inv.getId())
                .toList();

        // 최근 6개월
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();

            labels.add(month.getMonthValue() + "월");

            BigDecimal totalSavings = BigDecimal.ZERO;
            for (Long invId : investmentIds) {
                BigDecimal savings = investmentUsageRepository.calculateMonthlySavings(invId, startOfMonth, endOfMonth);
                if (savings != null) {
                    totalSavings = totalSavings.add(savings);
                }
            }
            data.add(totalSavings);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }

    /**
     * 총 통계 요약
     */
    public Map<String, Object> getSummaryStats(String userUuid) {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();

        // 구독 통계 (종료일이 지나지 않은 현재 구독만 포함)
        long subscriptionCount = subscriptionRepository.countCurrentSubscriptions(userUuid, today);
        BigDecimal totalMonthlyFee = subscriptionRepository
                .findCurrentSubscriptions(userUuid, today)
                .stream()
                .map(Subscription::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 투자 통계
        long investmentCount = investmentRepository.countByUserUuidAndIsActiveTrue(userUuid);

        result.put("subscriptionCount", subscriptionCount);
        result.put("totalMonthlyFee", totalMonthlyFee);
        result.put("investmentCount", investmentCount);

        return result;
    }
}
