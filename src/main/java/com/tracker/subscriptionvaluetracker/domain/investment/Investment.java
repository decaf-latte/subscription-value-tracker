package com.tracker.subscriptionvaluetracker.domain.investment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment", indexes = {
    @Index(name = "idx_investment_user", columnList = "userUuid")
})
public class Investment {

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
    private String category;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal comparisonBaseline;

    @Column(length = 255)
    private String note;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Investment() {
    }

    public Investment(String userUuid, String name, String emojiCode, String category,
                      BigDecimal purchasePrice, LocalDate purchaseDate, BigDecimal comparisonBaseline) {
        this.userUuid = userUuid;
        this.name = name;
        this.emojiCode = emojiCode;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.comparisonBaseline = comparisonBaseline;
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

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void setComparisonBaseline(BigDecimal comparisonBaseline) {
        this.comparisonBaseline = comparisonBaseline;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
