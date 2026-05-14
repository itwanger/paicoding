package com.github.paicoding.forum.web.hook.filter;

import cn.hutool.core.date.StopWatch;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.core.mdc.MdcUtil;
import com.github.paicoding.forum.core.util.CrossUtil;
import com.github.paicoding.forum.core.util.EnvUtil;
import com.github.paicoding.forum.core.util.IpUtil;
import com.github.paicoding.forum.core.util.Md5Util;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.sitemap.service.SitemapService;
import com.github.paicoding.forum.service.statistics.service.StatisticsSettingService;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.web.global.GlobalInitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 1. 请求参数日志输出过滤器
 * 2. 判断用户是否登录
 *
 * @author YiHui
 * @date 2022/7/6
 */
@Slf4j
@WebFilter(urlPatterns = "/*", filterName = "reqRecordFilter", asyncSupported = true)
public class ReqRecordFilter implements Filter {
    private static Logger REQ_LOG = LoggerFactory.getLogger("req");
    /**
     * 返回给前端的traceId，用于日志追踪
     */
    private static final String GLOBAL_TRACE_ID_HEADER = "g-trace-id";
    /**
     * 客户端提示前缀，仅用于参与指纹混合；不允许直接作为设备 id 落库。
     */
    private static final String CLIENT_HINT_PREFIX = "fp-";
    private static final String DEVICE_ID_PREFIX = "sf-";

    @Autowired
    private GlobalInitService globalInitService;

