package com.tracker.subscriptionvaluetracker.domain.subscription;

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
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService 테스트")
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UsageLogRepository usageLogRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private final String TEST_USER_UUID = "test-user-uuid-1234";

    @Nested
    @DisplayName("구독 생성")
    class CreateSubscription {

        @Test
        @DisplayName("정상적인 구독을 생성할 수 있다")
        void createSubscription_Success() {
            // given
            SubscriptionForm form = new SubscriptionForm();
            form.setName("넷플릭스");
            form.setEmojiCode("netflix");
            form.setPeriodType("1개월");
            form.setTotalAmount(new BigDecimal("17000"));
            form.setMonthlyAmount(new BigDecimal("17000"));
            form.setStartDate(LocalDate.now());

            Subscription savedSubscription = new Subscription(
                    TEST_USER_UUID, "넷플릭스", "netflix", "1개월",
                    new BigDecimal("17000"), new BigDecimal("17000"), LocalDate.now()
            );

            given(subscriptionRepository.save(any(Subscription.class))).willReturn(savedSubscription);

            // when
            Subscription result = subscriptionService.createSubscription(TEST_USER_UUID, form);

            // then
            assertThat(result.getName()).isEqualTo("넷플릭스");
            assertThat(result.getMonthlyAmount()).isEqualTo(new BigDecimal("17000"));
            verify(subscriptionRepository).save(any(Subscription.class));
        }

        @Test
        @DisplayName("종료일이 있는 구독을 생성할 수 있다")
        void createSubscription_WithEndDate() {
            // given
            SubscriptionForm form = new SubscriptionForm();
            form.setName("헬스장");
            form.setEmojiCode("gym");
            form.setPeriodType("12개월");
            form.setTotalAmount(new BigDecimal("360000"));
            form.setMonthlyAmount(new BigDecimal("30000"));
            form.setStartDate(LocalDate.now());
            form.setEndDate(LocalDate.now().plusMonths(12));

            given(subscriptionRepository.save(any(Subscription.class))).willAnswer(i -> i.getArgument(0));

            // when
            Subscription result = subscriptionService.createSubscription(TEST_USER_UUID, form);

            // then
            assertThat(result.getEndDate()).isNotNull();
            assertThat(result.getEndDate()).isEqualTo(LocalDate.now().plusMonths(12));
        }
    }

    @Nested
    @DisplayName("구독 조회")
    class GetSubscription {

        @Test
        @DisplayName("사용자의 활성 구독 목록을 조회할 수 있다")
        void getActiveSubscriptions_Success() {
            // given
            Subscription sub1 = createTestSubscription("넷플릭스", "17000");
            Subscription sub2 = createTestSubscription("헬스장", "30000");

            given(subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(TEST_USER_UUID))
                    .willReturn(List.of(sub1, sub2));

            // when
            List<Subscription> result = subscriptionService.getActiveSubscriptions(TEST_USER_UUID);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("name").containsExactly("넷플릭스", "헬스장");
        }

        @Test
        @DisplayName("특정 구독을 조회할 수 있다")
        void getSubscription_Success() {
            // given
            Subscription subscription = createTestSubscription("넷플릭스", "17000");
            given(subscriptionRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(subscription));

            // when
            Optional<Subscription> result = subscriptionService.getSubscription(1L, TEST_USER_UUID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("넷플릭스");
        }

        @Test
        @DisplayName("존재하지 않는 구독 조회 시 빈 Optional을 반환한다")
        void getSubscription_NotFound() {
            // given
            given(subscriptionRepository.findByIdAndUserUuid(999L, TEST_USER_UUID))
                    .willReturn(Optional.empty());

            // when
            Optional<Subscription> result = subscriptionService.getSubscription(999L, TEST_USER_UUID);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("구독 수정")
    class UpdateSubscription {

        @Test
        @DisplayName("구독 정보를 수정할 수 있다")
        void updateSubscription_Success() {
            // given
            Subscription existing = createTestSubscription("넷플릭스", "17000");
            given(subscriptionRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(existing));
            given(subscriptionRepository.save(any(Subscription.class))).willAnswer(i -> i.getArgument(0));

            SubscriptionForm form = new SubscriptionForm();
            form.setName("넷플릭스 프리미엄");
            form.setEmojiCode("netflix");
            form.setPeriodType("1개월");
            form.setTotalAmount(new BigDecimal("27000"));
            form.setMonthlyAmount(new BigDecimal("27000"));
            form.setStartDate(LocalDate.now());

            // when
            Subscription result = subscriptionService.updateSubscription(1L, TEST_USER_UUID, form);

            // then
            assertThat(result.getName()).isEqualTo("넷플릭스 프리미엄");
            assertThat(result.getMonthlyAmount()).isEqualTo(new BigDecimal("27000"));
        }

        @Test
        @DisplayName("존재하지 않는 구독 수정 시 예외가 발생한다")
        void updateSubscription_NotFound() {
            // given
            given(subscriptionRepository.findByIdAndUserUuid(999L, TEST_USER_UUID))
                    .willReturn(Optional.empty());

            SubscriptionForm form = new SubscriptionForm();

            // when & then
            assertThatThrownBy(() -> subscriptionService.updateSubscription(999L, TEST_USER_UUID, form))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("구독을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("구독 삭제")
    class DeleteSubscription {

        @Test
        @DisplayName("구독을 비활성화(소프트 삭제)할 수 있다")
        void deleteSubscription_Success() {
            // given
            Subscription subscription = createTestSubscription("넷플릭스", "17000");
            given(subscriptionRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(subscription));

            // when
            subscriptionService.deleteSubscription(1L, TEST_USER_UUID);

            // then
            assertThat(subscription.getIsActive()).isFalse();
            verify(subscriptionRepository).save(subscription);
        }

        @Test
        @DisplayName("존재하지 않는 구독 삭제 시 예외가 발생한다")
        void deleteSubscription_NotFound() {
            // given
            given(subscriptionRepository.findByIdAndUserUuid(999L, TEST_USER_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subscriptionService.deleteSubscription(999L, TEST_USER_UUID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("구독을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("출석 체크")
    class CheckIn {

        @Test
        @DisplayName("오늘 날짜로 출석 체크할 수 있다")
        void checkIn_Success() {
            // given
            Subscription subscription = createTestSubscription("헬스장", "30000");
            given(subscriptionRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(subscription));
            given(usageLogRepository.findBySubscriptionIdAndUsedAt(1L, LocalDate.now()))
                    .willReturn(Optional.empty());
            given(usageLogRepository.save(any(UsageLog.class))).willAnswer(i -> i.getArgument(0));

            // when
            boolean result = subscriptionService.toggleCheckIn(1L, TEST_USER_UUID, LocalDate.now());

            // then
            assertThat(result).isTrue();
            verify(usageLogRepository).save(any(UsageLog.class));
        }

        @Test
        @DisplayName("이미 출석한 날짜에 토글하면 출석이 취소된다")
        void toggleCheckIn_Cancel() {
            // given
            Subscription subscription = createTestSubscription("헬스장", "30000");
            UsageLog existingLog = new UsageLog(1L, LocalDate.now());

            given(subscriptionRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(subscription));
            given(usageLogRepository.findBySubscriptionIdAndUsedAt(1L, LocalDate.now()))
                    .willReturn(Optional.of(existingLog));

            // when
            boolean result = subscriptionService.toggleCheckIn(1L, TEST_USER_UUID, LocalDate.now());

            // then
            assertThat(result).isFalse();
            verify(usageLogRepository).delete(existingLog);
        }

        @Test
        @DisplayName("특정 날짜에 출석 체크할 수 있다")
        void checkInOnDate_Success() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            Subscription subscription = createTestSubscription("헬스장", "30000");

            given(subscriptionRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(subscription));
            given(usageLogRepository.existsBySubscriptionIdAndUsedAt(1L, targetDate))
                    .willReturn(false);
            given(usageLogRepository.save(any(UsageLog.class))).willAnswer(i -> i.getArgument(0));

            // when
            UsageLog result = subscriptionService.checkInOnDate(1L, TEST_USER_UUID, targetDate);

            // then
            assertThat(result.getUsedAt()).isEqualTo(targetDate);
        }

        @Test
        @DisplayName("이미 출석한 날짜에 출석 시도 시 예외가 발생한다")
        void checkInOnDate_AlreadyCheckedIn() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            Subscription subscription = createTestSubscription("헬스장", "30000");

            given(subscriptionRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(subscription));
            given(usageLogRepository.existsBySubscriptionIdAndUsedAt(1L, targetDate))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> subscriptionService.checkInOnDate(1L, TEST_USER_UUID, targetDate))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("해당 날짜에 이미 출석했습니다.");
        }
    }

    @Nested
    @DisplayName("회당 비용 계산")
    class DailyCostCalculation {

        @Test
        @DisplayName("사용 횟수가 0이면 월 금액 전체가 회당 비용이다")
        void calculateDailyCost_ZeroUsage() {
            // given
            Subscription subscription = createTestSubscription("넷플릭스", "17000");
            setSubscriptionId(subscription, 1L);

            given(usageLogRepository.countBySubscriptionId(1L)).willReturn(0L);

            // when
            BigDecimal result = subscriptionService.calculateDailyCost(subscription);

            // then
            assertThat(result).isEqualTo(new BigDecimal("17000"));
        }

        @Test
        @DisplayName("총 금액을 사용 횟수로 나눈 회당 비용을 계산한다")
        void calculateDailyCost_WithUsage() {
            // given
            // 1개월 구독, 총 금액 30000원, 총 10회 사용
            Subscription subscription = createTestSubscription("헬스장", "30000");
            setSubscriptionId(subscription, 1L);

            given(usageLogRepository.countBySubscriptionId(1L)).willReturn(10L);

            // when
            BigDecimal result = subscriptionService.calculateDailyCost(subscription);

            // then
            // 총 금액 30000원 / 10회 = 3000원
            assertThat(result).isEqualTo(new BigDecimal("3000"));
        }

        @Test
        @DisplayName("총 금액을 기준으로 회당 비용을 계산한다 (3개월 구독)")
        void calculateDailyCost_MultipleMonths() {
            // given
            // 3개월 구독 (총 90000원, 월 30000원)
            Subscription subscription = new Subscription(
                    TEST_USER_UUID, "헬스장", "gym", "3개월",
                    new BigDecimal("90000"), new BigDecimal("30000"),
                    LocalDate.now().minusMonths(2)
            );
            setSubscriptionId(subscription, 1L);

            // 총 15회 사용
            given(usageLogRepository.countBySubscriptionId(1L)).willReturn(15L);

            // when
            BigDecimal result = subscriptionService.calculateDailyCost(subscription);

            // then
            // 총 금액 90000원 / 15회 = 6000원
            assertThat(result).isEqualTo(new BigDecimal("6000"));
        }

        @Test
        @DisplayName("일일 비용 레벨이 good으로 계산된다 (월 금액의 1/20 이하)")
        void getDailyCostLevel_Good() {
            // given
            BigDecimal monthlyAmount = new BigDecimal("20000");
            BigDecimal dailyCost = new BigDecimal("1000"); // 1/20

            // when
            String result = subscriptionService.getDailyCostLevel(dailyCost, monthlyAmount);

            // then
            assertThat(result).isEqualTo("good");
        }

        @Test
        @DisplayName("일일 비용 레벨이 normal로 계산된다 (월 금액의 1/20 초과, 1/10 이하)")
        void getDailyCostLevel_Normal() {
            // given
            BigDecimal monthlyAmount = new BigDecimal("20000");
            BigDecimal dailyCost = new BigDecimal("1500"); // 1/20 초과, 1/10 이하

            // when
            String result = subscriptionService.getDailyCostLevel(dailyCost, monthlyAmount);

            // then
            assertThat(result).isEqualTo("normal");
        }

        @Test
        @DisplayName("일일 비용 레벨이 warning으로 계산된다 (월 금액의 1/10 초과)")
        void getDailyCostLevel_Warning() {
            // given
            BigDecimal monthlyAmount = new BigDecimal("20000");
            BigDecimal dailyCost = new BigDecimal("5000"); // 1/10 초과

            // when
            String result = subscriptionService.getDailyCostLevel(dailyCost, monthlyAmount);

            // then
            assertThat(result).isEqualTo("warning");
        }
    }

    @Nested
    @DisplayName("오늘 출석 여부 확인")
    class CheckedInToday {

        @Test
        @DisplayName("오늘 출석한 경우 true를 반환한다")
        void isCheckedInToday_True() {
            // given
            given(usageLogRepository.existsBySubscriptionIdAndUsedAt(1L, LocalDate.now()))
                    .willReturn(true);

            // when
            boolean result = subscriptionService.isCheckedInToday(1L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("오늘 출석하지 않은 경우 false를 반환한다")
        void isCheckedInToday_False() {
            // given
            given(usageLogRepository.existsBySubscriptionIdAndUsedAt(1L, LocalDate.now()))
                    .willReturn(false);

            // when
            boolean result = subscriptionService.isCheckedInToday(1L);

            // then
            assertThat(result).isFalse();
        }
    }

    // Helper methods
    private Subscription createTestSubscription(String name, String monthlyAmount) {
        return new Subscription(
                TEST_USER_UUID, name, "test", "1개월",
                new BigDecimal(monthlyAmount), new BigDecimal(monthlyAmount), LocalDate.now()
        );
    }

    private void setSubscriptionId(Subscription subscription, Long id) {
        try {
            java.lang.reflect.Field idField = Subscription.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(subscription, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
