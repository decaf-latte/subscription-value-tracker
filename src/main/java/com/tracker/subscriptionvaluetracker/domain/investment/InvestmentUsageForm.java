package com.tracker.subscriptionvaluetracker.domain.investment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvestmentUsageForm {

    private String itemName;
    private BigDecimal originalPrice;
    private BigDecimal actualPrice;
    private LocalDate usedAt;
    private String source;
    private String note;

    public InvestmentUsageForm() {
        this.usedAt = LocalDate.now();
        this.actualPrice = BigDecimal.ZERO; // 기본값: 무료(구독)
    }

    // Getters and Setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(BigDecimal actualPrice) {
        this.actualPrice = actualPrice;
    }

    public LocalDate getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDate usedAt) {
        this.usedAt = usedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
