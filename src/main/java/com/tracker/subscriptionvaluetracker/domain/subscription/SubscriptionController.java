package com.tracker.subscriptionvaluetracker.domain.subscription;

import com.tracker.subscriptionvaluetracker.common.EmojiMapper;
import com.tracker.subscriptionvaluetracker.common.UserIdentifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public String list(Model model, HttpServletRequest request, HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        List<SubscriptionViewDto> subscriptions = subscriptionService.getSubscriptionsWithStats(userUuid);
        model.addAttribute("subscriptions", subscriptions);
        return "subscription/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        Subscription subscription = subscriptionService.getSubscription(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        SubscriptionViewDto dto = subscriptionService.toViewDto(subscription);
        List<UsageLog> usageLogs = subscriptionService.getRecentUsageLogs(id);

        model.addAttribute("subscription", dto);
        model.addAttribute("usageLogs", usageLogs);
        return "subscription/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new SubscriptionForm());
        model.addAttribute("emojiCodes", EmojiMapper.getAllCodes());
        model.addAttribute("isEdit", false);
        return "subscription/form";
    }

    @PostMapping
    public String create(@ModelAttribute SubscriptionForm form,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            subscriptionService.createSubscription(userUuid, form);
            redirectAttributes.addFlashAttribute("message", "구독이 등록되었습니다.");
            return "redirect:/calendar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/calendar";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        Subscription subscription = subscriptionService.getSubscription(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        model.addAttribute("form", SubscriptionForm.from(subscription));
        model.addAttribute("subscriptionId", id);
        model.addAttribute("emojiCodes", EmojiMapper.getAllCodes());
        model.addAttribute("isEdit", true);
        return "subscription/form";
    }

    @GetMapping("/{id}/json")
    @ResponseBody
    public Map<String, Object> getSubscriptionJson(@PathVariable Long id,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        Subscription subscription = subscriptionService.getSubscription(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        Map<String, Object> result = new HashMap<>();
        result.put("id", subscription.getId());
        result.put("name", subscription.getName());
        result.put("emojiCode", subscription.getEmojiCode());
        result.put("periodType", subscription.getPeriodType());
        result.put("totalAmount", subscription.getTotalAmount());
        result.put("monthlyAmount", subscription.getMonthlyAmount());
        result.put("startDate", subscription.getStartDate().toString());
        result.put("endDate", subscription.getEndDate() != null ? subscription.getEndDate().toString() : null);
        result.put("monthlyTargetUsage", subscription.getMonthlyTargetUsage());
        return result;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute SubscriptionForm form,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            subscriptionService.updateSubscription(id, userUuid, form);
            redirectAttributes.addFlashAttribute("message", "구독이 수정되었습니다.");
            return "redirect:/subscriptions/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/subscriptions/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        subscriptionService.deleteSubscription(id, userUuid);
        redirectAttributes.addFlashAttribute("message", "구독이 삭제되었습니다.");
        return "redirect:/";
    }

    @PostMapping("/{id}/check-in")
    public String checkIn(@PathVariable Long id,
                          @RequestParam(required = false) String date,
                          @RequestParam(required = false) String returnUrl,
                          Model model,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);

        LocalDate checkInDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();

        boolean checkedIn = subscriptionService.toggleCheckIn(id, userUuid, checkInDate);

        // HTMX 요청인지 확인
        boolean isHtmxRequest = request.getHeader("HX-Request") != null;

        if (isHtmxRequest) {
            // 업데이트된 구독 정보 조회
            SubscriptionViewDto updatedSub = subscriptionService.getSubscriptionWithStats(id, userUuid);
            model.addAttribute("sub", updatedSub);

            // 요약 통계도 함께 업데이트 (oob-swap용)
            List<SubscriptionViewDto> subscriptions = subscriptionService.getSubscriptionsWithStats(userUuid);
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

            model.addAttribute("totalMonthlyFee", totalMonthlyFee);
            model.addAttribute("totalUsageCount", totalUsageCount);
            model.addAttribute("avgDailyCost", avgDailyCost);
            model.addAttribute("activeSubscriptionCount", activeSubscriptionCount);

            return "fragments/check-in-response";
        }

        if (checkedIn) {
            redirectAttributes.addFlashAttribute("message", "출석 완료!");
        } else {
            redirectAttributes.addFlashAttribute("message", "출석이 취소되었습니다.");
        }

        return "redirect:" + (returnUrl != null ? returnUrl : "/calendar");
    }
}
