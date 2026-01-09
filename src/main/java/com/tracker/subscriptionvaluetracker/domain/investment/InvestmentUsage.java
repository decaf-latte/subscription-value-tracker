package com.tracker.subscriptionvaluetracker.domain.investment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment_usage", indexes = {
    @Index(name = "idx_investment_usage_investment", columnList = "investmentId"),
    @Index(name = "idx_investment_usage_date", columnList = "usedAt")
})
public class InvestmentUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long investmentId;

    @Column(nullable = false)
    private LocalDate usedAt;

    @Column(nullable = false, length = 200)
    private String itemName;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal actualPrice;

    @Column(length = 50)
    private String source;

    @Column(length = 255)
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected InvestmentUsage() {
    }

    public InvestmentUsage(Long investmentId, LocalDate usedAt, String itemName,
                           BigDecimal originalPrice, BigDecimal actualPrice) {
        this.investmentId = investmentId;
        this.usedAt = usedAt;
        this.itemName = itemName;
        this.originalPrice = originalPrice;
        this.actualPrice = actualPrice;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getInvestmentId() {
        return investmentId;
    }

    public LocalDate getUsedAt() {
        return usedAt;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public BigDecimal getActualPrice() {
        return actualPrice;
    }

    public String getSource() {
        return source;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 절약 금액 계산
    public BigDecimal getSavedAmount() {
        return originalPrice.subtract(actualPrice);
    }

    // Setters
    public void setUsedAt(LocalDate usedAt) {
        this.usedAt = usedAt;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setActualPrice(BigDecimal actualPrice) {
        this.actualPrice = actualPrice;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
