package com.tracker.subscriptionvaluetracker.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserIdentifier 테스트")
class UserIdentifierTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Nested
    @DisplayName("사용자 UUID 조회")
    class GetUserUuid {

        @Test
        @DisplayName("기존 쿠키가 있으면 해당 UUID를 반환한다")
        void getUserUuid_ExistingCookie() {
            // given
            String existingUuid = "existing-uuid-1234";
            Cookie[] cookies = { new Cookie("user_uuid", existingUuid) };
            given(request.getCookies()).willReturn(cookies);

            // when
            String result = UserIdentifier.getUserUuid(request, response);

            // then
            assertThat(result).isEqualTo(existingUuid);
            verify(response, never()).addCookie(any(Cookie.class));
        }

        @Test
        @DisplayName("쿠키가 없으면 새 UUID를 생성하고 쿠키에 저장한다")
        void getUserUuid_NoCookie_CreatesNew() {
            // given
            given(request.getCookies()).willReturn(null);

            // when
            String result = UserIdentifier.getUserUuid(request, response);

            // then
            assertThat(result).isNotNull();
            assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

            ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(cookieCaptor.capture());

            Cookie savedCookie = cookieCaptor.getValue();
            assertThat(savedCookie.getName()).isEqualTo("user_uuid");
            assertThat(savedCookie.getValue()).isEqualTo(result);
            assertThat(savedCookie.getMaxAge()).isEqualTo(30 * 24 * 60 * 60); // 30일
            assertThat(savedCookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("빈 쿠키 배열이면 새 UUID를 생성한다")
        void getUserUuid_EmptyCookies_CreatesNew() {
            // given
            given(request.getCookies()).willReturn(new Cookie[0]);

            // when
            String result = UserIdentifier.getUserUuid(request, response);

            // then
            assertThat(result).isNotNull();
            assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
            verify(response).addCookie(any(Cookie.class));
        }

        @Test
        @DisplayName("다른 이름의 쿠키만 있으면 새 UUID를 생성한다")
        void getUserUuid_DifferentCookie_CreatesNew() {
            // given
            Cookie[] cookies = { new Cookie("other_cookie", "some_value") };
            given(request.getCookies()).willReturn(cookies);

            // when
            String result = UserIdentifier.getUserUuid(request, response);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isNotEqualTo("some_value");
            verify(response).addCookie(any(Cookie.class));
        }

        @Test
        @DisplayName("매 호출마다 다른 UUID를 생성한다")
        void getUserUuid_GeneratesDifferentUuids() {
            // given
            given(request.getCookies()).willReturn(null);

            // when
            String result1 = UserIdentifier.getUserUuid(request, response);
            reset(response);
            String result2 = UserIdentifier.getUserUuid(request, response);

            // then
            assertThat(result1).isNotEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("쿠키 설정")
    class CookieSettings {

        @Test
        @DisplayName("쿠키의 HttpOnly 속성이 설정된다")
        void cookie_IsHttpOnly() {
            // given
            given(request.getCookies()).willReturn(null);

            // when
            UserIdentifier.getUserUuid(request, response);

            // then
            ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(cookieCaptor.capture());

            Cookie savedCookie = cookieCaptor.getValue();
            assertThat(savedCookie.isHttpOnly()).isTrue();
        }
    }
}
