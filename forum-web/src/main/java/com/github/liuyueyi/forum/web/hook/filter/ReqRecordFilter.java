package com.github.liuyueyi.forum.web.hook.filter;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liuyueyi.forum.core.util.CrossUtil;
import com.github.liuyueyi.forum.core.util.IpUtil;
import com.github.liuyueyi.forum.web.global.GlobalInitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * 1. 请求参数日志输出过滤器
 * 2. 判断用户是否登录
 *
 * @author YiHui
 * @date 2022/7/6
 */
@Slf4j
@WebFilter(urlPatterns = "/*", filterName = "reqRecordFilter")
public class ReqRecordFilter implements Filter {
    private static Logger REQ_LOG = LoggerFactory.getLogger("req");

    @Autowired
    private GlobalInitService globalInitService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        HttpServletRequest request = null;
        try {
            request = this.initReqInfo((HttpServletRequest) servletRequest);
            CrossUtil.buildCors(request, (HttpServletResponse) servletResponse);
            filterChain.doFilter(request, servletResponse);
        } finally {
            buildRequestLog(ReqInfoContext.getReqInfo(), request, System.currentTimeMillis() - start);
            ReqInfoContext.clear();
        }
    }

    @Override
    public void destroy() {
    }

    private HttpServletRequest initReqInfo(HttpServletRequest request) {
        try {
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
        } catch (Exception e) {
            log.error("init reqInfo error!", e);
        }

        return request;
    }

    private void buildRequestLog(ReqInfoContext.ReqInfo req, HttpServletRequest request, long costTime) {
        // fixme 过滤不需要记录请求日志的场景
        if (request == null
                || request.getRequestURI().endsWith("css")
                || request.getRequestURI().endsWith("js")
                || request.getRequestURI().endsWith("png")
                || request.getRequestURI().endsWith("ico")) {
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
    }


    private HttpServletRequest wrapperRequest(HttpServletRequest request, ReqInfoContext.ReqInfo reqInfo) {
        if (!HttpMethod.POST.name().equalsIgnoreCase(request.getMethod())) {
            return request;
        }

        BodyReaderHttpServletRequestWrapper requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
        reqInfo.setPayload(requestWrapper.getBodyString());
        return requestWrapper;
    }

}
