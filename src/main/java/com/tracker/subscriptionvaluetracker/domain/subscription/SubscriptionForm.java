package com.tracker.subscriptionvaluetracker.domain.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SubscriptionForm {

    private String name;
    private String emojiCode;
    private String periodType;
    private BigDecimal totalAmount;
    private BigDecimal monthlyAmount;
    private LocalDate startDate;
    private LocalDate endDate;

    public SubscriptionForm() {
    }

    public SubscriptionForm(String name, String emojiCode, String periodType,
                            BigDecimal totalAmount, BigDecimal monthlyAmount,
                            LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.emojiCode = emojiCode;
        this.periodType = periodType;
        this.totalAmount = totalAmount;
        this.monthlyAmount = monthlyAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmojiCode() {
        return emojiCode;
    }

    public void setEmojiCode(String emojiCode) {
        this.emojiCode = emojiCode;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(BigDecimal monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // Convert from entity
    public static SubscriptionForm from(Subscription subscription) {
        return new SubscriptionForm(
                subscription.getName(),
                subscription.getEmojiCode(),
                subscription.getPeriodType(),
                subscription.getTotalAmount(),
                subscription.getMonthlyAmount(),
                subscription.getStartDate(),
                subscription.getEndDate()
        );
    }
}
