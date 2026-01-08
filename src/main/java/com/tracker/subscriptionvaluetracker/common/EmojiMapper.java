package com.tracker.subscriptionvaluetracker.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmojiMapper {

    private static final Map<String, String> EMOJI_MAP;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("gym", "🏋️");
        map.put("netflix", "🎬");
        map.put("youtube", "📺");
        map.put("book", "📚");
        map.put("ebook", "📖");
        map.put("music", "🎵");
        map.put("game", "🎮");
        map.put("coffee", "☕");
        map.put("swim", "🏊");
        map.put("pilates", "🧘");
        map.put("language", "🗣️");
        map.put("default", "📌");
        EMOJI_MAP = Collections.unmodifiableMap(map);
    }

    private EmojiMapper() {
    }

    public static String toEmoji(String code) {
        return EMOJI_MAP.getOrDefault(code, EMOJI_MAP.get("default"));
    }

    public static Map<String, String> getAllCodes() {
        return EMOJI_MAP;
    }
}
