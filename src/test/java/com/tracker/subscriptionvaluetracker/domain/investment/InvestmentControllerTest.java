package com.tracker.subscriptionvaluetracker.domain.investment;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentController 테스트")
class InvestmentControllerTest {

    @Mock
    private InvestmentService investmentService;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private InvestmentController controller;

    private final String TEST_USER_UUID = "test-user-uuid-1234";

    @BeforeEach
    void setUp() {
        Cookie[] cookies = { new Cookie("user_uuid", TEST_USER_UUID) };
        lenient().when(request.getCookies()).thenReturn(cookies);
    }

    @Nested
    @DisplayName("GET /investments")
    class ListInvestments {

        @Test
        @DisplayName("투자 목록 페이지를 반환한다")
        void list_ReturnsListView() {
            // given
            InvestmentViewDto dto = createViewDto(1L, "크레마 카르타");
            given(investmentService.getInvestmentsWithStats(TEST_USER_UUID))
                    .willReturn(List.of(dto));

            // when
            String result = controller.list(model, request, response);

            // then
            assertThat(result).isEqualTo("investment/list");
            verify(model).addAttribute(eq("investments"), any());
        }
    }

    @Nested
    @DisplayName("GET /investments/new")
    class NewInvestmentForm {

        @Test
        @DisplayName("투자 추가 폼 페이지를 반환한다")
        void newForm_ReturnsFormView() {
            // when
            String result = controller.newForm(model);

            // then
            assertThat(result).isEqualTo("investment/form");
            verify(model).addAttribute(eq("form"), any(InvestmentForm.class));
            verify(model).addAttribute(eq("emojiCodes"), any());
            verify(model).addAttribute(eq("categories"), any());
            verify(model).addAttribute("isEdit", false);
        }
    }

    @Nested
    @DisplayName("POST /investments")
    class CreateInvestment {

        @Test
        @DisplayName("투자를 생성하고 목록으로 리다이렉트한다")
        void create_RedirectsToList() {
            // given
            InvestmentForm form = new InvestmentForm();
            form.setName("크레마 카르타");
            Investment investment = createInvestment("크레마 카르타");
            given(investmentService.createInvestment(eq(TEST_USER_UUID), any(InvestmentForm.class)))
                    .willReturn(investment);

            // when
            String result = controller.create(form, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/investments");
            verify(investmentService).createInvestment(eq(TEST_USER_UUID), any(InvestmentForm.class));
            verify(redirectAttributes).addFlashAttribute(eq("message"), any());
        }
    }

    @Nested
    @DisplayName("GET /investments/{id}")
    class DetailInvestment {

