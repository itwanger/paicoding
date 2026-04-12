package com.github.paicoding.forum.core.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.math.NumberUtils;
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


    /**
     * 安全地截取HTML内容，确保标签完整性
     *
     * @param html      原始HTML内容
     * @param maxLength 可见文本截取长度
     * @return 截取后的HTML内容
     */
    public static String safeSubstringHtml(String html, int maxLength) {
        if (html == null) {
            return html;
        }
        if (maxLength <= 0) {
            return "";
        }

        try {
            // 使用Jsoup解析HTML
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parseBodyFragment(html);
            org.jsoup.nodes.Element body = doc.body();
            if (body.text().length() <= maxLength) {
                return html;
            }

            // 递归截取内容直到达到指定长度
            StringBuilder result = new StringBuilder();
            truncateElement(body, result, new HtmlTruncateState(maxLength));

            return result.toString();
        } catch (Exception e) {
            // 降级处理
            if (html.length() <= maxLength) {
                return html;
            }
            String subContent = html.substring(0, maxLength);
            int lastTagEnd = subContent.lastIndexOf('>');
            if (lastTagEnd > 0 && subContent.lastIndexOf('<') > lastTagEnd) {
                // 存在未闭合标签，截断到最近的完整标签
                return subContent.substring(0, lastTagEnd + 1) + "...";
            }
            return subContent + "...";
        }
    }

    /**
     * 兼容两种配置方式：
     * 1. 纯数字：按可见文本字数截断，如 "300"
     * 2. 百分比：按可见文本百分比截断，如 "20%"
     */
    public static String safeSubstringHtml(String html, String lengthConfig) {
        return safeSubstringHtml(html, resolvePreviewLength(html, lengthConfig));
    }

    private static int resolvePreviewLength(String html, String lengthConfig) {
        if (StringUtils.isBlank(html)) {
            return 0;
        }
        if (StringUtils.isBlank(lengthConfig)) {
            return html.length();
        }

        String config = lengthConfig.trim();
        int visibleTextLength = org.jsoup.Jsoup.parseBodyFragment(html).body().text().length();
        if (visibleTextLength <= 0) {
            return 0;
        }

        if (config.endsWith("%")) {
            double percent = NumberUtils.toDouble(config.substring(0, config.length() - 1), 100D);
            if (percent <= 0) {
                return 0;
            }
            if (percent >= 100) {
                return visibleTextLength;
            }
            return Math.max(1, (int) Math.floor(visibleTextLength * percent / 100D));
        }

        int maxLength = NumberUtils.toInt(config, visibleTextLength);
        if (maxLength <= 0) {
            return 0;
        }
        return Math.min(maxLength, visibleTextLength);
    }

    private static void truncateElement(org.jsoup.nodes.Element element, StringBuilder result, HtmlTruncateState state) {
        if (state.remainingLength <= 0) {
            return;
        }

        for (org.jsoup.nodes.Node node : element.childNodes()) {
            if (state.remainingLength <= 0) {
                break;
            }

            if (node instanceof org.jsoup.nodes.TextNode) {
                org.jsoup.nodes.TextNode textNode = (org.jsoup.nodes.TextNode) node;
                String text = textNode.getWholeText();
                if (text.length() > state.remainingLength) {
                    result.append(text, 0, state.remainingLength).append("...");
                    state.remainingLength = 0;
                    break;
                } else {
                    result.append(text);
                    state.remainingLength -= text.length();
                }
            } else if (node instanceof org.jsoup.nodes.Element) {
                org.jsoup.nodes.Element child = (org.jsoup.nodes.Element) node;
                String tagName = child.tagName();
                result.append("<").append(tagName);

                // 添加属性
                for (org.jsoup.nodes.Attribute attr : child.attributes()) {
                    result.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
                }
                result.append(">");

                // 递归处理子元素
                truncateElement(child, result, state);

                // 添加闭合标签
                if (!child.tag().isSelfClosing()) {
                    result.append("</").append(tagName).append(">");
                }
            }
        }
    }

    private static class HtmlTruncateState {
        private int remainingLength;

        private HtmlTruncateState(int remainingLength) {
            this.remainingLength = remainingLength;
        }
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
