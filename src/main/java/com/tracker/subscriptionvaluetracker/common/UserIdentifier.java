package com.tracker.subscriptionvaluetracker.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

public class UserIdentifier {

    private static final String COOKIE_NAME = "user_uuid";
    private static final int MAX_AGE = 60 * 60 * 24 * 30; // 30일

    private UserIdentifier() {
    }

    public static String getUserUuid(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 없으면 새로 생성
        String uuid = UUID.randomUUID().toString();
        Cookie newCookie = new Cookie(COOKIE_NAME, uuid);
        newCookie.setMaxAge(MAX_AGE);
        newCookie.setPath("/");
        newCookie.setHttpOnly(true);
        response.addCookie(newCookie);
        return uuid;
    }
}
