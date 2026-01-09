package com.tracker.subscriptionvaluetracker.web;

import com.tracker.subscriptionvaluetracker.common.UserIdentifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public String statsPage(Model model, HttpServletRequest request, HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);

        Map<String, Object> summary = statisticsService.getSummaryStats(userUuid);
        model.addAttribute("summary", summary);

        return "stats";
    }

    @GetMapping("/api/monthly-usage")
    @ResponseBody
    public Map<String, Object> getMonthlyUsage(HttpServletRequest request, HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        return statisticsService.getMonthlyUsageStats(userUuid);
    }

    @GetMapping("/api/cost-comparison")
    @ResponseBody
    public Map<String, Object> getCostComparison(HttpServletRequest request, HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        return statisticsService.getSubscriptionCostComparison(userUuid);
    }

    @GetMapping("/api/investment-savings")
    @ResponseBody
    public Map<String, Object> getInvestmentSavings(HttpServletRequest request, HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        return statisticsService.getInvestmentSavingsStats(userUuid);
    }
}
