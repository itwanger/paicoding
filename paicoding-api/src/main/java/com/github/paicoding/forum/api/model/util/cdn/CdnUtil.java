package com.github.paicoding.forum.api.model.util.cdn;

import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;

/**
 * @author YiHui
 * @date 2026/1/12
 */
public class CdnUtil {

    private static final String TO_REPLACE_CDN = "https://cdn.tobebetterjavaer.com";
    private static final String TARGET_REPLACE_CDN = "https://cdn.paicoding.com";
    private static volatile String siteOssPrefix = "";

    public static void setSiteOssPrefix(String ossPrefix) {
        if (ossPrefix == null) {
            siteOssPrefix = "";
            return;
        }
        siteOssPrefix = trimTrailingSlash(ossPrefix.trim());
    }

    /**
     * 自动替换成新的CDN
     *
     * @param val
     * @return
     */
    public static String autoTransCdn(String val) {
        if (val == null || val.isEmpty()) {
            return val;
        }
//  备案完成，无需做域名替换
//        if (val.startsWith(TO_REPLACE_CDN)) {
//            return val.replace(TO_REPLACE_CDN, TARGET_REPLACE_CDN);
//        }
        String botAvatar = rewriteBotAvatar(val);
        if (botAvatar != null) {
            return botAvatar;
        }
        return val;
    }

    private static String rewriteBotAvatar(String val) {
        if (siteOssPrefix.isEmpty()) {
            return null;
        }
        for (AiBotEnum bot : AiBotEnum.values()) {
            String avatarPath = bot.getAvatar();
            if (matchesStaticAvatar(val, avatarPath)) {
                return join(siteOssPrefix, avatarPath);
            }
        }
        return null;
    }

    private static boolean matchesStaticAvatar(String val, String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            return false;
        }
        return avatarPath.equals(val) || val.endsWith("/static" + avatarPath);
    }

    private static String join(String prefix, String path) {
        boolean prefixEndsWithSlash = prefix.endsWith("/");
        boolean pathStartsWithSlash = path.startsWith("/");
        if (prefixEndsWithSlash && pathStartsWithSlash) {
            return prefix.substring(0, prefix.length() - 1) + path;
        }
        if (!prefixEndsWithSlash && !pathStartsWithSlash) {
            return prefix + "/" + path;
        }
        return prefix + path;
    }

    private static String trimTrailingSlash(String val) {
        if (val.endsWith("/")) {
            return val.substring(0, val.length() - 1);
        }
        return val;
    }
}
