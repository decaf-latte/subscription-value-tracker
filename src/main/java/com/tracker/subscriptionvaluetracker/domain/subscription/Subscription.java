package com.tracker.subscriptionvaluetracker.domain.subscription;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription", indexes = {
    @Index(name = "idx_subscription_user", columnList = "userUuid")
})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String userUuid;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String emojiCode;

    @Column(nullable = false, length = 50)
    private String periodType;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal monthlyAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    // 월 목표 사용 횟수 (null이면 자동계산: 월구독료 ÷ 3000)
    private Integer monthlyTargetUsage;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Subscription() {
    }

    public Subscription(String userUuid, String name, String emojiCode, String periodType,
                        BigDecimal totalAmount, BigDecimal monthlyAmount, LocalDate startDate) {
        this.userUuid = userUuid;
        this.name = name;
        this.emojiCode = emojiCode;
        this.periodType = periodType;
        this.totalAmount = totalAmount;
        this.monthlyAmount = monthlyAmount;
        this.startDate = startDate;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public String getName() {
        return name;
    }

    public String getEmojiCode() {
        return emojiCode;
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

    public Integer getMonthlyTargetUsage() {
        return monthlyTargetUsage;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters for update
    public void setName(String name) {
        this.name = name;
    }

    public void setEmojiCode(String emojiCode) {
        this.emojiCode = emojiCode;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setMonthlyAmount(BigDecimal monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setMonthlyTargetUsage(Integer monthlyTargetUsage) {
        this.monthlyTargetUsage = monthlyTargetUsage;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * 월 목표 사용 횟수 계산 (설정값 또는 자동계산)
     */
    public int getCalculatedMonthlyTarget() {
        if (monthlyTargetUsage != null && monthlyTargetUsage > 0) {
            return monthlyTargetUsage;
        }
        // 자동계산: 월구독료 ÷ 3000 (최소 1회)
        int calculated = monthlyAmount.divide(java.math.BigDecimal.valueOf(3000), 0, java.math.RoundingMode.HALF_UP).intValue();
        return Math.max(calculated, 1);
    }
}
