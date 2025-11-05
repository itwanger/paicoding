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


    /**
     * 安全地截取HTML内容，确保标签完整性
     *
     * @param html      原始HTML内容
     * @param maxLength 截取长度
     * @return 截取后的HTML内容
     */
    public static String safeSubstringHtml(String html, int maxLength) {
        if (html == null || html.length() <= maxLength) {
            return html;
        }

        try {
            // 使用Jsoup解析HTML
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parseBodyFragment(html);
            org.jsoup.nodes.Element body = doc.body();

            // 递归截取内容直到达到指定长度
            StringBuilder result = new StringBuilder();
            truncateElement(body, result, maxLength);

            return result.toString();
        } catch (Exception e) {
            // 降级处理
            String subContent = html.substring(0, maxLength);
            int lastTagEnd = subContent.lastIndexOf('>');
            if (lastTagEnd > 0 && subContent.lastIndexOf('<') > lastTagEnd) {
                // 存在未闭合标签，截断到最近的完整标签
                return subContent.substring(0, lastTagEnd + 1) + "...";
            }
            return subContent + "...";
        }
    }

    private static void truncateElement(org.jsoup.nodes.Element element, StringBuilder result, int maxLength) {
        if (result.length() >= maxLength) {
            return;
        }

        for (org.jsoup.nodes.Node node : element.childNodes()) {
            if (result.length() >= maxLength) {
                break;
            }

            if (node instanceof org.jsoup.nodes.TextNode) {
                org.jsoup.nodes.TextNode textNode = (org.jsoup.nodes.TextNode) node;
                String text = textNode.getWholeText();
                int availableLength = maxLength - result.length();
                if (text.length() > availableLength) {
                    result.append(text, 0, availableLength).append("...");
                    break;
                } else {
                    result.append(text);
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
                truncateElement(child, result, maxLength);

                // 添加闭合标签
                if (!child.tag().isSelfClosing()) {
                    result.append("</").append(tagName).append(">");
                }
            }
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
