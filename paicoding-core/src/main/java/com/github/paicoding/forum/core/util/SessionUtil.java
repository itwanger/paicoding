package com.github.paicoding.forum.core.util;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2023/6/6
 */
@Slf4j
public class SessionUtil {
    private static final int COOKIE_AGE = 5 * 86400;

    public static String buildSetCookieString(Cookie cookie) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookie.getName()).append("=").append(cookie.getValue());

        if (cookie.getMaxAge() >= 0) {
            sb.append("; Max-Age=").append(cookie.getMaxAge());
        }

        if (cookie.getDomain() != null) {
            sb.append("; Domain=").append(cookie.getDomain());
        }

        if (cookie.getPath() != null) {
            sb.append("; Path=").append(cookie.getPath());
        }

        if (cookie.getSecure()) {
            sb.append("; Secure");
        }

        if (cookie.isHttpOnly()) {
            sb.append("; HttpOnly");
        }

        return sb.toString();
    }

    public static Cookie newCookie(String key, String session) {
        return newCookie(key, session, "/", COOKIE_AGE);
    }

    public static Cookie newCookie(String key, String session, String path, int maxAge) {
        String host = ReqInfoContext.getReqInfo() == null ? "" : ReqInfoContext.getReqInfo().getHost();
        return newCookie(key, session, host, path, maxAge);
    }

    public static Cookie newCookie(String key, String session, String domain, String path, int maxAge) {
        // 移除端口号
        domain = removePortFromHost(domain);
        Cookie cookie = new Cookie(key, session);
        if (StringUtils.isNotBlank(domain)) {
            if (EnvUtil.isPro() && "127.0.0.1".equals(domain)) {
                // 说明：对于使用nginx进行转发的场景，需要设置： proxy_set_header X-Forwarded-Host $host; 否则会导致这里拿到的host为 127.0.0.1
                log.info("登录的来源：{}", ReqInfoContext.getReqInfo());
                domain = "paicoding.com";
            }
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
            // 移除端口号
            host = host.substring(0, portIndex);
        }

        // 将 www 开头的域名，移除掉开头的www
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }
        return host;
    }

    public static Cookie delCookie(String key) {
        String host = ReqInfoContext.getReqInfo() == null ? "" : ReqInfoContext.getReqInfo().getHost();
        return delCookie(key, host);
    }

    /**
     * 移除所有相关的Cookie
     *
     * @param key
     */
    public static void delCookies(String key) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Arrays.stream(request.getCookies()).filter(ck -> Objects.equals(ck.getName(), key)).forEach(ck -> {
            ck.setMaxAge(0);
            if (response != null) {
                response.addCookie(ck);
            }
        });
    }

    public static Cookie delCookie(String key, String host) {
        return delCookie(key, host, "/");
    }

    public static Cookie delCookie(String key, String host, String path) {
        Cookie cookie = new Cookie(key, null);
        cookie.setPath(path);
        if (StringUtils.isNotBlank(host)) {
            cookie.setDomain(removePortFromHost(host));
        }
        cookie.setMaxAge(0);
        return cookie;
    }

    public static void delCookie(Cookie ck) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        ck.setMaxAge(0);
        if (response != null) {
            response.addCookie(ck);
        }
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

    public static List<Cookie> findCookiesByName(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        return Arrays.stream(cookies).filter(cookie -> StringUtils.equalsAnyIgnoreCase(cookie.getName(), name)).collect(Collectors.toList());
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