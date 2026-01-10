package com.tracker.subscriptionvaluetracker.web;

import com.tracker.subscriptionvaluetracker.domain.investment.Investment;
import com.tracker.subscriptionvaluetracker.domain.investment.InvestmentRepository;
import com.tracker.subscriptionvaluetracker.domain.investment.InvestmentUsageRepository;
import com.tracker.subscriptionvaluetracker.domain.subscription.Subscription;
import com.tracker.subscriptionvaluetracker.domain.subscription.SubscriptionRepository;
import com.tracker.subscriptionvaluetracker.domain.subscription.UsageLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService 테스트")
class StatisticsServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UsageLogRepository usageLogRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private InvestmentUsageRepository investmentUsageRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private static final String USER_UUID = "test-user-uuid";

    @Nested
    @DisplayName("getMonthlyUsageStats")
    class GetMonthlyUsageStats {

        @Test
        @DisplayName("최근 6개월 월별 사용 횟수를 반환한다")
        void returnsMonthlyUsageStats() {
            // given
            Subscription subscription = createSubscription(1L, "헬스장");
            when(subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(USER_UUID))
                    .thenReturn(List.of(subscription));
            when(usageLogRepository.countBySubscriptionIdAndUsedAtBetween(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(5L);

            // when
            Map<String, Object> result = statisticsService.getMonthlyUsageStats(USER_UUID);

            // then
            assertThat(result).containsKey("labels");
            assertThat(result).containsKey("data");

            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) result.get("labels");
            @SuppressWarnings("unchecked")
            List<Long> data = (List<Long>) result.get("data");

            assertThat(labels).hasSize(6);
            assertThat(data).hasSize(6);
            assertThat(data).allMatch(count -> count == 5L);
        }

        @Test
        @DisplayName("구독이 없으면 빈 데이터를 반환한다")
        void returnsEmptyDataWhenNoSubscriptions() {
            // given
            when(subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(USER_UUID))
                    .thenReturn(Collections.emptyList());

            // when
            Map<String, Object> result = statisticsService.getMonthlyUsageStats(USER_UUID);

            // then
            @SuppressWarnings("unchecked")
            List<Long> data = (List<Long>) result.get("data");
            assertThat(data).hasSize(6);
            assertThat(data).allMatch(count -> count == 0L);
        }
    }

    @Nested
    @DisplayName("getSubscriptionCostComparison")
    class GetSubscriptionCostComparison {

        @Test
        @DisplayName("구독별 월 비용을 반환한다")
        void returnsCostComparison() {
            // given
            Subscription sub1 = createSubscription(1L, "헬스장", new BigDecimal("50000"));
            Subscription sub2 = createSubscription(2L, "넷플릭스", new BigDecimal("15000"));
            when(subscriptionRepository.findCurrentSubscriptions(eq(USER_UUID), any(LocalDate.class)))
                    .thenReturn(Arrays.asList(sub1, sub2));

            // when
            Map<String, Object> result = statisticsService.getSubscriptionCostComparison(USER_UUID);

            // then
            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) result.get("labels");
            @SuppressWarnings("unchecked")
            List<BigDecimal> data = (List<BigDecimal>) result.get("data");

            assertThat(labels).containsExactly("헬스장", "넷플릭스");
            assertThat(data).containsExactly(new BigDecimal("50000"), new BigDecimal("15000"));
        }
    }

    @Nested
    @DisplayName("getInvestmentSavingsStats")
    class GetInvestmentSavingsStats {

        @Test
        @DisplayName("최근 6개월 투자 절약액을 반환한다")
        void returnsInvestmentSavingsStats() {
            // given
            Investment investment = createInvestment(1L, "전자책 리더기");
            when(investmentRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(USER_UUID))
                    .thenReturn(List.of(investment));
            when(investmentUsageRepository.calculateMonthlySavings(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(new BigDecimal("10000"));

            // when
            Map<String, Object> result = statisticsService.getInvestmentSavingsStats(USER_UUID);

            // then
            @SuppressWarnings("unchecked")
            List<BigDecimal> data = (List<BigDecimal>) result.get("data");
            assertThat(data).hasSize(6);
            assertThat(data).allMatch(savings -> savings.compareTo(new BigDecimal("10000")) == 0);
        }

        @Test
        @DisplayName("투자가 없으면 0원을 반환한다")
        void returnsZeroWhenNoInvestments() {
            // given
            when(investmentRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(USER_UUID))
                    .thenReturn(Collections.emptyList());

            // when
            Map<String, Object> result = statisticsService.getInvestmentSavingsStats(USER_UUID);

            // then
            @SuppressWarnings("unchecked")
            List<BigDecimal> data = (List<BigDecimal>) result.get("data");
            assertThat(data).hasSize(6);
            assertThat(data).allMatch(savings -> savings.compareTo(BigDecimal.ZERO) == 0);
        }
    }

    @Nested
    @DisplayName("getSummaryStats")
    class GetSummaryStats {

        @Test
        @DisplayName("총 통계 요약을 반환한다")
        void returnsSummaryStats() {
            // given
            Subscription sub1 = createSubscription(1L, "헬스장", new BigDecimal("50000"));
            Subscription sub2 = createSubscription(2L, "넷플릭스", new BigDecimal("15000"));

            when(subscriptionRepository.countCurrentSubscriptions(eq(USER_UUID), any(LocalDate.class))).thenReturn(2L);
            when(subscriptionRepository.findCurrentSubscriptions(eq(USER_UUID), any(LocalDate.class)))
                    .thenReturn(Arrays.asList(sub1, sub2));
            when(investmentRepository.countByUserUuidAndIsActiveTrue(USER_UUID)).thenReturn(1L);

            // when
            Map<String, Object> result = statisticsService.getSummaryStats(USER_UUID);

            // then
            assertThat(result.get("subscriptionCount")).isEqualTo(2L);
            assertThat(result.get("totalMonthlyFee")).isEqualTo(new BigDecimal("65000"));
            assertThat(result.get("investmentCount")).isEqualTo(1L);
        }
    }

    private Subscription createSubscription(Long id, String name) {
        return createSubscription(id, name, new BigDecimal("50000"));
    }

    private Subscription createSubscription(Long id, String name, BigDecimal monthlyAmount) {
        // Constructor: (userUuid, name, emojiCode, periodType, totalAmount, monthlyAmount, startDate)
        Subscription subscription = new Subscription(USER_UUID, name, "gym", "monthly",
                monthlyAmount.multiply(BigDecimal.valueOf(12)), monthlyAmount, LocalDate.now());
        try {
            var idField = Subscription.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(subscription, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return subscription;
    }

    private Investment createInvestment(Long id, String name) {
        // Constructor: (userUuid, name, emojiCode, category, purchasePrice, purchaseDate, comparisonBaseline)
        Investment investment = new Investment(USER_UUID, name, "book", "gadget",
                new BigDecimal("200000"), LocalDate.now(), new BigDecimal("15000"));
        try {
            var idField = Investment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(investment, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return investment;
    }
}
