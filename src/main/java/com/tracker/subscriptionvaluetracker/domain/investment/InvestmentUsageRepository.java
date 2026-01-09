package com.tracker.subscriptionvaluetracker.domain.investment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface InvestmentUsageRepository extends JpaRepository<InvestmentUsage, Long> {

    List<InvestmentUsage> findByInvestmentIdOrderByUsedAtDesc(Long investmentId);

    long countByInvestmentId(Long investmentId);

    @Query("SELECT COALESCE(SUM(u.originalPrice - u.actualPrice), 0) FROM InvestmentUsage u WHERE u.investmentId = :investmentId")
    BigDecimal calculateTotalSavings(@Param("investmentId") Long investmentId);

    List<InvestmentUsage> findTop5ByInvestmentIdOrderByUsedAtDesc(Long investmentId);
}
