package com.github.paicoding.forum.core.util;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @author YiHui
 * @date 2023/6/6
 */
public class SessionUtil {
    private static final int COOKIE_AGE = 30 * 86400;

    public static Cookie newCookie(String key, String session) {
        return newCookie(key, session, "/", COOKIE_AGE);
    }

    public static Cookie newCookie(String key, String session, String path, int maxAge) {
        String host = ReqInfoContext.getReqInfo().getHost();
        return newCookie(key, session, host, path, maxAge);
    }

    public static Cookie newCookie(String key, String session, String domain, String path, int maxAge) {
        // 移除端口号
        domain = removePortFromHost(domain);
        Cookie cookie = new Cookie(key, session);
        if (StringUtils.isNotBlank(domain)) {
            cookie.setDomain(domain);
        }
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    /**
     * 从host中移除端口号
     *
     * @param host 包含端口号的host，如 "localhost:8080"
     * @return 移除端口号后的host，如 "localhost"
     */
    private static String removePortFromHost(String host) {
        if (StringUtils.isBlank(host)) {
            return host;
        }
        int portIndex = host.indexOf(':');
        if (portIndex > 0) {
            return host.substring(0, portIndex);
        }
        return host;
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