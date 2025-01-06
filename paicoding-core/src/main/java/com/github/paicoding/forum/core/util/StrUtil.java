package com.github.paicoding.forum.core.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author YiHui
 * @date 2024/12/5
 */
public class StrUtil {

    /**
     * 微信支付的提示信息，不支持表情包，因此我们只保留中文 + 数字 + 英文字母 + 符号 '《》【】-_.'
     *
     * @return
     */
    public static String pickWxSupportTxt(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        StringBuilder str = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= '\u4E00' && c <= '\u9FA5') {
                str.append(c);
            } else if (CharUtils.isAsciiAlphanumeric(c)) {
                str.append(c);
            } else if (c == '【' || c == '】' || c == '《' || c == '》' || c == '-' || c == '_' || c == '.') {
                str.append(c);
            }
        }
        return str.toString();
    }

    private static final char MID_LINE = '-';
    private static final char DOT = '.';

    /**
     * Spring的配置命名规则有要求, 若不满足时，可能出现启动异常
     * <p>
     * Reason: Canonical names should be kebab-case (’-’ separated), lowercase alpha-numeric characters, and must start with a letter。
     *
     * @return
     */
    public static String formatSpringConfigKey(String key) {
        if (null == key || key.isEmpty()) {
            return null;
        }

        int len = key.length();
        StringBuilder res = new StringBuilder(len + 2);
        char pre = 0;
        for (int i = 0; i < len; i++) {
            char ch = key.charAt(i);
            if (Character.isUpperCase(ch)) {
                // 当前为大写字母时，若前面一个是中划线/点号，则直接转为小写；否则插入一个中划线
                if (pre != MID_LINE && pre != DOT) {
                    res.append(MID_LINE);
                }
                res.append(Character.toLowerCase(ch));
            } else {
                res.append(ch);
            }
            pre = ch;
        }
        return res.toString();
    }


    public static void main(String[] args) {
        String text = "这是一个有趣的表😄过滤- 123 143 d 哒哒";
        System.out.println(pickWxSupportTxt(text));

        text = "view.site.Host";
        System.out.println(formatSpringConfigKey(text));

        text = "view.site.webHost";
        System.out.println(formatSpringConfigKey(text));

        text = "view.site.web-Host";
        System.out.println(formatSpringConfigKey(text));
    }
}