        @Test
        @DisplayName("투자 상세 페이지를 반환한다")
        void detail_ReturnsDetailView() {
            // given
            Investment investment = createInvestment("크레마 카르타");
            InvestmentViewDto dto = createViewDto(1L, "크레마 카르타");
            given(investmentService.getInvestment(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(investment));
            given(investmentService.toViewDto(investment)).willReturn(dto);
            given(investmentService.getUsages(1L)).willReturn(Collections.emptyList());

            // when
            String result = controller.detail(1L, model, request, response);

            // then
            assertThat(result).isEqualTo("investment/detail");
            verify(model).addAttribute(eq("investment"), any());
            verify(model).addAttribute(eq("usages"), any());
            verify(model).addAttribute(eq("usageForm"), any(InvestmentUsageForm.class));
        }

        @Test
        @DisplayName("존재하지 않는 투자 상세 조회 시 예외가 발생한다")
        void detail_ThrowsExceptionWhenNotFound() {
            // given
            given(investmentService.getInvestment(999L, TEST_USER_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> controller.detail(999L, model, request, response))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("투자 항목을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("GET /investments/{id}/edit")
    class EditInvestmentForm {

        @Test
        @DisplayName("투자 수정 폼 페이지를 반환한다")
        void editForm_ReturnsFormView() {
            // given
            Investment investment = createInvestment("크레마 카르타");
            given(investmentService.getInvestment(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(investment));

            // when
            String result = controller.editForm(1L, model, request, response);

            // then
            assertThat(result).isEqualTo("investment/form");
            verify(model).addAttribute(eq("form"), any(InvestmentForm.class));
            verify(model).addAttribute("investmentId", 1L);
            verify(model).addAttribute("isEdit", true);
        }

        @Test
        @DisplayName("존재하지 않는 투자 수정 시 예외가 발생한다")
        void editForm_ThrowsExceptionWhenNotFound() {
            // given
            given(investmentService.getInvestment(999L, TEST_USER_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> controller.editForm(999L, model, request, response))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("투자 항목을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("POST /investments/{id}")
    class UpdateInvestment {

        @Test
        @DisplayName("투자를 수정하고 상세 페이지로 리다이렉트한다")
        void update_RedirectsToDetail() {
            // given
            InvestmentForm form = new InvestmentForm();
            form.setName("크레마 카르타 플러스");
            Investment investment = createInvestment("크레마 카르타 플러스");
            given(investmentService.updateInvestment(eq(1L), eq(TEST_USER_UUID), any(InvestmentForm.class)))
                    .willReturn(investment);

            // when
            String result = controller.update(1L, form, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/investments/1");
            verify(investmentService).updateInvestment(eq(1L), eq(TEST_USER_UUID), any(InvestmentForm.class));
        }
    }

    @Nested
    @DisplayName("POST /investments/{id}/delete")
    class DeleteInvestment {

        @Test
        @DisplayName("투자를 삭제하고 목록으로 리다이렉트한다")
        void delete_RedirectsToList() {
            // when
            String result = controller.delete(1L, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/investments");
            verify(investmentService).deleteInvestment(1L, TEST_USER_UUID);
        }
    }

    @Nested
    @DisplayName("POST /investments/{id}/usage")
    class AddUsage {

        @Test
        @DisplayName("사용 기록을 추가하고 상세 페이지로 리다이렉트한다")
        void addUsage_RedirectsToDetail() {
            // given
            InvestmentUsageForm form = new InvestmentUsageForm();
            form.setItemName("클린 코드");
            form.setOriginalPrice(new BigDecimal("33000"));
            form.setActualPrice(BigDecimal.ZERO);
            form.setUsedAt(LocalDate.now());

            InvestmentUsage usage = new InvestmentUsage(
                    1L, LocalDate.now(), "클린 코드",
                    new BigDecimal("33000"), BigDecimal.ZERO
            );
            given(investmentService.addUsage(eq(1L), eq(TEST_USER_UUID), any(InvestmentUsageForm.class)))
                    .willReturn(usage);

            // when
            String result = controller.addUsage(1L, form, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/investments/1");
            verify(investmentService).addUsage(eq(1L), eq(TEST_USER_UUID), any(InvestmentUsageForm.class));
            verify(redirectAttributes).addFlashAttribute(eq("message"), any());
        }
    }

    @Nested
    @DisplayName("POST /investments/{id}/usage/{usageId}/delete")
    class DeleteUsage {

        @Test
        @DisplayName("사용 기록을 삭제하고 상세 페이지로 리다이렉트한다")
        void deleteUsage_RedirectsToDetail() {
            // when
            String result = controller.deleteUsage(1L, 10L, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/investments/1");
            verify(investmentService).deleteUsage(10L, TEST_USER_UUID);
            verify(redirectAttributes).addFlashAttribute(eq("message"), any());
        }
    }

    // Helper methods
    private Investment createInvestment(String name) {
        return new Investment(
                TEST_USER_UUID, name, "ereader", "E_READER",
                new BigDecimal("189000"), LocalDate.now(), new BigDecimal("15000")
        );
    }

    private InvestmentViewDto createViewDto(Long id, String name) {
        return new InvestmentViewDto(
                id, name, "ereader", "📱", "E_READER",
                new BigDecimal("189000"), LocalDate.now(), new BigDecimal("15000"),
                null, 10, new BigDecimal("150000"), new BigDecimal("-39000"),
                false, new BigDecimal("39000"), 79, new BigDecimal("15000"),
                Collections.emptyList()
        );
    }
}
