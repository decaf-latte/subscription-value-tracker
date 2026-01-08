package com.tracker.subscriptionvaluetracker.domain.subscription;

import com.tracker.subscriptionvaluetracker.common.EmojiMapper;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarService 테스트")
class CalendarServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UsageLogRepository usageLogRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private CalendarService calendarService;

    private final String TEST_USER_UUID = "test-user-uuid-1234";

    @Nested
    @DisplayName("캘린더 데이터 조회")
    class GetCalendarDays {

        @Test
        @DisplayName("해당 월의 캘린더 일자 목록을 생성한다")
        void getCalendarDays_ReturnsCorrectDays() {
            // given
            int year = 2025;
            int month = 1;

            given(subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            List<CalendarDayDto> result = calendarService.getCalendarDays(TEST_USER_UUID, year, month);

            // then
            assertThat(result).isNotEmpty();
            // 2025년 1월은 수요일로 시작, 앞에 빈 날짜 포함
            // 첫 번째 날이 현재 월의 날짜인지 확인 (앞쪽 패딩 후)
            long currentMonthDays = result.stream()
                    .filter(CalendarDayDto::isCurrentMonth)
                    .count();
            assertThat(currentMonthDays).isEqualTo(31); // 1월은 31일
        }

        @Test
        @DisplayName("오늘 날짜가 올바르게 표시된다")
        void getCalendarDays_TodayMarked() {
            // given
            LocalDate today = LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();

            given(subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            List<CalendarDayDto> result = calendarService.getCalendarDays(TEST_USER_UUID, year, month);

            // then
            long todayCount = result.stream()
                    .filter(CalendarDayDto::isToday)
                    .count();
            assertThat(todayCount).isEqualTo(1);
        }

        @Test
        @DisplayName("출석 기록이 캘린더에 표시된다")
        void getCalendarDays_WithUsageRecords() {
            // given
            int year = 2025;
            int month = 1;
            LocalDate usageDate = LocalDate.of(2025, 1, 15);

            Subscription subscription = createTestSubscription(1L, "헬스장", "30000");
            UsageLog usageLog = new UsageLog(1L, usageDate);

            given(subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(TEST_USER_UUID))
                    .willReturn(List.of(subscription));
            given(usageLogRepository.findBySubscriptionIdsAndDateRange(
                    eq(List.of(1L)), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(List.of(usageLog));

            // when
            List<CalendarDayDto> result = calendarService.getCalendarDays(TEST_USER_UUID, year, month);

            // then
            CalendarDayDto day15 = result.stream()
                    .filter(d -> d.getDayOfMonth() == 15 && d.isCurrentMonth())
                    .findFirst()
                    .orElseThrow();

            assertThat(day15.hasUsages()).isTrue();
        }

        @Test
        @DisplayName("구독이 없으면 빈 캘린더가 생성된다")
        void getCalendarDays_NoSubscriptions() {
            // given
            int year = 2025;
            int month = 1;

            given(subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            List<CalendarDayDto> result = calendarService.getCalendarDays(TEST_USER_UUID, year, month);

            // then
            assertThat(result).isNotEmpty();
            boolean anyUsages = result.stream().anyMatch(CalendarDayDto::hasUsages);
            assertThat(anyUsages).isFalse();
        }
    }

    @Nested
    @DisplayName("범례용 구독 조회")
    class GetSubscriptionsForLegend {

        @Test
        @DisplayName("범례용 구독 목록을 조회한다")
        void getSubscriptionsForLegend_Success() {
            // given
            Subscription sub1 = createTestSubscription(1L, "넷플릭스", "17000");
            Subscription sub2 = createTestSubscription(2L, "헬스장", "30000");

            given(subscriptionRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(TEST_USER_UUID))
                    .willReturn(List.of(sub1, sub2));

            // when
            List<SubscriptionViewDto> result = calendarService.getSubscriptionsForLegend(TEST_USER_UUID);

            // then
            assertThat(result).hasSize(2);
        }
    }

    // Helper method
    private Subscription createTestSubscription(Long id, String name, String monthlyAmount) {
        Subscription subscription = new Subscription(
                TEST_USER_UUID, name, "test", "1개월",
                new BigDecimal(monthlyAmount), new BigDecimal(monthlyAmount), LocalDate.now()
        );
        setSubscriptionId(subscription, id);
        return subscription;
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
