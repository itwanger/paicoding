package com.github.paicoding.forum.web.hook.filter;

import com.github.paicoding.forum.web.config.GlobalViewConfig;
import org.apache.commons.lang3.StringUtils;
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
public class CanonicalUrlRedirectFilter extends OncePerRequestFilter {
    private static final Pattern JSESSIONID_PATTERN = Pattern.compile(";jsessionid=[^/?]*", Pattern.CASE_INSENSITIVE);

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

        if (StringUtils.isNotBlank(canonicalHost)
                && StringUtils.equalsIgnoreCase(request.getServerName(), "www." + canonicalHost)) {
            needRedirect = true;
        }

        if (!needRedirect) {
            filterChain.doFilter(request, response);
            return;
        }

        StringBuilder target = new StringBuilder(canonicalBase).append(redirectPath);
        if (StringUtils.isNotBlank(redirectQuery)) {
            target.append('?').append(redirectQuery);
        }

        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.setHeader("Location", target.toString());
    }

    private String normalizePath(String requestPath) {
        String normalized = JSESSIONID_PATTERN.matcher(StringUtils.defaultIfBlank(requestPath, "/")).replaceAll("");
        return StringUtils.isBlank(normalized) ? "/" : normalized;
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
