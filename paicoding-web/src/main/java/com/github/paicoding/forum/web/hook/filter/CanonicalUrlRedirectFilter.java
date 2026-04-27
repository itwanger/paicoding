package com.github.paicoding.forum.web.hook.filter;

import com.github.paicoding.forum.web.config.GlobalViewConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriUtils;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 统一 SEO 入口 URL，避免搜索引擎抓到 www、jsessionid、首页分类 query 这些重复地址。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CanonicalUrlRedirectFilter extends OncePerRequestFilter {
    private static final Pattern JSESSIONID_PATTERN = Pattern.compile(";jsessionid=[^/?]*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ARTICLE_ID_WITH_SUFFIX_PATTERN = Pattern.compile("^/article/detail/(\\d+)\\D[^/]*$");
    private static final Pattern NESTED_ARTICLE_DETAIL_PATTERN = Pattern.compile("^/article/detail/(\\d+)/article/detail/\\d+.*$");
    private static final Pattern RELATIVE_ARTICLE_DETAIL_PATTERN = Pattern.compile("^/(?:article|column|rank|user)(?:/[^?]*)?/article/detail/(\\d+).*$");
    private static final Pattern USER_HOME_PATH_PATTERN = Pattern.compile("^/user/\\d+$");

    @Resource
    private GlobalViewConfig globalViewConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!"GET".equalsIgnoreCase(request.getMethod()) && !"HEAD".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String canonicalBase = StringUtils.removeEnd(StringUtils.defaultString(globalViewConfig.getHost()), "/");
        if (StringUtils.isBlank(canonicalBase)) {
            filterChain.doFilter(request, response);
            return;
        }

        String canonicalHost = extractHost(canonicalBase);
        String originalPath = StringUtils.defaultIfBlank(request.getRequestURI(), "/");
        String normalizedPath = normalizePath(originalPath);
        String redirectPath = normalizedPath;
        String redirectQuery = request.getQueryString();
        boolean needRedirect = !StringUtils.equals(originalPath, normalizedPath);
        boolean dropQuery = false;

        String sanitizedPath = sanitizeArticleDetailPath(normalizedPath);
        if (!StringUtils.equals(sanitizedPath, normalizedPath)) {
            redirectPath = sanitizedPath;
            normalizedPath = sanitizedPath;
            needRedirect = true;
            dropQuery = true;
        }

        if ("/".equals(normalizedPath)) {
            String category = request.getParameter("category");
            if (StringUtils.isNotBlank(category)) {
                needRedirect = true;
                if ("全部".equals(category)) {
                    redirectPath = "/";
                } else {
                    redirectPath = "/article/category/" + UriUtils.encodePathSegment(category, StandardCharsets.UTF_8);
                }
                redirectQuery = null;
            }
        }

        if (USER_HOME_PATH_PATTERN.matcher(normalizedPath).matches() && isUserStateQuery(redirectQuery)) {
            response.setHeader("X-Robots-Tag", "noindex, follow");
        }

        if (StringUtils.isNotBlank(canonicalHost)
                && StringUtils.equalsIgnoreCase(request.getServerName(), "www." + canonicalHost)) {
            needRedirect = true;
        }

        if (!needRedirect) {
            filterChain.doFilter(request, response);
            return;
        }

        StringBuilder target = new StringBuilder(canonicalBase).append(redirectPath);
        if (!dropQuery && StringUtils.isNotBlank(redirectQuery)) {
            target.append('?').append(redirectQuery);
        }

        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.setHeader("Location", target.toString());
    }

    private String normalizePath(String requestPath) {
        String normalized = JSESSIONID_PATTERN.matcher(StringUtils.defaultIfBlank(requestPath, "/")).replaceAll("");
        if (StringUtils.equalsAnyIgnoreCase(normalized, "/home.html", "/index.html")) {
            return "/";
        }
        return StringUtils.isBlank(normalized) ? "/" : normalized;
    }

    private String sanitizeArticleDetailPath(String requestPath) {
        java.util.regex.Matcher nestedMatcher = NESTED_ARTICLE_DETAIL_PATTERN.matcher(requestPath);
        if (nestedMatcher.matches()) {
            return "/article/detail/" + nestedMatcher.group(1);
        }

        java.util.regex.Matcher relativeMatcher = RELATIVE_ARTICLE_DETAIL_PATTERN.matcher(requestPath);
        if (relativeMatcher.matches()) {
            return "/article/detail/" + relativeMatcher.group(1);
        }

        java.util.regex.Matcher suffixMatcher = ARTICLE_ID_WITH_SUFFIX_PATTERN.matcher(requestPath);
        if (suffixMatcher.matches()) {
            return "/article/detail/" + suffixMatcher.group(1);
        }

        return requestPath;
    }

    private boolean isUserStateQuery(String query) {
        return StringUtils.contains(query, "homeSelectType=")
                || StringUtils.contains(query, "followSelectType=")
                || StringUtils.contains(query, "userId=");
    }

    private String extractHost(String canonicalBase) {
        try {
            URI uri = URI.create(canonicalBase);
            return uri.getHost();
        } catch (Exception e) {
            return null;
        }
    }
}
