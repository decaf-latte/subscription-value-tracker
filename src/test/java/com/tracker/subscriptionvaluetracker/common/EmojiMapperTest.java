package com.tracker.subscriptionvaluetracker.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EmojiMapper 테스트")
class EmojiMapperTest {

    @Nested
    @DisplayName("이모지 코드 → 이모지 변환")
    class ToEmoji {

        @ParameterizedTest
        @DisplayName("유효한 이모지 코드를 이모지로 변환한다")
        @CsvSource({
                "gym, 🏋️",
                "netflix, 🎬",
                "youtube, 📺",
                "music, 🎵",
                "book, 📚",
                "game, 🎮",
                "coffee, ☕",
                "swim, 🏊"
        })
        void toEmoji_ValidCode(String code, String expected) {
            // when
            String result = EmojiMapper.toEmoji(code);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("알 수 없는 코드는 기본 이모지를 반환한다")
        void toEmoji_UnknownCode() {
            // when
            String result = EmojiMapper.toEmoji("unknown_code");

            // then
            assertThat(result).isEqualTo("📌");
        }

        @Test
        @DisplayName("null 코드는 기본 이모지를 반환한다")
        void toEmoji_NullCode() {
            // when
            String result = EmojiMapper.toEmoji(null);

            // then
            assertThat(result).isEqualTo("📌");
        }

        @Test
        @DisplayName("빈 문자열 코드는 기본 이모지를 반환한다")
        void toEmoji_EmptyCode() {
            // when
            String result = EmojiMapper.toEmoji("");

            // then
            assertThat(result).isEqualTo("📌");
        }

        @ParameterizedTest
        @DisplayName("직접 이모지 값이 전달되면 그대로 반환한다")
        @CsvSource({
                "😀, 😀",
                "🎉, 🎉",
                "❤️, ❤️",
                "🏋️, 🏋️",
                "🎬, 🎬",
                "🚀, 🚀"
        })
        void toEmoji_DirectEmoji(String emoji, String expected) {
            // when
            String result = EmojiMapper.toEmoji(emoji);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("이모지와 텍스트가 섞인 경우 그대로 반환한다")
        void toEmoji_MixedContent() {
            // when
            String result = EmojiMapper.toEmoji("테스트😀");

            // then - 알파벳과 언더스코어만으로 이루어지지 않았으므로 그대로 반환
            assertThat(result).isEqualTo("테스트😀");
        }
    }

    @Nested
    @DisplayName("모든 이모지 코드 조회")
    class GetAllCodes {

        @Test
        @DisplayName("모든 이모지 코드 맵을 반환한다")
        void getAllCodes_ReturnsAllEmojis() {
            // when
            Map<String, String> result = EmojiMapper.getAllCodes();

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).containsKey("gym");
            assertThat(result).containsKey("netflix");
            assertThat(result.get("gym")).isEqualTo("🏋️");
        }

        @Test
        @DisplayName("반환된 맵에는 최소 8개 이상의 이모지가 있다")
        void getAllCodes_HasMinimumEmojis() {
            // when
            Map<String, String> result = EmojiMapper.getAllCodes();

            // then
            assertThat(result.size()).isGreaterThanOrEqualTo(8);
        }
    }
}
