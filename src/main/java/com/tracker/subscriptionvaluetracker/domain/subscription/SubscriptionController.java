package com.tracker.subscriptionvaluetracker.domain.subscription;

import com.tracker.subscriptionvaluetracker.common.EmojiMapper;
import com.tracker.subscriptionvaluetracker.common.UserIdentifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
        subscriptionService.createSubscription(userUuid, form);
        redirectAttributes.addFlashAttribute("message", "구독이 등록되었습니다.");
        return "redirect:/";
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

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute SubscriptionForm form,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        subscriptionService.updateSubscription(id, userUuid, form);
        redirectAttributes.addFlashAttribute("message", "구독이 수정되었습니다.");
        return "redirect:/";
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
                          @RequestParam(required = false) String returnUrl,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            subscriptionService.checkIn(id, userUuid);
            redirectAttributes.addFlashAttribute("message", "출석 완료!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + (returnUrl != null ? returnUrl : "/calendar");
    }
}
