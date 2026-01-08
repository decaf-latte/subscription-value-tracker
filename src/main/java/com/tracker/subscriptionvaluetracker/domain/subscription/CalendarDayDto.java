package com.tracker.subscriptionvaluetracker.domain.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CalendarDayDto {

    private final LocalDate date;
    private final int dayOfMonth;
    private final boolean isCurrentMonth;
    private final boolean isToday;
    private final List<UsageEntry> usages;

    public CalendarDayDto(LocalDate date, int dayOfMonth, boolean isCurrentMonth, boolean isToday, List<UsageEntry> usages) {
        this.date = date;
        this.dayOfMonth = dayOfMonth;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
        this.usages = usages;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public boolean isToday() {
        return isToday;
    }

    public List<UsageEntry> getUsages() {
        return usages;
    }

    public boolean hasUsages() {
        return usages != null && !usages.isEmpty();
    }

    // 각 출석 기록 항목
    public static class UsageEntry {
        private final Long subscriptionId;
        private final String subscriptionName;
        private final String emoji;
        private final BigDecimal dailyCost;
        private final String costLevel;

        public UsageEntry(Long subscriptionId, String subscriptionName, String emoji,
                         BigDecimal dailyCost, String costLevel) {
            this.subscriptionId = subscriptionId;
            this.subscriptionName = subscriptionName;
            this.emoji = emoji;
            this.dailyCost = dailyCost;
            this.costLevel = costLevel;
        }

        public Long getSubscriptionId() {
            return subscriptionId;
        }

        public String getSubscriptionName() {
            return subscriptionName;
        }

        public String getEmoji() {
            return emoji;
        }

        public BigDecimal getDailyCost() {
            return dailyCost;
        }

        public String getCostLevel() {
            return costLevel;
        }
    }
}
