package com.tracker.subscriptionvaluetracker.domain.subscription;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_log", indexes = {
    @Index(name = "idx_usage_subscription_date", columnList = "subscriptionId, usedAt"),
    @Index(name = "idx_usage_used_at", columnList = "usedAt")
})
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long subscriptionId;

    @Column(nullable = false)
    private LocalDate usedAt;

    @Column(length = 255)
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UsageLog() {
    }

    public UsageLog(Long subscriptionId, LocalDate usedAt) {
        this.subscriptionId = subscriptionId;
        this.usedAt = usedAt;
    }

    public UsageLog(Long subscriptionId, LocalDate usedAt, String note) {
        this.subscriptionId = subscriptionId;
        this.usedAt = usedAt;
        this.note = note;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public LocalDate getUsedAt() {
        return usedAt;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setNote(String note) {
        this.note = note;
    }
}
