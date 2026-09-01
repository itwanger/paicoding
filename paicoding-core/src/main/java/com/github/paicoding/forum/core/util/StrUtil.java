package com.github.paicoding.forum.core.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.paicoding.forum.core.config.ImageProperties;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
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
    private static final String IMAGE_DIMENSION_CACHE_VERSION = "text-size-v12:";
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
    private static final int IMAGE_DIMENSION_TIMEOUT_MS = 1500;
    private static final int TEXT_SCREENSHOT_COMPACT_POSTER_WIDTH = 420;
    private static final int PORTRAIT_IMAGE_MIN_SOURCE_WIDTH = 640;
    private static final int PORTRAIT_IMAGE_MAX_SOURCE_WIDTH = 1200;
    private static final int PORTRAIT_IMAGE_MIN_RENDER_WIDTH = 400;
    private static final int PORTRAIT_IMAGE_MAX_RENDER_WIDTH = 560;

    /**
     * 正文内联图片下发的最大宽度。正文容器实测 736px，2x 高清屏取 1400 已有富余；
     * 原图多是 4000px 宽的 retina 截图，多出来的像素浏览器渲染时会直接丢弃。
     */
    private static final int INLINE_IMAGE_WIDTH = 1400;
    /**
     * 点击放大（Fancybox）时下发的宽度，比内联版清晰但仍远小于原图。
     */
    private static final int ZOOM_IMAGE_WIDTH = 2400;
    /**
     * 动图转 WebP 会丢动画，矢量图本身就很小，两者都不交给 OSS 处理。
     */
    private static final String[] IMAGE_PROCESS_SKIP_SUFFIXES = {".gif", ".svg"};

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
            ImageDimensionStore store = resolveImageDimensionStore();
            String cdnHost = resolveCdnHost();
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parseBodyFragment(html);
            org.jsoup.select.Elements images = doc.select("img");
            int imageIndex = 0;
            for (org.jsoup.nodes.Element img : images) {
                stabilizeImageLoadingAttrs(img, imageIndex++);

                String src = normalizeImageSrc(img.attr("src"));
                // 注意：src 变量始终保持原图地址，宽高探测与 image_dimension 表都以它为键
                applyCdnImageProcess(img, src, cdnHost);

                ImageDimension dimension = null;
                if (StringUtils.isNotBlank(src)) {
                    String cacheKey = buildImageDimensionCacheKey(src);
                    dimension = IMAGE_DIMENSION_CACHE.getIfPresent(cacheKey);
                    if (dimension == null && store != null) {
                        int[] stored = store.find(src);
                        if (stored != null) {
                            dimension = new ImageDimension(stored[0], stored[1]);
                            IMAGE_DIMENSION_CACHE.put(cacheKey, dimension);
                        }
                    }
                    if (dimension == null) {
                        warmImageDimension(src, cacheKey, store);
                    }
                }

                if (dimension != null) {
                    img.attr("width", String.valueOf(dimension.width));
                    img.attr("height", String.valueOf(dimension.height));
                    img.removeClass("article-content-img--pending-size");
                    markReadableImageClass(img, dimension);
                    continue;
                }

                ImageDimension attrDimension = resolveAttrImageDimension(img);
                if (attrDimension != null) {
                    markReadableImageClass(img, attrDimension);
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

        // 首图往往就是 LCP 元素，lazy 加载会显著推迟发现时间，必须立即加载
        if (imageIndex == 0) {
            img.removeAttr("loading");
            if (!img.hasAttr("fetchpriority")) {
                img.attr("fetchpriority", "high");
            }
            return;
        }

        if (!img.hasAttr("loading")) {
            img.attr("loading", "lazy");
        }

        if (!img.hasAttr("fetchpriority")) {
            img.attr("fetchpriority", "low");
        }
    }

    private static void markReadableImageClass(org.jsoup.nodes.Element img, ImageDimension dimension) {
        int maxDisplayWidth = inferPortraitImageMaxWidth(dimension.width, dimension.height);
        if (looksLikeCompactPoster(img, dimension)) {
            img.addClass("article-content-img--compact-poster");
            maxDisplayWidth = Math.min(dimension.width, TEXT_SCREENSHOT_COMPACT_POSTER_WIDTH);
        }

        if (maxDisplayWidth > 0) {
            img.addClass("article-content-img--text-shot");
            appendStyle(img, "--article-img-max-width: " + maxDisplayWidth + "px");
        }
    }

    private static ImageDimension resolveAttrImageDimension(org.jsoup.nodes.Element img) {
        int width = NumberUtils.toInt(img.attr("width"), 0);
        int height = NumberUtils.toInt(img.attr("height"), 0);
        if (width <= 0 || height <= 0) {
            return null;
        }

        return new ImageDimension(width, height);
    }

    /**
     * 只收窄竖图（高 > 宽，典型为微信聊天/手机截图，文字偏大），横图、方图保持原始宽度，
     * 避免终端/产品界面等横版截图被压缩后文字不可读。规则纯几何、确定性，不做像素内容分析。
     */
    private static int inferPortraitImageMaxWidth(int width, int height) {
        if (width < PORTRAIT_IMAGE_MIN_SOURCE_WIDTH || width > PORTRAIT_IMAGE_MAX_SOURCE_WIDTH || height <= width) {
            return 0;
        }

        int maxDisplayWidth = width / 2;
        maxDisplayWidth = Math.max(PORTRAIT_IMAGE_MIN_RENDER_WIDTH, maxDisplayWidth);
        maxDisplayWidth = Math.min(PORTRAIT_IMAGE_MAX_RENDER_WIDTH, maxDisplayWidth);
        return maxDisplayWidth < width * 0.92D ? maxDisplayWidth : 0;
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

    /**
     * 只重写正文图片地址，不改动任何布局属性。
     *
     * <p>给小程序这类自带排版逻辑的渲染端用：它们的容器样式不受站点 CSS 约束，
     * 写入原图宽高（动辄 3872px）反而可能撑破布局；但图片体积的问题是共通的——
     * 小程序侧同样在下发单张 6~8MB 的原图。</p>
     */
    public static String compressHtmlImages(String html) {
        return compressHtmlImages(html, resolveCdnHost());
    }

    static String compressHtmlImages(String html, String cdnHost) {
        if (StringUtils.isBlank(html) || StringUtils.isBlank(cdnHost)) {
            return html;
        }

        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parseBodyFragment(html);
            for (org.jsoup.nodes.Element img : doc.select("img")) {
                String processed = buildProcessedImageUrl(normalizeImageSrc(img.attr("src")), cdnHost, INLINE_IMAGE_WIDTH);
                if (processed != null) {
                    img.attr("src", processed);
                }
            }
            return doc.body().html();
        } catch (Exception e) {
            return html;
        }
    }

    /**
     * 正文图片交给阿里云 OSS 做缩放和格式转换。
     *
     * <p>原图多是 4000px 宽的未压缩 retina 截图，单张 6~8MB，而正文容器只有 736px——
     * 多下发的像素在浏览器端直接被丢弃。实测单篇文章的图片总量可以从 62MB 降到 2MB，
     * 其中约八成的节省来自"不再下发看不见的像素"，与画质无关。</p>
     *
     * <p>宽高属性仍写原图数值：等比缩放不改变宽高比，占位盒子依然正确，CLS 不受影响。</p>
     */
    static void applyCdnImageProcess(org.jsoup.nodes.Element img, String src, String cdnHost) {
        String inline = buildProcessedImageUrl(src, cdnHost, INLINE_IMAGE_WIDTH);
        if (inline == null) {
            return;
        }

        img.attr("src", inline);
        // Fancybox 绑定在 img 上，默认拿 src 当放大源；用 data-src 给它更清晰的一份
        img.attr("data-src", buildProcessedImageUrl(src, cdnHost, ZOOM_IMAGE_WIDTH));
    }

    /**
     * 拼接 OSS 图片处理地址，不适用时返回 null。
     *
     * @param src     已规范化的图片地址
     * @param cdnHost 本站 CDN 前缀，只处理本站图片，外链原样保留
     * @param width   下发的最大宽度，OSS 只缩不放，小图不受影响
     */
    static String buildProcessedImageUrl(String src, String cdnHost, int width) {
        if (StringUtils.isBlank(src) || StringUtils.isBlank(cdnHost)) {
            return null;
        }
        if (!StringUtils.startsWithIgnoreCase(src, cdnHost)) {
            return null;
        }
        // 已带参数的地址可能是签名 URL 或已处理过，不重复追加
        if (src.indexOf('?') >= 0) {
            return null;
        }
        String lower = src.toLowerCase(Locale.ROOT);
        for (String suffix : IMAGE_PROCESS_SKIP_SUFFIXES) {
            if (lower.endsWith(suffix)) {
                return null;
            }
        }
        return src + "?x-oss-process=image/resize,w_" + width + "/format,webp";
    }

    private static String resolveCdnHost() {
        try {
            ImageProperties properties = SpringUtil.getBeanOrNull(ImageProperties.class);
            return properties == null ? null : StringUtils.trimToNull(properties.getCdnHost());
        } catch (Exception e) {
            return null;
        }
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
                        return new ImageDimension(width, height);
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

    private static ImageDimensionStore resolveImageDimensionStore() {
        try {
            return SpringUtil.getBeanOrNull(ImageDimensionStore.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 同步探测图片尺寸并持久化，供存量数据回填等后台任务调用
     *
     * @return true 表示尺寸已可用（缓存/库中已有或本次探测成功）
     */
    public static boolean ensureImageDimension(String rawSrc) {
        String src = normalizeImageSrc(rawSrc);
        if (StringUtils.isBlank(src)) {
            return false;
        }

        String cacheKey = buildImageDimensionCacheKey(src);
        if (IMAGE_DIMENSION_CACHE.getIfPresent(cacheKey) != null) {
            return true;
        }

        ImageDimensionStore store = resolveImageDimensionStore();
        if (store != null) {
            int[] stored = store.find(src);
            if (stored != null) {
                IMAGE_DIMENSION_CACHE.put(cacheKey, new ImageDimension(stored[0], stored[1]));
                return true;
            }
        }

        ImageDimension dimension = loadImageDimension(src);
        if (dimension == null) {
            IMAGE_DIMENSION_MISS_CACHE.put(cacheKey, Boolean.TRUE);
            return false;
        }

        IMAGE_DIMENSION_CACHE.put(cacheKey, dimension);
        if (store != null) {
            store.save(src, dimension.width, dimension.height);
        }
        return true;
    }

    private static void warmImageDimension(String src, String cacheKey, ImageDimensionStore store) {
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
                    if (store != null) {
                        store.save(src, dimension.width, dimension.height);
                    }
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

        private ImageDimension(int width, int height) {
            this.width = width;
            this.height = height;
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
