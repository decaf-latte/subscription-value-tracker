package com.tracker.subscriptionvaluetracker.domain.subscription;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("구독 이모지 통합 테스트")
class SubscriptionEmojiIntegrationTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    private final String TEST_USER_UUID = "test-user-emoji-test";

    @Test
    @DisplayName("이모지를 직접 저장하고 조회할 수 있다")
    void saveAndRetrieveDirectEmoji() {
        // given
        String emoji = "😀";
        SubscriptionForm form = new SubscriptionForm(
                "테스트 구독",
                emoji,
                "1개월",
                new BigDecimal("10000"),
                new BigDecimal("10000"),
                LocalDate.now(),
                null
        );

        // when
        Subscription saved = subscriptionService.createSubscription(TEST_USER_UUID, form);
        Subscription found = subscriptionRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getEmojiCode()).isEqualTo(emoji);
        assertThat(found.getEmojiCode()).isEqualTo("😀");
    }

    @Test
    @DisplayName("다양한 이모지를 저장하고 조회할 수 있다")
    void saveAndRetrieveVariousEmojis() {
        // given
        String[] emojis = {"🎉", "❤️", "🏋️", "🎬", "🚀", "🌟", "🎮", "📚"};

        for (int i = 0; i < emojis.length; i++) {
            SubscriptionForm form = new SubscriptionForm(
                    "테스트 구독 " + i,
                    emojis[i],
                    "1개월",
                    new BigDecimal("10000"),
                    new BigDecimal("10000"),
                    LocalDate.now(),
                    null
            );

            // when
            Subscription saved = subscriptionService.createSubscription(TEST_USER_UUID, form);
            Subscription found = subscriptionRepository.findById(saved.getId()).orElseThrow();

            // then
            assertThat(found.getEmojiCode()).isEqualTo(emojis[i]);
        }
    }

    @Test
    @DisplayName("SubscriptionViewDto에서 이모지가 올바르게 변환된다")
    void emojiInViewDto() {
        // given
        String emoji = "🎬";
        SubscriptionForm form = new SubscriptionForm(
                "넷플릭스",
                emoji,
                "1개월",
                new BigDecimal("17000"),
                new BigDecimal("17000"),
                LocalDate.now(),
                null
        );

        // when
        Subscription saved = subscriptionService.createSubscription(TEST_USER_UUID, form);
        SubscriptionViewDto dto = subscriptionService.toViewDto(saved);

        // then
        assertThat(dto.getEmojiCode()).isEqualTo(emoji);
        assertThat(dto.getEmoji()).isEqualTo(emoji);
    }

    @Test
    @DisplayName("기존 코드 방식 이모지도 여전히 지원된다")
    void legacyCodeStyleEmoji() {
        // given - 기존 방식의 코드 사용
        String emojiCode = "netflix";
        SubscriptionForm form = new SubscriptionForm(
                "레거시 구독",
                emojiCode,
                "1개월",
                new BigDecimal("10000"),
                new BigDecimal("10000"),
                LocalDate.now(),
                null
        );

        // when
        Subscription saved = subscriptionService.createSubscription(TEST_USER_UUID, form);
        SubscriptionViewDto dto = subscriptionService.toViewDto(saved);

        // then
        assertThat(dto.getEmojiCode()).isEqualTo("netflix");
        assertThat(dto.getEmoji()).isEqualTo("🎬"); // 코드가 이모지로 변환됨
    }
}
