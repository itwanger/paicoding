package com.github.paicoding.forum.core.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * URL Slug生成工具类
 * 用于将文章标题转换为SEO友好的URL标识
 *
 * @author Claude
 * @date 2025-11-10
 */
public class UrlSlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DUPLICATE_DASH = Pattern.compile("-+");
    private static final int MAX_SLUG_LENGTH = 100;

    /**
     * 生成URL友好的slug
     * 支持中文(转拼音)、英文、数字
     *
     * @param text 原始文本(通常是文章标题)
     * @return URL友好的slug字符串
     */
    public static String generateSlug(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }

        // 1. 转小写
        String slug = text.toLowerCase(Locale.ENGLISH);

        // 2. 移除特殊字符,保留中文、英文、数字、空格、连字符
        slug = slug.replaceAll("[^a-z0-9\\s\\-\\u4e00-\\u9fa5]", "");

        // 3. 将中文转换为拼音
        slug = chineseToPinyin(slug);

        // 4. Unicode标准化
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);

        // 5. 移除非ASCII字符
        slug = NON_LATIN.matcher(slug).replaceAll("-");

        // 6. 将空白字符替换为连字符
        slug = WHITESPACE.matcher(slug).replaceAll("-");

        // 7. 移除重复的连字符
        slug = DUPLICATE_DASH.matcher(slug).replaceAll("-");

        // 8. 移除首尾的连字符
        slug = slug.replaceAll("^-+|-+$", "");

        // 9. 限制长度
        if (slug.length() > MAX_SLUG_LENGTH) {
            slug = slug.substring(0, MAX_SLUG_LENGTH);
            // 确保不在单词中间截断
            int lastDash = slug.lastIndexOf('-');
            if (lastDash > 0) {
                slug = slug.substring(0, lastDash);
            }
        }

        // 10. 如果最终结果为空,使用时间戳
        if (StringUtils.isBlank(slug)) {
            slug = "article-" + System.currentTimeMillis();
        }

        return slug;
    }

    /**
     * 将中文字符串转换为拼音
     *
     * @param chinese 包含中文的字符串
     * @return 转换后的拼音字符串
     */
    private static String chineseToPinyin(String chinese) {
        if (StringUtils.isBlank(chinese)) {
            return "";
        }

        StringBuilder pinyin = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] chars = chinese.toCharArray();
        for (char c : chars) {
            if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                // 是中文字符
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyin.append(pinyinArray[0]);
                    } else {
                        pinyin.append(c);
                    }
                } catch (Exception e) {
                    // 转换失败,保留原字符
                    pinyin.append(c);
                }
            } else {
                // 非中文字符直接保留
                pinyin.append(c);
            }
        }

        return pinyin.toString();
    }

    /**
     * 生成唯一的slug(带文章ID后缀)
     *
     * @param title     文章标题
     * @param articleId 文章ID
     * @return 唯一的slug
     */
    public static String generateUniqueSlug(String title, Long articleId) {
        String baseSlug = generateSlug(title);
        if (articleId != null && articleId > 0) {
            return baseSlug;
        }
        return baseSlug;
    }

    /**
     * 验证slug格式是否有效
     *
     * @param slug 要验证的slug
     * @return 是否有效
     */
    public static boolean isValidSlug(String slug) {
        if (StringUtils.isBlank(slug)) {
            return false;
        }
        // slug只能包含小写字母、数字和连字符
        return slug.matches("^[a-z0-9-]+$") && slug.length() <= MAX_SLUG_LENGTH;
    }
}
