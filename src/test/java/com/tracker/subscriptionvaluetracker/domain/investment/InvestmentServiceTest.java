package com.tracker.subscriptionvaluetracker.domain.investment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentService 테스트")
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private InvestmentUsageRepository usageRepository;

    @InjectMocks
    private InvestmentService investmentService;

    private final String TEST_USER_UUID = "test-user-uuid-1234";

    @Nested
    @DisplayName("투자 항목 생성")
    class CreateInvestment {

        @Test
        @DisplayName("정상적인 투자 항목을 생성할 수 있다")
        void createInvestment_Success() {
            // given
            InvestmentForm form = new InvestmentForm();
            form.setName("크레마 카르타");
            form.setEmojiCode("ereader");
            form.setCategory("E_READER");
            form.setPurchasePrice(new BigDecimal("189000"));
            form.setPurchaseDate(LocalDate.now());
            form.setComparisonBaseline(new BigDecimal("15000"));

            Investment savedInvestment = new Investment(
                    TEST_USER_UUID, "크레마 카르타", "ereader", "E_READER",
                    new BigDecimal("189000"), LocalDate.now(), new BigDecimal("15000")
            );

            given(investmentRepository.save(any(Investment.class))).willReturn(savedInvestment);

            // when
            Investment result = investmentService.createInvestment(TEST_USER_UUID, form);

            // then
            assertThat(result.getName()).isEqualTo("크레마 카르타");
            assertThat(result.getPurchasePrice()).isEqualTo(new BigDecimal("189000"));
            verify(investmentRepository).save(any(Investment.class));
        }
    }

    @Nested
    @DisplayName("투자 항목 조회")
    class GetInvestment {

        @Test
        @DisplayName("사용자의 활성 투자 목록을 조회할 수 있다")
        void getActiveInvestments_Success() {
            // given
            Investment inv1 = createTestInvestment("크레마 카르타", "189000");
            Investment inv2 = createTestInvestment("연간 이용권", "100000");

            given(investmentRepository.findByUserUuidAndIsActiveTrueOrderByCreatedAtDesc(TEST_USER_UUID))
                    .willReturn(List.of(inv1, inv2));

            // when
            List<Investment> result = investmentService.getActiveInvestments(TEST_USER_UUID);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("name").containsExactly("크레마 카르타", "연간 이용권");
        }

        @Test
        @DisplayName("특정 투자 항목을 조회할 수 있다")
        void getInvestment_Success() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "189000");
            given(investmentRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(investment));

            // when
            Optional<Investment> result = investmentService.getInvestment(1L, TEST_USER_UUID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("크레마 카르타");
        }
    }

    @Nested
    @DisplayName("투자 항목 삭제")
    class DeleteInvestment {

        @Test
        @DisplayName("투자 항목을 비활성화(소프트 삭제)할 수 있다")
        void deleteInvestment_Success() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "189000");
            given(investmentRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(investment));

            // when
            investmentService.deleteInvestment(1L, TEST_USER_UUID);

            // then
            assertThat(investment.getIsActive()).isFalse();
            verify(investmentRepository).save(investment);
        }

        @Test
        @DisplayName("존재하지 않는 투자 항목 삭제 시 예외가 발생한다")
        void deleteInvestment_NotFound() {
            // given
            given(investmentRepository.findByIdAndUserUuid(999L, TEST_USER_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> investmentService.deleteInvestment(999L, TEST_USER_UUID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("투자 항목을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("사용 기록 추가")
    class AddUsage {

        @Test
        @DisplayName("사용 기록을 추가할 수 있다")
        void addUsage_Success() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "189000");
            given(investmentRepository.findByIdAndUserUuid(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(investment));
            given(usageRepository.save(any(InvestmentUsage.class))).willAnswer(i -> i.getArgument(0));

            InvestmentUsageForm form = new InvestmentUsageForm();
            form.setItemName("클린 코드");
            form.setOriginalPrice(new BigDecimal("33000"));
            form.setActualPrice(BigDecimal.ZERO);
            form.setUsedAt(LocalDate.now());
            form.setSource("밀리의서재");

            // when
            InvestmentUsage result = investmentService.addUsage(1L, TEST_USER_UUID, form);

            // then
            assertThat(result.getItemName()).isEqualTo("클린 코드");
            assertThat(result.getSavedAmount()).isEqualTo(new BigDecimal("33000"));
        }
    }

    @Nested
    @DisplayName("손익분기점 계산")
    class BreakEvenCalculation {

        @Test
        @DisplayName("총 절약액을 계산할 수 있다")
        void calculateTotalSavings_Success() {
            // given
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("100000"));

            // when
            BigDecimal result = investmentService.calculateTotalSavings(1L);

            // then
            assertThat(result).isEqualTo(new BigDecimal("100000"));
        }

        @Test
        @DisplayName("사용 기록이 없으면 총 절약액은 0이다")
        void calculateTotalSavings_NoUsage() {
            // given
            given(usageRepository.calculateTotalSavings(1L)).willReturn(null);

            // when
            BigDecimal result = investmentService.calculateTotalSavings(1L);

            // then
            assertThat(result).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("순이익을 계산할 수 있다 (절약액 - 구매가)")
        void calculateNetProfit_Success() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "189000");
            setInvestmentId(investment, 1L);
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("250000"));

            // when
            BigDecimal result = investmentService.calculateNetProfit(investment);

            // then
            // 250000 - 189000 = 61000
            assertThat(result).isEqualTo(new BigDecimal("61000"));
        }

        @Test
        @DisplayName("손익분기점 미달성 시 순이익이 음수다")
        void calculateNetProfit_Negative() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "189000");
            setInvestmentId(investment, 1L);
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("50000"));

            // when
            BigDecimal result = investmentService.calculateNetProfit(investment);

            // then
            // 50000 - 189000 = -139000
            assertThat(result).isEqualTo(new BigDecimal("-139000"));
        }

        @Test
        @DisplayName("손익분기점 도달 여부를 확인할 수 있다 - 도달")
        void isBreakEvenReached_True() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "189000");
            setInvestmentId(investment, 1L);
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("200000"));

            // when
            boolean result = investmentService.isBreakEvenReached(investment);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("손익분기점 도달 여부를 확인할 수 있다 - 미도달")
        void isBreakEvenReached_False() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "189000");
            setInvestmentId(investment, 1L);
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("100000"));

            // when
            boolean result = investmentService.isBreakEvenReached(investment);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("손익분기점까지 남은 금액을 계산할 수 있다")
        void getBreakEvenRemaining_Success() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "189000");
            setInvestmentId(investment, 1L);
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("100000"));

            // when
            BigDecimal result = investmentService.getBreakEvenRemaining(investment);

            // then
            // 189000 - 100000 = 89000
            assertThat(result).isEqualTo(new BigDecimal("89000"));
        }

        @Test
        @DisplayName("손익분기점 도달 시 남은 금액은 0이다")
        void getBreakEvenRemaining_Zero() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "189000");
            setInvestmentId(investment, 1L);
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("250000"));

            // when
            BigDecimal result = investmentService.getBreakEvenRemaining(investment);

            // then
            assertThat(result).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("손익분기점 진행률을 계산할 수 있다")
        void getBreakEvenProgress_Success() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "200000");
            setInvestmentId(investment, 1L);
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("100000"));

            // when
            int result = investmentService.getBreakEvenProgress(investment);

            // then
            // 100000 / 200000 * 100 = 50%
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("손익분기점 초과 시 진행률은 100%다")
        void getBreakEvenProgress_Over100() {
            // given
            Investment investment = createTestInvestment("크레마 카르타", "100000");
            setInvestmentId(investment, 1L);
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("150000"));

            // when
            int result = investmentService.getBreakEvenProgress(investment);

            // then
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("회당 평균 절약액을 계산할 수 있다")
        void getAvgSavingsPerUse_Success() {
            // given
            given(usageRepository.countByInvestmentId(1L)).willReturn(10L);
            given(usageRepository.calculateTotalSavings(1L)).willReturn(new BigDecimal("150000"));

            // when
            BigDecimal result = investmentService.getAvgSavingsPerUse(1L);

            // then
            // 150000 / 10 = 15000
            assertThat(result).isEqualTo(new BigDecimal("15000"));
        }

        @Test
        @DisplayName("사용 횟수가 0이면 평균 절약액은 0이다")
        void getAvgSavingsPerUse_NoUsage() {
            // given
            given(usageRepository.countByInvestmentId(1L)).willReturn(0L);

            // when
            BigDecimal result = investmentService.getAvgSavingsPerUse(1L);

            // then
            assertThat(result).isEqualTo(BigDecimal.ZERO);
        }
    }

    // Helper methods
    private Investment createTestInvestment(String name, String purchasePrice) {
        return new Investment(
                TEST_USER_UUID, name, "ereader", "E_READER",
                new BigDecimal(purchasePrice), LocalDate.now(), new BigDecimal("15000")
        );
    }

    private void setInvestmentId(Investment investment, Long id) {
        try {
            java.lang.reflect.Field idField = Investment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(investment, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
