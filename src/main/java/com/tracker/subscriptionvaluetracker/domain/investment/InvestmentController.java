package com.tracker.subscriptionvaluetracker.domain.investment;

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
@RequestMapping("/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping
    public String list(Model model, HttpServletRequest request, HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        List<InvestmentViewDto> investments = investmentService.getInvestmentsWithStats(userUuid);
        model.addAttribute("investments", investments);
        return "investment/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new InvestmentForm());
        model.addAttribute("emojiCodes", EmojiMapper.getInvestmentEmojiCodes());
        model.addAttribute("categories", EmojiMapper.getCategories());
        model.addAttribute("isEdit", false);
        return "investment/form";
    }

    @PostMapping
    public String create(@ModelAttribute InvestmentForm form,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        investmentService.createInvestment(userUuid, form);
        redirectAttributes.addFlashAttribute("message", "투자 항목이 등록되었습니다.");
        return "redirect:/investments";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        Investment investment = investmentService.getInvestment(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("투자 항목을 찾을 수 없습니다."));

        InvestmentViewDto dto = investmentService.toViewDto(investment);
        List<InvestmentUsage> usages = investmentService.getUsages(id);

        model.addAttribute("investment", dto);
        model.addAttribute("usages", usages);
        model.addAttribute("usageForm", new InvestmentUsageForm());
        return "investment/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        Investment investment = investmentService.getInvestment(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("투자 항목을 찾을 수 없습니다."));

        model.addAttribute("form", InvestmentForm.from(investment));
        model.addAttribute("investmentId", id);
        model.addAttribute("emojiCodes", EmojiMapper.getInvestmentEmojiCodes());
        model.addAttribute("categories", EmojiMapper.getCategories());
        model.addAttribute("isEdit", true);
        return "investment/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute InvestmentForm form,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        investmentService.updateInvestment(id, userUuid, form);
        redirectAttributes.addFlashAttribute("message", "투자 항목이 수정되었습니다.");
        return "redirect:/investments/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        investmentService.deleteInvestment(id, userUuid);
        redirectAttributes.addFlashAttribute("message", "투자 항목이 삭제되었습니다.");
        return "redirect:/investments";
    }

    @PostMapping("/{id}/usage")
    public String addUsage(@PathVariable Long id,
                           @ModelAttribute InvestmentUsageForm form,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        investmentService.addUsage(id, userUuid, form);
        redirectAttributes.addFlashAttribute("message", "사용 기록이 추가되었습니다.");
        return "redirect:/investments/" + id;
    }

    @PostMapping("/{id}/usage/{usageId}/delete")
    public String deleteUsage(@PathVariable Long id,
                              @PathVariable Long usageId,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        investmentService.deleteUsage(usageId, userUuid);
        redirectAttributes.addFlashAttribute("message", "사용 기록이 삭제되었습니다.");
        return "redirect:/investments/" + id;
    }
}