    @Autowired
    private StatisticsSettingService statisticsSettingService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        HttpServletRequest request = null;
        StopWatch stopWatch = new StopWatch("请求耗时");
        try {
            stopWatch.start("请求参数构建");
            request = this.initReqInfo((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
            stopWatch.stop();
            stopWatch.start("cors");
            CrossUtil.buildCors(request, (HttpServletResponse) servletResponse);
            stopWatch.stop();
            stopWatch.start("业务执行");
            filterChain.doFilter(request, servletResponse);
        } finally {
            if (stopWatch.isRunning()) {
                // 避免doFitler执行异常，导致上面的 stopWatch无法结束，这里先首当结束一下上次的计数
                stopWatch.stop();
            }
            stopWatch.start("输出请求日志");
            buildRequestLog(ReqInfoContext.getReqInfo(), request, System.currentTimeMillis() - start);
            // 一个链路请求完毕，清空MDC相关的变量(如GlobalTraceId，用户信息)
            MdcUtil.clear();
            ReqInfoContext.clear();
            stopWatch.stop();

            if (!isStaticURI(request) && !EnvUtil.isPro()) {
                log.info("{} - cost:\n{}", request.getRequestURI(), stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
            }
        }
    }

    @Override
    public void destroy() {
    }

    private HttpServletRequest initReqInfo(HttpServletRequest request, HttpServletResponse response) {
        if (isStaticURI(request)) {
            // 静态资源直接放行
            return request;
        }

        StopWatch stopWatch = new StopWatch("请求参数构建");
        try {
            stopWatch.start("traceId");
            // 添加全链路的traceId
            MdcUtil.addTraceId();
            stopWatch.stop();

            stopWatch.start("请求基本信息");
            // 手动写入一个session，借助 OnlineUserCountListener 实现在线人数实时统计
            request.getSession().setAttribute("latestVisit", System.currentTimeMillis());

            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            String hostHeader = request.getHeader("host");
            if (StringUtils.isNotBlank(forwardedHost)) {
                // 需要配合修改nginx的转发，添加  proxy_set_header X-Forwarded-Host $host;
                reqInfo.setHost(forwardedHost);
            } else if (StringUtils.isNotBlank(hostHeader)) {
                reqInfo.setHost(hostHeader);
            } else {
                URL reqUrl = new URL(request.getRequestURL().toString());
                reqInfo.setHost(reqUrl.getHost());
            }
            reqInfo.setPath(request.getPathInfo());
            if (reqInfo.getPath() == null) {
                String url = request.getRequestURI();
                int index = url.indexOf("?");
                if (index > 0) {
                    url = url.substring(0, index);
                }
                reqInfo.setPath(url);
            }
            reqInfo.setReferer(request.getHeader("referer"));
            reqInfo.setClientIp(IpUtil.getClientIp(request));
            reqInfo.setUserAgent(request.getHeader("User-Agent"));
            reqInfo.setDeviceId(resolveRiskDeviceId(request, response));

            // 告诉浏览器后续请求带上更细的设备相关 Client Hints，让服务端指纹更可靠
            hintClientHeaders(response);

            request = this.wrapperRequest(request, reqInfo);
            stopWatch.stop();

            stopWatch.start("登录用户信息");
            // 初始化登录信息
            globalInitService.initLoginUser(reqInfo);
            stopWatch.stop();

            ReqInfoContext.addReqInfo(reqInfo);
            stopWatch.start("pv/uv站点统计");
            // 更新uv/pv计数
            AsyncUtil.execute(() -> SpringUtil.getBean(SitemapService.class).saveVisitInfo(reqInfo.getClientIp(), reqInfo.getPath()));
            stopWatch.stop();

            stopWatch.start("回写traceId");
            // 返回头中记录traceId
            response.setHeader(GLOBAL_TRACE_ID_HEADER, Optional.ofNullable(MdcUtil.getTraceId()).orElse(""));
            stopWatch.stop();
        } catch (Exception e) {
            log.error("init reqInfo error!", e);
        } finally {
            if (!EnvUtil.isPro()) {
                log.info("{} -> 请求构建耗时: \n{}", request.getRequestURI(), stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
            }
        }

        return request;
    }

    private void buildRequestLog(ReqInfoContext.ReqInfo req, HttpServletRequest request, long costTime) {
        if (req == null || isStaticURI(request)) {
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("method=").append(request.getMethod()).append("; ");
        if (StringUtils.isNotBlank(req.getReferer())) {
            msg.append("referer=").append(URLDecoder.decode(req.getReferer())).append("; ");
        }
        msg.append("remoteIp=").append(req.getClientIp());
        msg.append("; agent=").append(req.getUserAgent());

        if (req.getUserId() != null) {
            // 打印用户信息
            msg.append("; user=").append(req.getUserId());
        }

        msg.append("; uri=").append(request.getRequestURI());
        if (StringUtils.isNotBlank(request.getQueryString())) {
            msg.append('?').append(URLDecoder.decode(request.getQueryString()));
        }

        msg.append("; payload=").append(req.getPayload());
        msg.append("; cost=").append(costTime);
        REQ_LOG.info("{}", msg);

        // 保存请求计数
        statisticsSettingService.saveRequestCount(req.getClientIp());
    }


    private HttpServletRequest wrapperRequest(HttpServletRequest request, ReqInfoContext.ReqInfo reqInfo) {
        if (!HttpMethod.POST.name().equalsIgnoreCase(request.getMethod())) {
            return request;
        }

        BodyReaderHttpServletRequestWrapper requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
        reqInfo.setPayload(requestWrapper.getBodyString());
        return requestWrapper;
    }

    private boolean isStaticURI(HttpServletRequest request) {
        return request == null
                || request.getRequestURI().endsWith("css")
                || request.getRequestURI().endsWith("js")
                || request.getRequestURI().endsWith("png")
                || request.getRequestURI().endsWith("ico")
                || request.getRequestURI().endsWith("gif")
                || request.getRequestURI().endsWith("svg")
                || request.getRequestURI().endsWith("min.js.map")
                || request.getRequestURI().endsWith("min.css.map");
    }


    /**
     * 设备指纹策略：服务端 cookie 是稳定锚点（仅服务端写入的随机 UUID），
     * 再叠加服务端可观察到的 UA/语言/网段/Client Hints；客户端传入的 deviceId 只作为提示参与混合，
     * 不能直接覆盖最终值——以此防止前端任意伪造设备 id 绕过共享账号检测。
     */
    private String resolveRiskDeviceId(HttpServletRequest request, HttpServletResponse response) {
        String anchor = getOrInitDeviceAnchorCookie(request, response);
        String serverFingerprint = buildServerDeviceFingerprint(request);
        String clientHint = request.getParameter("deviceId");
        boolean validHint = StringUtils.isNotBlank(clientHint)
                && !"null".equalsIgnoreCase(clientHint)
                && StringUtils.startsWith(clientHint, CLIENT_HINT_PREFIX);

        StringBuilder mix = new StringBuilder("anchor=").append(StringUtils.defaultString(anchor));
        if (StringUtils.isNotBlank(serverFingerprint)) {
            mix.append('|').append(serverFingerprint);
        }
        if (validHint) {
            mix.append("|hint=").append(clientHint);
        }
        return DEVICE_ID_PREFIX + Md5Util.encode(mix.toString());
    }

    private String getOrInitDeviceAnchorCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = SessionUtil.findCookieByName(request, LoginService.USER_DEVICE_KEY);
        if (cookie != null && StringUtils.isNotBlank(cookie.getValue()) && !"null".equalsIgnoreCase(cookie.getValue())) {
            return cookie.getValue();
        }
        String uuid = UUID.randomUUID().toString();
        if (response != null) {
            // 走自定义 Set-Cookie，附带 Secure + SameSite=Lax，避免明文嗅探与第三方页面携带
            response.addHeader("Set-Cookie", buildDeviceAnchorCookieHeader(uuid));
        }
        return uuid;
    }

    private String buildDeviceAnchorCookieHeader(String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(LoginService.USER_DEVICE_KEY).append('=').append(value);
        sb.append("; Path=/");
        sb.append("; Max-Age=").append(31536000); // 1 year
        sb.append("; SameSite=Lax");
        if (EnvUtil.isPro()) {
            sb.append("; Secure");
        }
        return sb.toString();
    }

    private void hintClientHeaders(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        if (response.getHeader("Accept-CH") == null) {
            response.setHeader("Accept-CH", "Sec-CH-UA-Platform, Sec-CH-UA-Mobile, Sec-CH-UA-Model");
        }
        if (response.getHeader("Permissions-Policy") == null) {
            response.setHeader("Permissions-Policy", "ch-ua-platform=(self), ch-ua-mobile=(self), ch-ua-model=(self)");
        }
    }

    private String buildServerDeviceFingerprint(HttpServletRequest request) {
        List<String> factors = new ArrayList<>();
        addFactor(factors, "ua", normalizeUserAgentForDevice(request.getHeader("User-Agent")));
        addFactor(factors, "lang", normalizeAcceptLanguage(request.getHeader("Accept-Language")));
        addFactor(factors, "platform", request.getHeader("Sec-CH-UA-Platform"));
        addFactor(factors, "mobile", request.getHeader("Sec-CH-UA-Mobile"));
        addFactor(factors, "net", normalizeClientNetwork(IpUtil.getClientIp(request)));
        if (factors.isEmpty()) {
            return null;
        }
        return String.join("|", factors);
    }

    private void addFactor(List<String> factors, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            factors.add(name + "=" + value.trim().toLowerCase(Locale.ROOT));
        }
    }

    private String normalizeAcceptLanguage(String acceptLanguage) {
        if (StringUtils.isBlank(acceptLanguage)) {
            return null;
        }
        String[] segments = acceptLanguage.split(",");
        return segments.length == 0 ? null : segments[0];
    }

    private String normalizeUserAgentForDevice(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return null;
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        String os;
        if (ua.contains("windows")) {
            os = "windows";
        } else if (ua.contains("mac os x")) {
            os = "macos";
        } else if (ua.contains("android")) {
            os = "android";
        } else if (ua.contains("iphone") || ua.contains("ipad")) {
            os = "ios";
        } else if (ua.contains("linux")) {
            os = "linux";
        } else {
            os = "unknown-os";
        }

        String deviceClass;
        if (ua.contains("ipad") || ua.contains("tablet")) {
            deviceClass = "tablet";
        } else if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("android")) {
            deviceClass = "mobile";
        } else {
            deviceClass = "desktop";
        }
        return os + "/" + deviceClass;
    }

    /**
     * 取客户端 IP 的 /24 网段，避免同一办公网络下每次拨号重连导致设备 id 漂移；
     * 同一 NAT 出口下被认为是同一台设备的可能性更高一点，符合共享账号场景。
     */
    private String normalizeClientNetwork(String clientIp) {
        if (StringUtils.isBlank(clientIp)) {
            return null;
        }
        int lastDot = clientIp.lastIndexOf('.');
        if (lastDot <= 0) {
            return clientIp;
        }
        return clientIp.substring(0, lastDot) + ".0/24";
    }
}
