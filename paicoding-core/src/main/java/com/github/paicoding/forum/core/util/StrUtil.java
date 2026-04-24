package com.github.paicoding.forum.core.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YiHui
 * @date 2024/12/5
 */
public class StrUtil {
    private static final String IMAGE_DIMENSION_CACHE_VERSION = "text-size-v3:";
    private static final Cache<String, ImageDimension> IMAGE_DIMENSION_CACHE = Caffeine.newBuilder()
            .maximumSize(2048)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();
    private static final Cache<String, Boolean> IMAGE_DIMENSION_MISS_CACHE = Caffeine.newBuilder()
            .maximumSize(4096)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();
    private static final Cache<String, Boolean> IMAGE_DIMENSION_WARMING_CACHE = Caffeine.newBuilder()
            .maximumSize(4096)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();
    private static final AtomicInteger IMAGE_PROBE_THREAD_INDEX = new AtomicInteger(1);
    private static final ExecutorService IMAGE_DIMENSION_PROBE_EXECUTOR = new ThreadPoolExecutor(
            1,
            2,
            30,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(256),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "paicoding-image-probe-" + IMAGE_PROBE_THREAD_INDEX.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.DiscardPolicy());
    private static final int IMAGE_DIMENSION_PROBE_LIMIT = 3;
    private static final int IMAGE_DIMENSION_TIMEOUT_MS = 1500;
    private static final int TEXT_SCREENSHOT_TARGET_TEXT_HEIGHT = 24;
    private static final int TEXT_SCREENSHOT_MIN_TEXT_HEIGHT = 28;
    private static final int TEXT_SCREENSHOT_MIN_RENDER_WIDTH = 560;
    private static final int TEXT_SCREENSHOT_MAX_RENDER_WIDTH = 960;
    private static final int TEXT_SCREENSHOT_DOCUMENT_RENDER_WIDTH = 960;
    private static final int TEXT_SCREENSHOT_COMPACT_POSTER_WIDTH = 420;
    private static final int TEXT_SCREENSHOT_MAX_ANALYSIS_PIXELS = 4_000_000;

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

