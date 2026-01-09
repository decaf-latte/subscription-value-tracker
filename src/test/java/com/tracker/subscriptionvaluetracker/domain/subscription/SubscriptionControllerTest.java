package com.tracker.subscriptionvaluetracker.domain.subscription;

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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionController 테스트")
class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private SubscriptionController controller;

    private final String TEST_USER_UUID = "test-user-uuid-1234";

    @BeforeEach
    void setUp() {
        Cookie[] cookies = { new Cookie("user_uuid", TEST_USER_UUID) };
        lenient().when(request.getCookies()).thenReturn(cookies);
    }

    @Nested
    @DisplayName("GET /subscriptions")
    class ListSubscriptions {

        @Test
        @DisplayName("구독 목록 페이지를 반환한다")
        void list_ReturnsListView() {
            // given
            SubscriptionViewDto dto = createViewDto(1L, "넷플릭스");
            given(subscriptionService.getSubscriptionsWithStats(TEST_USER_UUID))
                    .willReturn(List.of(dto));

            // when
            String result = controller.list(model, request, response);

            // then
            assertThat(result).isEqualTo("subscription/list");
            verify(model).addAttribute(eq("subscriptions"), any());
        }
    }

    @Nested
    @DisplayName("GET /subscriptions/new")
    class NewSubscriptionForm {

        @Test
        @DisplayName("구독 추가 폼 페이지를 반환한다")
        void newForm_ReturnsFormView() {
            // when
            String result = controller.newForm(model);

            // then
            assertThat(result).isEqualTo("subscription/form");
            verify(model).addAttribute(eq("form"), any(SubscriptionForm.class));
            verify(model).addAttribute(eq("emojiCodes"), any());
            verify(model).addAttribute("isEdit", false);
        }
    }

    @Nested
    @DisplayName("POST /subscriptions")
    class CreateSubscription {

        @Test
        @DisplayName("구독을 생성하고 캘린더로 리다이렉트한다")
        void create_RedirectsToCalendar() {
            // given
            SubscriptionForm form = new SubscriptionForm();
            form.setName("넷플릭스");
            Subscription subscription = createSubscription("넷플릭스");
            given(subscriptionService.createSubscription(eq(TEST_USER_UUID), any(SubscriptionForm.class)))
                    .willReturn(subscription);

            // when
            String result = controller.create(form, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/calendar");
            verify(subscriptionService).createSubscription(eq(TEST_USER_UUID), any(SubscriptionForm.class));
            verify(redirectAttributes).addFlashAttribute(eq("message"), any());
        }
    }

    @Nested
    @DisplayName("GET /subscriptions/{id}/edit")
    class EditSubscriptionForm {

        @Test
        @DisplayName("구독 수정 폼 페이지를 반환한다")
        void editForm_ReturnsFormView() {
            // given
            Subscription subscription = createSubscription("넷플릭스");
            given(subscriptionService.getSubscription(1L, TEST_USER_UUID))
                    .willReturn(Optional.of(subscription));

            // when
            String result = controller.editForm(1L, model, request, response);

            // then
            assertThat(result).isEqualTo("subscription/form");
            verify(model).addAttribute(eq("form"), any(SubscriptionForm.class));
            verify(model).addAttribute("subscriptionId", 1L);
            verify(model).addAttribute("isEdit", true);
        }

        @Test
        @DisplayName("존재하지 않는 구독 수정 시 예외가 발생한다")
        void editForm_ThrowsExceptionWhenNotFound() {
            // given
            given(subscriptionService.getSubscription(999L, TEST_USER_UUID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> controller.editForm(999L, model, request, response))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("구독을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("POST /subscriptions/{id}")
    class UpdateSubscription {

        @Test
        @DisplayName("구독을 수정하고 상세 페이지로 리다이렉트한다")
        void update_RedirectsToDetail() {
            // given
            SubscriptionForm form = new SubscriptionForm();
            form.setName("넷플릭스 프리미엄");
            Subscription subscription = createSubscription("넷플릭스 프리미엄");
            given(subscriptionService.updateSubscription(eq(1L), eq(TEST_USER_UUID), any(SubscriptionForm.class)))
                    .willReturn(subscription);

            // when
            String result = controller.update(1L, form, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/subscriptions/1");
            verify(subscriptionService).updateSubscription(eq(1L), eq(TEST_USER_UUID), any(SubscriptionForm.class));
        }
    }

    @Nested
    @DisplayName("POST /subscriptions/{id}/delete")
    class DeleteSubscription {

        @Test
        @DisplayName("구독을 삭제하고 대시보드로 리다이렉트한다")
        void delete_RedirectsToDashboard() {
            // when
            String result = controller.delete(1L, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/");
            verify(subscriptionService).deleteSubscription(1L, TEST_USER_UUID);
        }
    }

    @Nested
    @DisplayName("POST /subscriptions/{id}/check-in")
    class CheckIn {

        @Test
        @DisplayName("오늘 출석 체크하고 캘린더로 리다이렉트한다")
        void checkIn_Today_RedirectsToCalendar() {
            // given
            given(subscriptionService.toggleCheckIn(eq(1L), eq(TEST_USER_UUID), eq(LocalDate.now())))
                    .willReturn(true);

            // when
            String result = controller.checkIn(1L, null, null, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/calendar");
            verify(redirectAttributes).addFlashAttribute("message", "출석 완료!");
        }

        @Test
        @DisplayName("특정 날짜에 출석 체크한다")
        void checkIn_SpecificDate() {
            // given
            String dateStr = "2025-01-15";
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            given(subscriptionService.toggleCheckIn(eq(1L), eq(TEST_USER_UUID), eq(targetDate)))
                    .willReturn(true);

            // when
            String result = controller.checkIn(1L, dateStr, null, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/calendar");
            verify(subscriptionService).toggleCheckIn(1L, TEST_USER_UUID, targetDate);
        }

        @Test
        @DisplayName("출석 취소 시 취소 메시지를 표시한다")
        void checkIn_Cancel_ShowsCancelMessage() {
            // given
            given(subscriptionService.toggleCheckIn(eq(1L), eq(TEST_USER_UUID), eq(LocalDate.now())))
                    .willReturn(false);

            // when
            String result = controller.checkIn(1L, null, null, request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/calendar");
            verify(redirectAttributes).addFlashAttribute("message", "출석이 취소되었습니다.");
        }

        @Test
        @DisplayName("returnUrl이 있으면 해당 URL로 리다이렉트한다")
        void checkIn_WithReturnUrl() {
            // given
            given(subscriptionService.toggleCheckIn(eq(1L), eq(TEST_USER_UUID), eq(LocalDate.now())))
                    .willReturn(true);

            // when
            String result = controller.checkIn(1L, null, "/", request, response, redirectAttributes);

            // then
            assertThat(result).isEqualTo("redirect:/");
        }
    }

    // Helper methods
    private Subscription createSubscription(String name) {
        return new Subscription(
                TEST_USER_UUID, name, "netflix", "1개월",
                new BigDecimal("17000"), new BigDecimal("17000"), LocalDate.now()
        );
    }

    private SubscriptionViewDto createViewDto(Long id, String name) {
        return new SubscriptionViewDto(
                id, name, "netflix", "🎬", "1개월",
                new BigDecimal("17000"), new BigDecimal("17000"),
                LocalDate.now(), null, 5,
                new BigDecimal("3400"), "good", false
        );
    }
}
