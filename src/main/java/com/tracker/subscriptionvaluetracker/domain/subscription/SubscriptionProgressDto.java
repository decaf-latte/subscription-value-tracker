package com.tracker.subscriptionvaluetracker.domain.subscription;

import java.math.BigDecimal;

/**
 * 구독 진행률 정보를 담는 DTO
 */
public class SubscriptionProgressDto {

    private Long id;
    private String name;
    private String emoji;

    // 기간 정보
    private int totalMonths;           // 전체 구독 기간 (월)
    private int elapsedMonths;         // 경과 기간 (월)
    private int periodProgress;        // 기간 진행률 (%)

    // 사용 정보
    private int targetTotalUsage;      // 목표 총 사용 횟수
    private int currentTotalUsage;     // 현재 총 사용 횟수
    private int usageProgress;         // 사용 진행률 (%)

    // 상태
    private String status;             // good, warning, danger
    private String statusMessage;      // 상태 메시지

    // 월 목표
    private int monthlyTarget;         // 월 목표 횟수

    public SubscriptionProgressDto() {
    }

    public SubscriptionProgressDto(Long id, String name, String emoji,
                                   int totalMonths, int elapsedMonths, int periodProgress,
                                   int targetTotalUsage, int currentTotalUsage, int usageProgress,
                                   String status, String statusMessage, int monthlyTarget) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.totalMonths = totalMonths;
        this.elapsedMonths = elapsedMonths;
        this.periodProgress = periodProgress;
        this.targetTotalUsage = targetTotalUsage;
        this.currentTotalUsage = currentTotalUsage;
        this.usageProgress = usageProgress;
        this.status = status;
        this.statusMessage = statusMessage;
        this.monthlyTarget = monthlyTarget;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getTotalMonths() {
        return totalMonths;
    }

    public int getElapsedMonths() {
        return elapsedMonths;
    }

    public int getPeriodProgress() {
        return periodProgress;
    }

    public int getTargetTotalUsage() {
        return targetTotalUsage;
    }

    public int getCurrentTotalUsage() {
        return currentTotalUsage;
    }

    public int getUsageProgress() {
        return usageProgress;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getMonthlyTarget() {
        return monthlyTarget;
    }
}
