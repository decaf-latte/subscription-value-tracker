package com.tracker.subscriptionvaluetracker.domain.investment;

import com.tracker.subscriptionvaluetracker.common.EmojiMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestmentUsageRepository usageRepository;

    public InvestmentService(InvestmentRepository investmentRepository,
                             InvestmentUsageRepository usageRepository) {
        this.investmentRepository = investmentRepository;
        this.usageRepository = usageRepository;
    }

    public List<Investment> getActiveInvestments(String userUuid) {
        return investmentRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(userUuid);
    }

    public Optional<Investment> getInvestment(Long id, String userUuid) {
        return investmentRepository.findByIdAndUserUuid(id, userUuid);
    }

    @Transactional
    public Investment createInvestment(String userUuid, InvestmentForm form) {
        Investment investment = new Investment(
                userUuid,
                form.getName(),
                form.getEmojiCode(),
                form.getCategory(),
                form.getPurchasePrice(),
                form.getPurchaseDate(),
                form.getComparisonBaseline()
        );
        if (form.getNote() != null) {
            investment.setNote(form.getNote());
        }
        return investmentRepository.save(investment);
    }

    @Transactional
    public Investment updateInvestment(Long id, String userUuid, InvestmentForm form) {
        Investment investment = investmentRepository.findByIdAndUserUuid(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("투자 항목을 찾을 수 없습니다."));

        investment.setName(form.getName());
        investment.setEmojiCode(form.getEmojiCode());
        investment.setCategory(form.getCategory());
        investment.setPurchasePrice(form.getPurchasePrice());
        investment.setPurchaseDate(form.getPurchaseDate());
        investment.setComparisonBaseline(form.getComparisonBaseline());
        investment.setNote(form.getNote());

        return investmentRepository.save(investment);
    }

    @Transactional
    public void deleteInvestment(Long id, String userUuid) {
        Investment investment = investmentRepository.findByIdAndUserUuid(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("투자 항목을 찾을 수 없습니다."));
        investment.setIsActive(false);
        investmentRepository.save(investment);
    }

    @Transactional
    public InvestmentUsage addUsage(Long investmentId, String userUuid, InvestmentUsageForm form) {
        investmentRepository.findByIdAndUserUuid(investmentId, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("투자 항목을 찾을 수 없습니다."));

        InvestmentUsage usage = new InvestmentUsage(
                investmentId,
                form.getUsedAt(),
                form.getItemName(),
                form.getOriginalPrice(),
                form.getActualPrice()
        );
        if (form.getSource() != null) {
            usage.setSource(form.getSource());
        }
        if (form.getNote() != null) {
            usage.setNote(form.getNote());
        }
        return usageRepository.save(usage);
    }

    @Transactional
    public void deleteUsage(Long usageId, String userUuid) {
        InvestmentUsage usage = usageRepository.findById(usageId)
                .orElseThrow(() -> new IllegalArgumentException("사용 기록을 찾을 수 없습니다."));

        investmentRepository.findByIdAndUserUuid(usage.getInvestmentId(), userUuid)
                .orElseThrow(() -> new IllegalArgumentException("권한이 없습니다."));

        usageRepository.delete(usage);
    }

    public List<InvestmentUsage> getUsages(Long investmentId) {
        return usageRepository.findByInvestmentIdOrderByUsedAtDesc(investmentId);
    }

    public int getUsageCount(Long investmentId) {
        return (int) usageRepository.countByInvestmentId(investmentId);
    }

    /**
     * 총 절약액 계산: 원래 가격 - 실제 지불 가격의 합
     */
    public BigDecimal calculateTotalSavings(Long investmentId) {
        BigDecimal savings = usageRepository.calculateTotalSavings(investmentId);
        return savings != null ? savings : BigDecimal.ZERO;
    }

    /**
     * 순이익 계산: 총 절약액 - 구매가
     */
    public BigDecimal calculateNetProfit(Investment investment) {
        BigDecimal totalSavings = calculateTotalSavings(investment.getId());
        return totalSavings.subtract(investment.getPurchasePrice());
    }

    /**
     * 손익분기점 도달 여부
     */
    public boolean isBreakEvenReached(Investment investment) {
        return calculateNetProfit(investment).compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * 손익분기점까지 남은 금액 (도달했으면 0)
     */
    public BigDecimal getBreakEvenRemaining(Investment investment) {
        BigDecimal netProfit = calculateNetProfit(investment);
        if (netProfit.compareTo(BigDecimal.ZERO) >= 0) {
            return BigDecimal.ZERO;
        }
        return netProfit.abs();
    }

    /**
     * 손익분기점 진행률 (0-100%)
     */
    public int getBreakEvenProgress(Investment investment) {
        BigDecimal totalSavings = calculateTotalSavings(investment.getId());
        BigDecimal purchasePrice = investment.getPurchasePrice();

        if (purchasePrice.compareTo(BigDecimal.ZERO) == 0) {
            return 100;
        }

        BigDecimal progress = totalSavings.multiply(BigDecimal.valueOf(100))
                .divide(purchasePrice, 0, RoundingMode.HALF_UP);

        return Math.min(progress.intValue(), 100);
    }

    /**
     * 사용당 평균 절약액
     */
    public BigDecimal getAvgSavingsPerUse(Long investmentId) {
        int usageCount = getUsageCount(investmentId);
        if (usageCount == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalSavings = calculateTotalSavings(investmentId);
        return totalSavings.divide(BigDecimal.valueOf(usageCount), 0, RoundingMode.HALF_UP);
    }

    public long getActiveInvestmentCount(String userUuid) {
        return investmentRepository.countByUserUuidAndIsActiveTrue(userUuid);
    }

    public InvestmentViewDto toViewDto(Investment investment) {
        int usageCount = getUsageCount(investment.getId());
        BigDecimal totalSavings = calculateTotalSavings(investment.getId());
        BigDecimal netProfit = calculateNetProfit(investment);
        boolean breakEvenReached = isBreakEvenReached(investment);
        BigDecimal breakEvenRemaining = getBreakEvenRemaining(investment);
        int breakEvenProgress = getBreakEvenProgress(investment);
        BigDecimal avgSavingsPerUse = getAvgSavingsPerUse(investment.getId());
        List<InvestmentUsage> recentUsages = usageRepository.findTop5ByInvestmentIdOrderByUsedAtDesc(investment.getId());
        String emoji = EmojiMapper.toInvestmentEmoji(investment.getEmojiCode());

        return new InvestmentViewDto(
                investment.getId(),
                investment.getName(),
                investment.getEmojiCode(),
                emoji,
                investment.getCategory(),
                investment.getPurchasePrice(),
                investment.getPurchaseDate(),
                investment.getComparisonBaseline(),
                investment.getNote(),
                usageCount,
                totalSavings,
                netProfit,
                breakEvenReached,
                breakEvenRemaining,
                breakEvenProgress,
                avgSavingsPerUse,
                recentUsages
        );
    }

    public List<InvestmentViewDto> getInvestmentsWithStats(String userUuid) {
        return getActiveInvestments(userUuid).stream()
                .map(this::toViewDto)
                .toList();
    }
}
