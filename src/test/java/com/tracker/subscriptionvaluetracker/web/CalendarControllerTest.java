package com.tracker.subscriptionvaluetracker.web;

import com.tracker.subscriptionvaluetracker.domain.subscription.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarController 테스트")
class CalendarControllerTest {

    @Mock
    private CalendarService calendarService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CalendarController controller;

    private final String TEST_USER_UUID = "test-user-uuid-1234";

    @BeforeEach
    void setUp() {
        Cookie[] cookies = { new Cookie("user_uuid", TEST_USER_UUID) };
        lenient().when(request.getCookies()).thenReturn(cookies);
    }

    @Nested
    @DisplayName("GET /calendar")
    class CalendarPage {

        @Test
        @DisplayName("캘린더 페이지를 반환한다")
        void calendar_ReturnsCalendarView() {
            // given
            given(calendarService.getCalendarDays(anyString(), anyInt(), anyInt()))
                    .willReturn(Collections.emptyList());
            given(subscriptionService.getSubscriptionsWithStats(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            String result = controller.calendar(null, null, model, request, response);

            // then
            assertThat(result).isEqualTo("calendar");
            verify(model).addAttribute(eq("calendarDays"), any());
            verify(model).addAttribute(eq("subscriptions"), any());
            verify(model).addAttribute(eq("totalMonthlyFee"), any());
        }

        @Test
        @DisplayName("특정 년월을 지정하면 해당 월의 캘린더를 표시한다")
        void calendar_WithYearMonth() {
            // given
            given(calendarService.getCalendarDays(TEST_USER_UUID, 2025, 6))
                    .willReturn(Collections.emptyList());
            given(subscriptionService.getSubscriptionsWithStats(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            String result = controller.calendar(2025, 6, model, request, response);

            // then
            assertThat(result).isEqualTo("calendar");
            verify(model).addAttribute("year", 2025);
            verify(model).addAttribute("month", 6);
        }

        @Test
        @DisplayName("월이 12를 초과하면 다음 해 1월로 처리한다")
        void calendar_MonthOverflow() {
            // given
            given(calendarService.getCalendarDays(TEST_USER_UUID, 2026, 1))
                    .willReturn(Collections.emptyList());
            given(subscriptionService.getSubscriptionsWithStats(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            String result = controller.calendar(2025, 13, model, request, response);

            // then
            verify(model).addAttribute("year", 2026);
            verify(model).addAttribute("month", 1);
        }

        @Test
        @DisplayName("월이 1 미만이면 이전 해 12월로 처리한다")
        void calendar_MonthUnderflow() {
            // given
            given(calendarService.getCalendarDays(TEST_USER_UUID, 2024, 12))
                    .willReturn(Collections.emptyList());
            given(subscriptionService.getSubscriptionsWithStats(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            String result = controller.calendar(2025, 0, model, request, response);

            // then
            verify(model).addAttribute("year", 2024);
            verify(model).addAttribute("month", 12);
        }

        @Test
        @DisplayName("구독 통계가 올바르게 계산된다")
        void calendar_CalculatesStats() {
            // given
            SubscriptionViewDto dto1 = createViewDto(1L, "넷플릭스", "17000", 5);
            SubscriptionViewDto dto2 = createViewDto(2L, "헬스장", "30000", 10);

            given(calendarService.getCalendarDays(anyString(), anyInt(), anyInt()))
                    .willReturn(Collections.emptyList());
            given(subscriptionService.getSubscriptionsWithStats(TEST_USER_UUID))
                    .willReturn(List.of(dto1, dto2));

            // when
            controller.calendar(null, null, model, request, response);

            // then
            verify(model).addAttribute("totalMonthlyFee", new BigDecimal("47000"));
            verify(model).addAttribute("totalUsageCount", 15);
            verify(model).addAttribute("activeSubscriptionCount", 2L);
        }
    }

    @Nested
    @DisplayName("GET /calendar/grid")
    class CalendarGrid {

        @Test
        @DisplayName("캘린더 그리드 프래그먼트를 반환한다")
        void calendarGrid_ReturnsFragment() {
            // given
            given(calendarService.getCalendarDays(TEST_USER_UUID, 2025, 1))
                    .willReturn(Collections.emptyList());
            given(calendarService.getSubscriptionsForLegend(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            String result = controller.calendarGrid(2025, 1, model, request, response);

            // then
            assertThat(result).isEqualTo("fragments/calendar-grid :: calendarGrid");
            verify(model).addAttribute(eq("calendarDays"), any());
            verify(model).addAttribute(eq("subscriptions"), any());
        }

        @Test
        @DisplayName("이전/다음 월 정보가 포함된다")
        void calendarGrid_ContainsNavigation() {
            // given
            given(calendarService.getCalendarDays(TEST_USER_UUID, 2025, 1))
                    .willReturn(Collections.emptyList());
            given(calendarService.getSubscriptionsForLegend(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            controller.calendarGrid(2025, 1, model, request, response);

            // then
            verify(model).addAttribute("prevYear", 2024);
            verify(model).addAttribute("prevMonth", 12);
            verify(model).addAttribute("nextYear", 2025);
            verify(model).addAttribute("nextMonth", 2);
        }

        @Test
        @DisplayName("현재 월인지 여부가 표시된다")
        void calendarGrid_IsCurrentMonth() {
            // given
            YearMonth now = YearMonth.now();
            given(calendarService.getCalendarDays(TEST_USER_UUID, now.getYear(), now.getMonthValue()))
                    .willReturn(Collections.emptyList());
            given(calendarService.getSubscriptionsForLegend(TEST_USER_UUID))
                    .willReturn(Collections.emptyList());

            // when
            controller.calendarGrid(now.getYear(), now.getMonthValue(), model, request, response);

            // then
            verify(model).addAttribute("isCurrentMonth", true);
        }
    }

    // Helper method
    private SubscriptionViewDto createViewDto(Long id, String name, String monthlyAmount, int usageCount) {
        return new SubscriptionViewDto(
                id, name, "test", "📌", "1개월",
                new BigDecimal(monthlyAmount), new BigDecimal(monthlyAmount),
                LocalDate.now(), null, usageCount,
                new BigDecimal("3000"), "good", false
        );
    }
}
