package com.github.paicoding.forum.service.shortlink.help;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceDetector {

    private static final String MOBILE_PATTERN = "(Android|iPhone|iPad|iPod|Windows Phone|Mobile)";
    private static final String DESKTOP_PATTERN = "(Windows NT|Macintosh|Linux)";
    private static final String BOT_PATTERN = "(bot|spider|crawler|curl|wget)";

    /**
     * 根据 User-Agent 和 Referer 判断请求来源
     *
     * @return 来源字符串 (WeChat, QQ, Mobile, Desktop, Bot, Unknown)
     */
    public static String detectSource() {
        String userAgent = ReqInfoContext.getReqInfo().getUserAgent();
        String referer = ReqInfoContext.getReqInfo().getReferer();

        // 1. 优先判断 Referer (更可靠，但可能为空)
        if (referer != null && !referer.isEmpty()) {
            if (referer.contains("servicewechat.com") || referer.contains("weixin")) {
                return "WeChat";
            } else if (referer.contains("qq.com") || referer.contains("mobile.qq.com") || referer.contains("connect.qq.com")) {
                return "QQ";
            }
        }

        // 2. 如果 Referer 无法判断，则根据 User-Agent 判断
        if (userAgent != null && !userAgent.isEmpty()) {

            // 2.1 微信 (User-Agent 中包含 MicroMessenger)
            if (userAgent.contains("MicroMessenger")) {
                return "WeChat";
            }

            // 2.2 QQ (User-Agent 中包含 QQ 或 MQQBrowser)
            Pattern qqPattern = Pattern.compile("(QQ|MQQBrowser)", Pattern.CASE_INSENSITIVE);
            Matcher qqMatcher = qqPattern.matcher(userAgent);
            if (qqMatcher.find()) {
                return "QQ";
            }

            // 2.3 浏览器判断 (常见浏览器 User-Agent 特征)
            if (userAgent.contains("Edg")) {  //Edge 浏览器需要在 Chrome 之前判断，因为Edge的UA字符串也包含 Chrome
                return "Edge";
            } else if (userAgent.contains("Chrome")) {
                return "Chrome";
            } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome") && !userAgent.contains("Edg")) { // 排除 Chrome 和 Edge
                return "Safari";
            } else if (userAgent.contains("Firefox")) {
                return "Firefox";
            } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
                return "IE"; // Internet Explorer
            } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
                return "Opera";
            }

            // 2.3 移动设备 (常见移动设备 User-Agent 特征)
            Pattern mobilePattern = Pattern.compile(MOBILE_PATTERN, Pattern.CASE_INSENSITIVE); // 增加更多移动设备
            Matcher mobileMatcher = mobilePattern.matcher(userAgent);
            if (mobileMatcher.find()) {
                return "Mobile";
            }


            // 2.4 桌面设备 (常见桌面设备 User-Agent 特征)
            Pattern desktopPattern = Pattern.compile(DESKTOP_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher desktopMatcher = desktopPattern.matcher(userAgent);
            if (desktopMatcher.find()) {
                return "Desktop";
            }

            // 2.5 爬虫/机器人 (常见爬虫 User-Agent 特征)
            Pattern botPattern = Pattern.compile(BOT_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher botMatcher = botPattern.matcher(userAgent);
            if (botMatcher.find()) {
                return "Bot";
            }

        }

        // 3. 无法识别
        return "Unknown";
    }
}
