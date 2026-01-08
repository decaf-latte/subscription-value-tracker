package com.tracker.subscriptionvaluetracker.web;

import com.tracker.subscriptionvaluetracker.common.UserIdentifier;
import com.tracker.subscriptionvaluetracker.domain.subscription.SubscriptionService;
import com.tracker.subscriptionvaluetracker.domain.subscription.SubscriptionViewDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Controller
public class DashboardController {

    private final SubscriptionService subscriptionService;

    public DashboardController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/")
    public String dashboard(Model model, HttpServletRequest request, HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);

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

        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("totalMonthlyFee", totalMonthlyFee);
        model.addAttribute("totalUsageCount", totalUsageCount);
        model.addAttribute("avgDailyCost", avgDailyCost);
        model.addAttribute("activeSubscriptionCount", activeSubscriptionCount);

        return "index";
    }
}
