package com.github.liuyueyi.forum.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author YiHui
 * @date 2022/7/6
 */
@Slf4j
public class IpUtil {
    private static final String UNKNOWN = "unKnown";

    public static String getClientIp(HttpServletRequest request) {
        try {
            String xIp = request.getHeader("X-Real-IP");
            String xFor = request.getHeader("X-Forwarded-For");
            if (StringUtils.isNotEmpty(xFor) && !UNKNOWN.equalsIgnoreCase(xFor)) {
                //多次反向代理后会有多个ip值，第一个ip才是真实ip
                int index = xFor.indexOf(",");
                if (index != -1) {
                    return xFor.substring(0, index);
                } else {
                    return xFor;
                }
            }
            xFor = xIp;
            if (StringUtils.isNotEmpty(xFor) && !UNKNOWN.equalsIgnoreCase(xFor)) {
                return xFor;
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getRemoteAddr();
            }
            return xFor;
        } catch (Exception e) {
            log.error("get remote ip error!", e);
            return "x.0.0.1";
        }
    }

}
