package com.tracker.subscriptionvaluetracker.domain.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SubscriptionViewDto {

    private final Long id;
    private final String name;
    private final String emojiCode;
    private final String emoji;
    private final String periodType;
    private final BigDecimal totalAmount;
    private final BigDecimal monthlyAmount;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int usageCount;
    private final BigDecimal dailyCost;
    private final String dailyCostLevel;
    private final boolean checkedInToday;

    public SubscriptionViewDto(Long id, String name, String emojiCode, String emoji,
                                String periodType, BigDecimal totalAmount, BigDecimal monthlyAmount,
                                LocalDate startDate, LocalDate endDate, int usageCount,
                                BigDecimal dailyCost, String dailyCostLevel, boolean checkedInToday) {
        this.id = id;
        this.name = name;
        this.emojiCode = emojiCode;
        this.emoji = emoji;
        this.periodType = periodType;
        this.totalAmount = totalAmount;
        this.monthlyAmount = monthlyAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.usageCount = usageCount;
        this.dailyCost = dailyCost;
        this.dailyCostLevel = dailyCostLevel;
        this.checkedInToday = checkedInToday;
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

    public String getPeriodType() {
        return periodType;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getMonthlyAmount() {
        return monthlyAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public BigDecimal getDailyCost() {
        return dailyCost;
    }

    public String getDailyCostLevel() {
        return dailyCostLevel;
    }

    public boolean isCheckedInToday() {
        return checkedInToday;
    }
}
