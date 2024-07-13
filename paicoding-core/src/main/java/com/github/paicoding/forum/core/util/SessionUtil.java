package com.github.paicoding.forum.core.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.util.CollectionUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class SessionUtil {
    private static final int COOKIE_AGE = 30 * 86400;

    public static Cookie newCookie(String key, String session) {
        return newCookie(key, session, "/", COOKIE_AGE);
    }

    public static Cookie newCookie(String key, String session, String path, int maxAge) {
        Cookie cookie = new Cookie(key, session);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return cookie;
    }


    public static Cookie delCookie(String key) {
        return delCookie(key, "/");
    }

    public static Cookie delCookie(String key, String path) {
        Cookie cookie = new Cookie(key, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    /**
     * 根据key查询cookie
     *
     * @param request
     * @param name
     * @return
     */
    public static Cookie findCookieByName(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        return Arrays.stream(cookies).filter(cookie -> StringUtils.equalsAnyIgnoreCase(cookie.getName(), name))
                .findFirst().orElse(null);
    }


    public static String findCookieByName(ServerHttpRequest request, String name) {
        List<String> list = request.getHeaders().get("cookie");
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        for (String sub : list) {
            String[] elements = StringUtils.split(sub, ";");
            for (String element : elements) {
                String[] subs = StringUtils.split(element, "=");
                if (subs.length == 2 && StringUtils.equalsAnyIgnoreCase(subs[0].trim(), name)) {
                    return subs[1].trim();
                }
            }
        }
        return null;
    }
}