    /**
     * 为缺少宽高的正文图片增加稳定占位，降低首屏图片加载导致的布局偏移。
     */
    public static String stabilizeHtmlImages(String html) {
        if (StringUtils.isBlank(html)) {
            return html;
        }

        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parseBodyFragment(html);
            org.jsoup.select.Elements images = doc.select("img");
            int remainingProbeCount = IMAGE_DIMENSION_PROBE_LIMIT;
            int imageIndex = 0;
            for (org.jsoup.nodes.Element img : images) {
                stabilizeImageLoadingAttrs(img, imageIndex++);

                String src = normalizeImageSrc(img.attr("src"));
                ImageDimension dimension = null;
                if (StringUtils.isNotBlank(src)) {
                    String cacheKey = buildImageDimensionCacheKey(src);
                    dimension = IMAGE_DIMENSION_CACHE.getIfPresent(cacheKey);
                    if (dimension == null && IMAGE_DIMENSION_MISS_CACHE.getIfPresent(cacheKey) == null && remainingProbeCount > 0) {
                        remainingProbeCount--;
                        dimension = loadImageDimension(src);
                        if (dimension != null) {
                            IMAGE_DIMENSION_CACHE.put(cacheKey, dimension);
                        } else {
                            IMAGE_DIMENSION_MISS_CACHE.put(cacheKey, Boolean.TRUE);
                        }
                    } else if (dimension == null) {
                        warmImageDimension(src, cacheKey);
                    }
                }

                if (dimension != null) {
                    img.attr("width", String.valueOf(dimension.width));
                    img.attr("height", String.valueOf(dimension.height));
                    img.removeClass("article-content-img--pending-size");
                    markReadableImageClass(img, dimension);
                    continue;
                }

                boolean missingWidth = !img.hasAttr("width");
                boolean missingHeight = !img.hasAttr("height");
                if (missingWidth || missingHeight) {
                    if (!img.classNames().contains("article-content-img--pending-size")) {
                        img.addClass("article-content-img--pending-size");
                    }
                }
            }
            return doc.body().html();
        } catch (Exception e) {
            return html;
        }
    }

    private static void stabilizeImageLoadingAttrs(org.jsoup.nodes.Element img, int imageIndex) {
        if (!img.hasAttr("decoding")) {
            img.attr("decoding", "async");
        }

        if (imageIndex > 0 && !img.hasAttr("loading")) {
            img.attr("loading", "lazy");
        }
    }

    private static void markReadableImageClass(org.jsoup.nodes.Element img, ImageDimension dimension) {
        int maxDisplayWidth = dimension.maxDisplayWidth;
        if (looksLikeCompactPoster(img, dimension)) {
            img.addClass("article-content-img--compact-poster");
            maxDisplayWidth = Math.min(dimension.width, TEXT_SCREENSHOT_COMPACT_POSTER_WIDTH);
        }

        if (maxDisplayWidth > 0) {
            img.addClass("article-content-img--text-shot");
            appendStyle(img, "--article-img-max-width: " + maxDisplayWidth + "px");
        }
    }

    private static boolean looksLikeCompactPoster(org.jsoup.nodes.Element img, ImageDimension dimension) {
        if (dimension.width <= 0 || dimension.height <= dimension.width) {
            return false;
        }

        String signal = img.attr("alt") + " " + img.attr("src");
        return dimension.width <= 1200
                && dimension.height >= dimension.width * 1.05D
                && containsAny(signal, "扫码", "二维码", "长按识别", "优惠券", "微信群", "交流群", "微信");
    }

    private static boolean containsAny(String source, String... targets) {
        if (StringUtils.isBlank(source)) {
            return false;
        }

        for (String target : targets) {
            if (source.contains(target)) {
                return true;
            }
        }
        return false;
    }

    private static void appendStyle(org.jsoup.nodes.Element img, String style) {
        String oldStyle = img.attr("style");
        if (StringUtils.isBlank(oldStyle)) {
            img.attr("style", style + ";");
            return;
        }

        String normalizedStyle = oldStyle.trim();
        if (!normalizedStyle.endsWith(";")) {
            normalizedStyle += ";";
        }
        img.attr("style", normalizedStyle + " " + style + ";");
    }

    private static String normalizeImageSrc(String src) {
        if (StringUtils.isBlank(src)) {
            return null;
        }

        String target = src.trim();
        if (target.startsWith("//")) {
            return "https:" + target;
        }
        if (target.startsWith("http://") || target.startsWith("https://")) {
            return encodeImageUrl(target);
        }
        return null;
    }

    private static String encodeImageUrl(String src) {
        if (StringUtils.isAsciiPrintable(src)) {
            return src;
        }

        try {
            URL url = new URL(src);
            return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString();
        } catch (Exception e) {
            return src;
        }
    }

    private static String buildImageDimensionCacheKey(String src) {
        return IMAGE_DIMENSION_CACHE_VERSION + src;
    }

    private static ImageDimension loadImageDimension(String src) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(src).openConnection();
            connection.setConnectTimeout(IMAGE_DIMENSION_TIMEOUT_MS);
            connection.setReadTimeout(IMAGE_DIMENSION_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 PaiCoding-SEO-ImageProbe");

            try (InputStream inputStream = connection.getInputStream();
                 ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
                if (imageInputStream == null) {
                    return null;
                }

                Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
                if (!readers.hasNext()) {
                    return null;
                }

                ImageReader reader = readers.next();
                try {
                    reader.setInput(imageInputStream, true, true);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    if (width > 0 && height > 0) {
                        return new ImageDimension(width, height, detectReadableImageMaxWidth(reader, width, height));
                    }
                    return null;
                } finally {
                    reader.dispose();
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static int detectReadableImageMaxWidth(ImageReader reader, int width, int height) {
        if ((long) width * height > TEXT_SCREENSHOT_MAX_ANALYSIS_PIXELS) {
            return 0;
        }

        try {
            BufferedImage image = reader.read(0);
            ImageTextProfile textProfile = estimateImageTextProfile(image);
            if (looksLikeDocumentScreenshot(width, height, textProfile)) {
                return Math.min(width, TEXT_SCREENSHOT_DOCUMENT_RENDER_WIDTH);
            }

            int estimatedTextHeight = textProfile.maxTextHeight;
            if (estimatedTextHeight < TEXT_SCREENSHOT_MIN_TEXT_HEIGHT) {
                return 0;
            }

            int maxDisplayWidth = width * TEXT_SCREENSHOT_TARGET_TEXT_HEIGHT / estimatedTextHeight;
            maxDisplayWidth = Math.max(TEXT_SCREENSHOT_MIN_RENDER_WIDTH, maxDisplayWidth);
            maxDisplayWidth = Math.min(TEXT_SCREENSHOT_MAX_RENDER_WIDTH, maxDisplayWidth);
            maxDisplayWidth = Math.min(width, maxDisplayWidth);
            return maxDisplayWidth < width * 0.92D ? maxDisplayWidth : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean looksLikeDocumentScreenshot(int width, int height, ImageTextProfile textProfile) {
        return width >= 1600
                && height >= 1000
                && textProfile.textGroupCount >= 10
                && textProfile.medianTextHeight > 0
                && textProfile.medianTextHeight <= TEXT_SCREENSHOT_MIN_TEXT_HEIGHT
                && textProfile.maxTextHeight >= textProfile.medianTextHeight * 3;
    }

    private static ImageTextProfile estimateImageTextProfile(BufferedImage image) {
        if (image == null) {
            return ImageTextProfile.empty();
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int sampleStep = Math.max(1, width / 1200);
        int sampleWidth = (width + sampleStep - 1) / sampleStep;
        int minInkPerRow = Math.max(8, sampleWidth / 100);
        int maxInkPerRow = Math.max(minInkPerRow + 1, sampleWidth * 65 / 100);
        List<Integer> textHeights = new ArrayList<>();
        int start = -1;

        for (int y = 0; y < height; y++) {
            int inkCount = countDarkPixelsInRow(image, y, sampleStep);
            boolean looksLikeTextRow = inkCount >= minInkPerRow && inkCount <= maxInkPerRow;
            if (looksLikeTextRow) {
                if (start < 0) {
                    start = y;
                }
            } else if (start >= 0) {
                collectTextHeight(textHeights, y - start);
                start = -1;
            }
        }

        if (start >= 0) {
            collectTextHeight(textHeights, height - start);
        }
        if (textHeights.isEmpty()) {
            return ImageTextProfile.empty();
        }

        Collections.sort(textHeights);
        return new ImageTextProfile(
                textHeights.get(textHeights.size() - 1),
                textHeights.get(textHeights.size() / 2),
                textHeights.size());
    }

    private static void collectTextHeight(List<Integer> textHeights, int height) {
        if (height >= 8 && height <= 140) {
            textHeights.add(height);
        }
    }

    private static int countDarkPixelsInRow(BufferedImage image, int y, int sampleStep) {
        int width = image.getWidth();
        int count = 0;
        for (int x = 0; x < width; x += sampleStep) {
            int rgb = image.getRGB(x, y);
            int red = (rgb >> 16) & 0xff;
            int green = (rgb >> 8) & 0xff;
            int blue = rgb & 0xff;
            int luminance = (red * 299 + green * 587 + blue * 114) / 1000;
            if (luminance < 125) {
                count++;
            }
        }
        return count;
    }

    private static void warmImageDimension(String src, String cacheKey) {
        if (IMAGE_DIMENSION_CACHE.getIfPresent(cacheKey) != null
                || IMAGE_DIMENSION_MISS_CACHE.getIfPresent(cacheKey) != null
                || IMAGE_DIMENSION_WARMING_CACHE.getIfPresent(cacheKey) != null) {
            return;
        }

        IMAGE_DIMENSION_WARMING_CACHE.put(cacheKey, Boolean.TRUE);
        IMAGE_DIMENSION_PROBE_EXECUTOR.execute(() -> {
            try {
                ImageDimension dimension = loadImageDimension(src);
                if (dimension != null) {
                    IMAGE_DIMENSION_CACHE.put(cacheKey, dimension);
                } else {
                    IMAGE_DIMENSION_MISS_CACHE.put(cacheKey, Boolean.TRUE);
                }
            } finally {
                IMAGE_DIMENSION_WARMING_CACHE.invalidate(cacheKey);
            }
        });
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

    private static class ImageDimension {
        private final int width;
        private final int height;
        private final int maxDisplayWidth;

        private ImageDimension(int width, int height, int maxDisplayWidth) {
            this.width = width;
            this.height = height;
            this.maxDisplayWidth = maxDisplayWidth;
        }
    }

    private static class ImageTextProfile {
        private final int maxTextHeight;
        private final int medianTextHeight;
        private final int textGroupCount;

        private ImageTextProfile(int maxTextHeight, int medianTextHeight, int textGroupCount) {
            this.maxTextHeight = maxTextHeight;
            this.medianTextHeight = medianTextHeight;
            this.textGroupCount = textGroupCount;
        }

        private static ImageTextProfile empty() {
            return new ImageTextProfile(0, 0, 0);
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
