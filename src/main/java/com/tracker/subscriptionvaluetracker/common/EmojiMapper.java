package com.tracker.subscriptionvaluetracker.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmojiMapper {

    private static final Map<String, String> EMOJI_MAP;

    private static final Map<String, String> INVESTMENT_EMOJI_MAP;
    private static final Map<String, String> CATEGORY_MAP;

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

        // 투자형 전용 이모지
        Map<String, String> investmentMap = new LinkedHashMap<>();
        investmentMap.put("ereader", "📱");
        investmentMap.put("tablet", "📲");
        investmentMap.put("laptop", "💻");
        investmentMap.put("annual_pass", "🎫");
        investmentMap.put("equipment", "🔧");
        investmentMap.put("camera", "📷");
        investmentMap.put("headphone", "🎧");
        investmentMap.put("default", "📦");
        INVESTMENT_EMOJI_MAP = Collections.unmodifiableMap(investmentMap);

        // 투자 카테고리
        Map<String, String> categoryMap = new LinkedHashMap<>();
        categoryMap.put("E_READER", "이북 리더기");
        categoryMap.put("ANNUAL_PASS", "연간 이용권");
        categoryMap.put("EQUIPMENT", "장비");
        categoryMap.put("OTHER", "기타");
        CATEGORY_MAP = Collections.unmodifiableMap(categoryMap);
    }

    private EmojiMapper() {
    }

    public static String toEmoji(String code) {
        return EMOJI_MAP.getOrDefault(code, EMOJI_MAP.get("default"));
    }

    public static Map<String, String> getAllCodes() {
        return EMOJI_MAP;
    }

    public static String toInvestmentEmoji(String code) {
        return INVESTMENT_EMOJI_MAP.getOrDefault(code, INVESTMENT_EMOJI_MAP.get("default"));
    }

    public static Map<String, String> getInvestmentEmojiCodes() {
        return INVESTMENT_EMOJI_MAP;
    }

    public static Map<String, String> getCategories() {
        return CATEGORY_MAP;
    }

    public static String getCategoryName(String code) {
        return CATEGORY_MAP.getOrDefault(code, "기타");
    }
}
