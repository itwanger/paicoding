package com.github.paicoding.forum.web.hook.filter;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.mdc.MdcUtil;
import com.github.paicoding.forum.core.util.CrossUtil;
import com.github.paicoding.forum.core.util.IpUtil;
import com.github.paicoding.forum.service.statistics.service.StatisticsSettingService;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Optional;

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
        try {
            request = this.initReqInfo((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
            CrossUtil.buildCors(request, (HttpServletResponse) servletResponse);
            filterChain.doFilter(request, servletResponse);
        } finally {
            buildRequestLog(ReqInfoContext.getReqInfo(), request, System.currentTimeMillis() - start);
            // 一个链路请求完毕，清空MDC相关的变量(如GlobalTraceId，用户信息)
            MdcUtil.clear();
            ReqInfoContext.clear();
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

        try {
            // 添加全链路的traceId
            MdcUtil.addTraceId();

            // 手动写入一个session，借助 OnlineUserCountListener 实现在线人数实时统计
            request.getSession().setAttribute("latestVisit", System.currentTimeMillis());

            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setHost(request.getHeader("host"));
            reqInfo.setPath(request.getPathInfo());
            reqInfo.setReferer(request.getHeader("referer"));
            reqInfo.setClientIp(IpUtil.getClientIp(request));
            reqInfo.setUserAgent(request.getHeader("User-Agent"));

            request = this.wrapperRequest(request, reqInfo);
            // 初始化登录信息
            globalInitService.initLoginUser(reqInfo);
            ReqInfoContext.addReqInfo(reqInfo);

            // 返回头中记录traceId
            response.setHeader(GLOBAL_TRACE_ID_HEADER, Optional.ofNullable(MdcUtil.getTraceId()).orElse(""));
        } catch (Exception e) {
            log.error("init reqInfo error!", e);
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
                || request.getRequestURI().endsWith("svg")
                || request.getRequestURI().endsWith("min.js.map")
                || request.getRequestURI().endsWith("min.css.map");
    }
}
