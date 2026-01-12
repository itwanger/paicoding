package com.github.paicoding.forum.api.model.util.cdn;

/**
 * @author YiHui
 * @date 2026/1/12
 */
public class CdnUtil {

    private static final String TO_REPLACE_CDN = "https://cdn.tobebetterjavaer.com";
    private static final String TARGET_REPLACE_CDN = "https://cdn.paicoding.com";

    /**
     * 自动替换成新的CDN
     *
     * @param val
     * @return
     */
    public static String autoTransCdn(String val) {
        if (val.startsWith(TO_REPLACE_CDN)) {
            return val.replace(TO_REPLACE_CDN, TARGET_REPLACE_CDN);
        }
        return val;
    }

}
