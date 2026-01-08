package com.tracker.subscriptionvaluetracker.web;

import com.tracker.subscriptionvaluetracker.common.UserIdentifier;
import com.tracker.subscriptionvaluetracker.domain.subscription.CalendarDayDto;
import com.tracker.subscriptionvaluetracker.domain.subscription.CalendarService;
import com.tracker.subscriptionvaluetracker.domain.subscription.SubscriptionService;
import com.tracker.subscriptionvaluetracker.domain.subscription.SubscriptionViewDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

@Controller
public class CalendarController {

    private final CalendarService calendarService;
    private final SubscriptionService subscriptionService;

    public CalendarController(CalendarService calendarService, SubscriptionService subscriptionService) {
        this.calendarService = calendarService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/calendar")
    public String calendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {

        String userUuid = UserIdentifier.getUserUuid(request, response);

        // 기본값: 현재 년월
        YearMonth currentYearMonth = YearMonth.now();
        int targetYear = (year != null) ? year : currentYearMonth.getYear();
        int targetMonth = (month != null) ? month : currentYearMonth.getMonthValue();

        // 유효성 검사
        if (targetMonth < 1) {
            targetMonth = 12;
            targetYear--;
        } else if (targetMonth > 12) {
            targetMonth = 1;
            targetYear++;
        }

        YearMonth targetYearMonth = YearMonth.of(targetYear, targetMonth);

        // 캘린더 데이터 조회
        List<CalendarDayDto> calendarDays = calendarService.getCalendarDays(userUuid, targetYear, targetMonth);

        // 구독 목록 (통계 포함)
        List<SubscriptionViewDto> subscriptions = subscriptionService.getSubscriptionsWithStats(userUuid);

        // 요약 통계 계산
        BigDecimal totalMonthlyFee = subscriptions.stream()
                .map(SubscriptionViewDto::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalUsageCount = subscriptions.stream()
                .mapToInt(SubscriptionViewDto::getUsageCount)
                .sum();

        BigDecimal avgDailyCost = BigDecimal.ZERO;
        if (totalUsageCount > 0) {
            avgDailyCost = totalMonthlyFee.divide(BigDecimal.valueOf(totalUsageCount), 0, RoundingMode.HALF_UP);
        }

        long activeSubscriptionCount = subscriptions.size();

        // 이전/다음 월 계산
        YearMonth prevMonth = targetYearMonth.minusMonths(1);
        YearMonth nextMonth = targetYearMonth.plusMonths(1);

        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("totalMonthlyFee", totalMonthlyFee);
        model.addAttribute("totalUsageCount", totalUsageCount);
        model.addAttribute("avgDailyCost", avgDailyCost);
        model.addAttribute("activeSubscriptionCount", activeSubscriptionCount);
        model.addAttribute("year", targetYear);
        model.addAttribute("month", targetMonth);
        model.addAttribute("yearMonth", targetYearMonth);
        model.addAttribute("prevYear", prevMonth.getYear());
        model.addAttribute("prevMonth", prevMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        model.addAttribute("isCurrentMonth", targetYearMonth.equals(currentYearMonth));

        return "calendar";
    }

    // HTMX 부분 업데이트용
    @GetMapping("/calendar/grid")
    public String calendarGrid(
            @RequestParam int year,
            @RequestParam int month,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {

        String userUuid = UserIdentifier.getUserUuid(request, response);

        List<CalendarDayDto> calendarDays = calendarService.getCalendarDays(userUuid, year, month);
        List<SubscriptionViewDto> subscriptions = calendarService.getSubscriptionsForLegend(userUuid);

        YearMonth targetYearMonth = YearMonth.of(year, month);
        YearMonth prevMonth = targetYearMonth.minusMonths(1);
        YearMonth nextMonth = targetYearMonth.plusMonths(1);

        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("yearMonth", targetYearMonth);
        model.addAttribute("prevYear", prevMonth.getYear());
        model.addAttribute("prevMonth", prevMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        model.addAttribute("isCurrentMonth", targetYearMonth.equals(YearMonth.now()));

        return "fragments/calendar-grid :: calendarGrid";
    }
}
