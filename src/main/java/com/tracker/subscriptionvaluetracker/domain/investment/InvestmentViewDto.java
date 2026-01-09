package com.tracker.subscriptionvaluetracker.domain.investment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvestmentViewDto {

    private Long id;
    private String name;
    private String emojiCode;
    private String emoji;
    private String category;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private BigDecimal comparisonBaseline;
    private String note;

    // 계산된 값들
    private int usageCount;
    private BigDecimal totalSavings;
    private BigDecimal netProfit;
    private boolean breakEvenReached;
    private BigDecimal breakEvenRemaining;
    private int breakEvenProgress;
    private BigDecimal avgSavingsPerUse;
    private List<InvestmentUsage> recentUsages;

    public InvestmentViewDto(Long id, String name, String emojiCode, String emoji, String category,
                             BigDecimal purchasePrice, LocalDate purchaseDate, BigDecimal comparisonBaseline,
                             String note, int usageCount, BigDecimal totalSavings, BigDecimal netProfit,
                             boolean breakEvenReached, BigDecimal breakEvenRemaining, int breakEvenProgress,
                             BigDecimal avgSavingsPerUse, List<InvestmentUsage> recentUsages) {
        this.id = id;
        this.name = name;
        this.emojiCode = emojiCode;
        this.emoji = emoji;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.comparisonBaseline = comparisonBaseline;
        this.note = note;
        this.usageCount = usageCount;
        this.totalSavings = totalSavings;
        this.netProfit = netProfit;
        this.breakEvenReached = breakEvenReached;
        this.breakEvenRemaining = breakEvenRemaining;
        this.breakEvenProgress = breakEvenProgress;
        this.avgSavingsPerUse = avgSavingsPerUse;
        this.recentUsages = recentUsages;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmojiCode() {
        return emojiCode;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public BigDecimal getComparisonBaseline() {
        return comparisonBaseline;
    }

    public String getNote() {
        return note;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public boolean isBreakEvenReached() {
        return breakEvenReached;
    }

    public BigDecimal getBreakEvenRemaining() {
        return breakEvenRemaining;
    }

    public int getBreakEvenProgress() {
        return breakEvenProgress;
    }

    public BigDecimal getAvgSavingsPerUse() {
        return avgSavingsPerUse;
    }

    public List<InvestmentUsage> getRecentUsages() {
        return recentUsages;
    }
}
