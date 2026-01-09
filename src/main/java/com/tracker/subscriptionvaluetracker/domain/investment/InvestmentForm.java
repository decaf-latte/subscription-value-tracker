package com.tracker.subscriptionvaluetracker.domain.investment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvestmentForm {

    private String name;
    private String emojiCode;
    private String category;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private BigDecimal comparisonBaseline;
    private String note;

    public InvestmentForm() {
        this.purchaseDate = LocalDate.now();
        this.comparisonBaseline = BigDecimal.valueOf(15000); // 종이책 평균가 기본값
    }

    public static InvestmentForm from(Investment investment) {
        InvestmentForm form = new InvestmentForm();
        form.setName(investment.getName());
        form.setEmojiCode(investment.getEmojiCode());
        form.setCategory(investment.getCategory());
        form.setPurchasePrice(investment.getPurchasePrice());
        form.setPurchaseDate(investment.getPurchaseDate());
        form.setComparisonBaseline(investment.getComparisonBaseline());
        form.setNote(investment.getNote());
        return form;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getComparisonBaseline() {
        return comparisonBaseline;
    }

    public void setComparisonBaseline(BigDecimal comparisonBaseline) {
        this.comparisonBaseline = comparisonBaseline;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
