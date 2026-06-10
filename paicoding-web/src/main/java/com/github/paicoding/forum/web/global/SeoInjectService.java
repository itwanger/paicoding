package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ArticleReadTypeEnum;
import com.github.paicoding.forum.api.model.enums.column.ColumnTypeEnum;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticlesDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.api.model.vo.seo.Seo;
import com.github.paicoding.forum.api.model.vo.seo.SeoTagVo;
import com.github.paicoding.forum.core.util.DateUtil;
import com.github.paicoding.forum.web.config.GlobalViewConfig;
import com.github.paicoding.forum.web.front.article.vo.ArticleDetailVo;
import com.github.paicoding.forum.web.front.user.vo.UserHomeVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * seo注入服务，下面加个页面使用
 * - 首页
 * - 文章详情页
 * - 用户主页
 * - 专栏内容详情页
 * <p>
 * ogp seo标签: <a href="https://ogp.me/">开放内容协议 OGP</a>
 *
 * @author YiHui
 * @date 2023/2/13
 */
@Service
public class SeoInjectService {
    private static final String KEYWORDS = "技术派,开源社区,java,springboot,IT,程序员,开发者,mysql,redis,Java基础,多线程,JVM,虚拟机,数据库,MySQL,Spring,Redis,MyBatis,系统设计,分布式,RPC,高可用,高并发,沉默王二";
    private static final String DES = "技术派,一个基于 Spring Boot、MyBatis-Plus、MySQL、Redis、ElasticSearch、MongoDB、Docker、RabbitMQ 等技术栈实现的社区系统，采用主流的互联网技术架构、全新的UI设计、支持一键源码部署，拥有完整的文章&教程发布/搜索/评论/统计流程等，代码完全开源，没有任何二次封装，是一个非常适合二次开发/实战的现代化社区项目。学编程，就上技术派";
    private static final int DESCRIPTION_MAX_LENGTH = 180;
    private static final String PAYWALL_SELECTOR = ".paywall";
    private static final String[] SEO_ENTITY_KEYWORDS = {
            "派聪明", "PaiSmart", "PaiFlow", "派派工作流", "PaiCLI", "MCP", "RAG", "Agent",
            "Dify", "Coze", "n8n", "Spring AI", "LangGraph4J", "DeepSeek", "GLM"
    };

    @Resource
    private GlobalViewConfig globalViewConfig;

    private static final String COLUMN_HOME_TITLE = "AI 实战项目教程：派聪明 RAG、PaiFlow、PaiCLI、技术派";
    private static final String COLUMN_HOME_DESCRIPTION = "技术派 AI 实战项目教程集合，覆盖派聪明 RAG 知识库、PaiFlow 企业级 Agent 工作流、PaiCLI Java Agent CLI、技术派社区、PmHub 微服务等项目，提供源码、架构设计、部署、简历和面试教程。";
    private static final String COLUMN_HOME_KEYWORDS = "AI实战项目,派聪明,RAG,PaiFlow,PaiCLI,Agent,技术派,PmHub,Java项目教程";

    /**
     * 文章详情页的seo标签
     *
     * @param detail
     */
    public void initColumnSeo(ArticleDetailVo detail) {
        Seo seo = initBasicSeoTag();
        List<SeoTagVo> list = seo.getOgp();
        Map<String, Object> jsonLd = seo.getJsonLd();

        String title = detail.getArticle().getTitle();
        String description = buildSeoDescription(title, detail.getArticle().getSummary());
        String authorName = detail.getAuthor().getUserName();
        String updateTime = DateUtil.time2LocalTime(detail.getArticle().getLastUpdateTime()).toString();
        String publishedTime = DateUtil.time2LocalTime(detail.getArticle().getCreateTime()).toString();
        String image = detail.getArticle().getCover();
        String tagKeywords = buildTagKeywords(detail.getArticle().getTags());
        String keywords = buildArticleKeywords(title, detail.getArticle().getCategory().getCategory(), tagKeywords, null);

        list.add(new SeoTagVo("og:title", title));
        list.add(new SeoTagVo("og:description", description));
        list.add(new SeoTagVo("og:type", "article"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));
        list.add(new SeoTagVo("og:updated_time", updateTime));

        list.add(new SeoTagVo("article:modified_time", updateTime));
        list.add(new SeoTagVo("article:published_time", publishedTime));
        list.add(new SeoTagVo("article:tag", tagKeywords));
        list.add(new SeoTagVo("article:section", detail.getArticle().getCategory().getCategory()));
        list.add(new SeoTagVo("article:author", authorName));

        list.add(new SeoTagVo("author", authorName));
        list.add(new SeoTagVo("title", title));
        list.add(new SeoTagVo("description", description));
        list.add(new SeoTagVo("keywords", keywords));

        if (StringUtils.isNotBlank(image)) {
            list.add(new SeoTagVo("og:image", image));
            jsonLd.put("image", image);
        }

        // 优化 JSON-LD 为 TechArticle 类型
        jsonLd.put("@type", "TechArticle");
        jsonLd.put("headline", title);
        putAlternativeHeadline(jsonLd, title);
        jsonLd.put("description", description);
        
        Map<String, Object> author = new HashMap<>();
        author.put("@type", "Person");
        author.put("name", authorName);
        jsonLd.put("author", author);
        
        jsonLd.put("dateModified", updateTime);
        jsonLd.put("datePublished", publishedTime);
        
        // 添加发布者信息
        Map<String, Object> publisher = new HashMap<>();
        publisher.put("@type", "Organization");
        publisher.put("name", "技术派");
        
        Map<String, Object> logo = new HashMap<>();
        logo.put("@type", "ImageObject");
        logo.put("url", globalViewConfig.getHost() + "/img/logo.svg");
        publisher.put("logo", logo);

        jsonLd.put("publisher", publisher);
        if (isPaywalledArticle(detail)) {
            markPaywalledContent(jsonLd);
        }

        ReqInfoContext.getReqInfo().setSeo(seo);
    }

    /**
     * 教程详情seo标签
     *
     * @param detail
     */
    public void initColumnSeo(ColumnArticlesDTO detail, ColumnDTO column) {
        Seo seo = initBasicSeoTag();
        List<SeoTagVo> list = seo.getOgp();
        Map<String, Object> jsonLd = seo.getJsonLd();

        String title = detail.getArticle().getTitle();
        String description = buildSeoDescription(title, detail.getArticle().getSummary());
        String authorName = column.getAuthorName();
        String updateTime = DateUtil.time2LocalTime(detail.getArticle().getLastUpdateTime()).toString();
        String publishedTime = DateUtil.time2LocalTime(detail.getArticle().getCreateTime()).toString();
        String image = column.getCover();
        String tagKeywords = buildTagKeywords(detail.getArticle().getTags());
        String keywords = buildArticleKeywords(title, detail.getArticle().getCategory().getCategory(), tagKeywords, column.getColumn());

        list.add(new SeoTagVo("og:title", title));
        list.add(new SeoTagVo("og:description", description));
        list.add(new SeoTagVo("og:type", "article"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));

        list.add(new SeoTagVo("og:updated_time", updateTime));
        list.add(new SeoTagVo("og:image", image));

        list.add(new SeoTagVo("article:modified_time", updateTime));
        list.add(new SeoTagVo("article:published_time", publishedTime));
        list.add(new SeoTagVo("article:tag", tagKeywords));
        list.add(new SeoTagVo("article:section", column.getColumn()));
        list.add(new SeoTagVo("article:author", authorName));

        list.add(new SeoTagVo("author", authorName));
        list.add(new SeoTagVo("title", title));
        list.add(new SeoTagVo("description", description));
        list.add(new SeoTagVo("keywords", keywords));

        // 优化 JSON-LD 为 TechArticle 类型（教程也是技术文章）
        jsonLd.put("@type", "TechArticle");
        jsonLd.put("headline", title);
        putAlternativeHeadline(jsonLd, title);
        jsonLd.put("description", description);
        
        Map<String, Object> author = new HashMap<>();
        author.put("@type", "Person");
        author.put("name", authorName);
        jsonLd.put("author", author);
        
        jsonLd.put("dateModified", updateTime);
        jsonLd.put("datePublished", publishedTime);
        jsonLd.put("image", image);
        
        // 添加发布者信息
        Map<String, Object> publisher = new HashMap<>();
        publisher.put("@type", "Organization");
        publisher.put("name", "技术派");
        
        Map<String, Object> logo = new HashMap<>();
        logo.put("@type", "ImageObject");
        logo.put("url", globalViewConfig.getHost() + "/img/logo.svg");
        publisher.put("logo", logo);
        
        jsonLd.put("publisher", publisher);
        
        // 添加教程所属专栏信息
        Map<String, Object> isPartOf = new HashMap<>();
        isPartOf.put("@type", "Course");
        isPartOf.put("name", column.getColumn());
        isPartOf.put("description", column.getIntroduction());
        jsonLd.put("isPartOf", isPartOf);
        if (isPaywalledColumnArticle(detail)) {
            markPaywalledContent(jsonLd);
        }

        if (ReqInfoContext.getReqInfo() != null) ReqInfoContext.getReqInfo().setSeo(seo);
    }

    /**
     * 教程聚合页 SEO：让 /column 明确成为 AI/RAG/Agent 项目集合入口。
     *
     * @param columns 首屏服务端渲染的教程列表
     */
    public void initColumnHomeSeo(List<ColumnDTO> columns) {
        Seo seo = initBasicSeoTag();
        List<SeoTagVo> list = seo.getOgp();
        Map<String, Object> jsonLd = seo.getJsonLd();

        list.add(new SeoTagVo("og:title", COLUMN_HOME_TITLE));
        list.add(new SeoTagVo("og:description", COLUMN_HOME_DESCRIPTION));
        list.add(new SeoTagVo("og:type", "website"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));
        list.add(new SeoTagVo("title", COLUMN_HOME_TITLE));
        list.add(new SeoTagVo("description", COLUMN_HOME_DESCRIPTION));
        list.add(new SeoTagVo("keywords", COLUMN_HOME_KEYWORDS));

        jsonLd.put("@type", "CollectionPage");
        jsonLd.put("name", COLUMN_HOME_TITLE);
        jsonLd.put("headline", COLUMN_HOME_TITLE);
        jsonLd.put("description", COLUMN_HOME_DESCRIPTION);

        List<Map<String, Object>> itemList = new ArrayList<>();
        if (columns != null) {
            int position = 1;
            for (ColumnDTO column : columns) {
                if (column == null || StringUtils.isBlank(column.getColumn())) {
                    continue;
                }
                Map<String, Object> item = new HashMap<>();
                item.put("@type", "Course");
                item.put("position", position++);
                item.put("name", column.getColumn());
                item.put("description", cleanSeoText(column.getIntroduction()));
                if (StringUtils.isNotBlank(column.getUrlSlug())) {
                    item.put("url", globalViewConfig.getHost() + "/column/" + column.getUrlSlug());
                }
                itemList.add(item);
            }
        }
        if (!itemList.isEmpty()) {
            jsonLd.put("mainEntity", itemList);
        }

        if (ReqInfoContext.getReqInfo() != null) ReqInfoContext.getReqInfo().setSeo(seo);
    }

    /**
     * 单个教程落地页 SEO：让 /column/{slug} 成为稳定的项目目录页。
     *
     * @param column 教程信息
     * @param articles 教程章节列表
     */
    public void initColumnLandingSeo(ColumnDTO column, List<SimpleArticleDTO> articles) {
        Seo seo = initBasicSeoTag();
        List<SeoTagVo> list = seo.getOgp();
        Map<String, Object> jsonLd = seo.getJsonLd();

        String columnName = StringUtils.defaultIfBlank(column.getColumn(), "技术派项目教程");
        String title = columnName + "：项目介绍、源码教程、学习路线和面试指南";
        String description = buildColumnLandingDescription(column);
        String keywords = buildArticleKeywords(columnName, null, null, columnName);

        list.add(new SeoTagVo("og:title", title));
        list.add(new SeoTagVo("og:description", description));
        list.add(new SeoTagVo("og:type", "website"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));
        if (StringUtils.isNotBlank(column.getCover())) {
            list.add(new SeoTagVo("og:image", column.getCover()));
            jsonLd.put("image", column.getCover());
        }
        list.add(new SeoTagVo("title", title));
        list.add(new SeoTagVo("description", description));
        list.add(new SeoTagVo("keywords", keywords));

        jsonLd.put("@type", "Course");
        jsonLd.put("name", columnName);
        jsonLd.put("headline", title);
        jsonLd.put("description", description);
        Map<String, Object> provider = new HashMap<>();
        provider.put("@type", "Organization");
        provider.put("name", "技术派");
        provider.put("url", globalViewConfig.getHost());
        jsonLd.put("provider", provider);

        List<Map<String, Object>> parts = new ArrayList<>();
        if (articles != null) {
            int position = 1;
            for (SimpleArticleDTO article : articles) {
                if (article == null || StringUtils.isBlank(article.getTitle())) {
                    continue;
                }
                Map<String, Object> part = new HashMap<>();
                part.put("@type", "CreativeWork");
                part.put("position", position++);
                part.put("name", article.getTitle());
                String articleUrl = buildColumnArticleUrl(column, article);
                if (StringUtils.isNotBlank(articleUrl)) {
                    part.put("url", globalViewConfig.getHost() + articleUrl);
                }
                parts.add(part);
            }
        }
        if (!parts.isEmpty()) {
            jsonLd.put("hasPart", parts);
        }

        if (ReqInfoContext.getReqInfo() != null) ReqInfoContext.getReqInfo().setSeo(seo);
    }

    private String buildColumnArticleUrl(ColumnDTO column, SimpleArticleDTO article) {
        if (column == null || article == null) {
            return "";
        }
        if (StringUtils.isNotBlank(article.getUrlSlug())) {
            return "/" + article.getUrlSlug();
        }
        if (article.getSort() == null) {
            return "";
        }
        String columnKey = StringUtils.defaultIfBlank(column.getUrlSlug(), String.valueOf(column.getColumnId()));
        return "/column/" + columnKey + "/" + article.getSort();
    }

    /**
     * 用户主页的seo标签
     *
     * @param user
     */
    public void initUserSeo(UserHomeVo user) {
        Seo seo = initBasicSeoTag();
        List<SeoTagVo> list = seo.getOgp();
        Map<String, Object> jsonLd = seo.getJsonLd();

        // 用户主页带 homeSelectType/followSelectType/userId 等 query 参数时，
        // 是同一份页面的不同视图，统一指向无 query 的 canonical 并屏蔽索引
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (isUserStateQuery(request.getQueryString())) {
            replaceOgpTag(list, "robots", "noindex, follow");
        }

        String title = "技术派 | " + user.getUserHome().getUserName() + " 的主页";
        list.add(new SeoTagVo("og:title", title));
        list.add(new SeoTagVo("og:description", user.getUserHome().getProfile()));
        list.add(new SeoTagVo("og:type", "article"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));

        list.add(new SeoTagVo("article:tag", "后端,前端,Java,Spring,计算机"));
        list.add(new SeoTagVo("article:section", "主页"));
        list.add(new SeoTagVo("article:author", user.getUserHome().getUserName()));

        list.add(new SeoTagVo("author", user.getUserHome().getUserName()));
        list.add(new SeoTagVo("title", title));
        list.add(new SeoTagVo("description", user.getUserHome().getProfile()));
        list.add(new SeoTagVo("keywords", KEYWORDS));

        jsonLd.put("headline", title);
        jsonLd.put("description", user.getUserHome().getProfile());
        Map<String, Object> author = new HashMap<>();
        author.put("@type", "Person");
        author.put("name", user.getUserHome().getUserName());
        jsonLd.put("author", author);

        if (ReqInfoContext.getReqInfo() != null) ReqInfoContext.getReqInfo().setSeo(seo);
    }


    public Seo defaultSeo() {
        Seo seo = initBasicSeoTag();
        List<SeoTagVo> list = seo.getOgp();
        list.add(new SeoTagVo("og:title", "技术派 - 学编程就上技术派"));
        list.add(new SeoTagVo("og:description", DES));
        list.add(new SeoTagVo("og:type", "website"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));

        list.add(new SeoTagVo("title", "技术派 - 学编程就上技术派"));
        list.add(new SeoTagVo("description", DES));
        list.add(new SeoTagVo("keywords", KEYWORDS));

        Map<String, Object> jsonLd = seo.getJsonLd();
        jsonLd.put("@context", "https://schema.org");
        jsonLd.put("@type", "WebSite");
        jsonLd.put("name", "技术派");
        jsonLd.put("url", globalViewConfig.getHost());
        jsonLd.put("description", DES);
        
        // 添加搜索功能
        Map<String, Object> potentialAction = new HashMap<>();
        potentialAction.put("@type", "SearchAction");
        potentialAction.put("target", globalViewConfig.getHost() + "/search?key={search_term_string}");
        potentialAction.put("query-input", "required name=search_term_string");
        jsonLd.put("potentialAction", potentialAction);

        if (ReqInfoContext.getReqInfo() != null) {
            ReqInfoContext.getReqInfo().setSeo(seo);
        }
        return seo;
    }

    private Seo initBasicSeoTag() {

        List<SeoTagVo> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        
        // 添加 @context，确保所有页面都有
        map.put("@context", "https://schema.org");

        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestUri = request.getRequestURI();
        if (StringUtils.isBlank(requestUri)) {
            requestUri = "/";
        }

        int sessionIdIndex = StringUtils.indexOfIgnoreCase(requestUri, ";jsessionid=");
        if (sessionIdIndex >= 0) {
            requestUri = requestUri.substring(0, sessionIdIndex);
        }

        String host = StringUtils.removeEnd(StringUtils.defaultString(globalViewConfig.getHost()), "/");
        String normalizedPath = StringUtils.startsWith(requestUri, "/") ? requestUri : "/" + requestUri;
        String url = host + normalizedPath;

        list.add(new SeoTagVo("canonical", url));
        list.add(new SeoTagVo("og:url", url));
        list.add(new SeoTagVo("robots", "all"));
        map.put("url", url);

        return Seo.builder().jsonLd(map).ogp(list).build();
    }

    private boolean isUserStateQuery(String query) {
        return StringUtils.contains(query, "homeSelectType=")
                || StringUtils.contains(query, "followSelectType=")
                || StringUtils.contains(query, "userId=");
    }

    private void replaceOgpTag(List<SeoTagVo> list, String key, String val) {
        for (SeoTagVo tag : list) {
            if (StringUtils.equals(tag.getKey(), key)) {
                tag.setVal(val);
                return;
            }
        }
        list.add(new SeoTagVo(key, val));
    }

    private String buildArticleKeywords(String title, String category, String tagKeywords, String columnName) {
        Set<String> keywords = new LinkedHashSet<>();
        addEntityKeywords(keywords, title);
        addEntityKeywords(keywords, columnName);
        if (StringUtils.isNotBlank(tagKeywords)) {
            for (String tag : tagKeywords.split(",")) {
                addKeyword(keywords, tag);
            }
        }
        if (keywords.isEmpty()) {
            addKeyword(keywords, leadingHeadline(title));
        }
        return keywords.stream().collect(Collectors.joining(","));
    }

    private void addEntityKeywords(Set<String> keywords, String text) {
        String val = StringUtils.defaultString(text);
        for (String entity : SEO_ENTITY_KEYWORDS) {
            if (StringUtils.containsIgnoreCase(val, entity)) {
                addKeyword(keywords, entity);
            }
        }
    }

    private String buildTagKeywords(List<TagDTO> tags) {
        if (tags == null) {
            return "";
        }
        return tags.stream()
                .map(TagDTO::getTag)
                .filter(this::isValidSeoText)
                .map(this::cleanSeoKeyword)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
    }

    private String buildSeoDescription(String title, String summary) {
        String description = cleanSeoText(summary);
        String leadingHeadline = leadingHeadline(title);
        if (StringUtils.isBlank(leadingHeadline) || StringUtils.contains(description, leadingHeadline)) {
            return abbreviateDescription(description);
        }
        if (StringUtils.isBlank(description)) {
            return leadingHeadline;
        }
        return abbreviateDescription(leadingHeadline + "：" + description);
    }

    private String buildColumnLandingDescription(ColumnDTO column) {
        String name = StringUtils.defaultIfBlank(column.getColumn(), "技术派项目教程");
        String intro = cleanSeoText(column.getIntroduction());
        if (StringUtils.isBlank(intro)) {
            return abbreviateDescription(name + "项目教程，包含项目介绍、技术栈、源码学习路线、部署实践、简历写法和面试指南。");
        }
        return abbreviateDescription(name + "：" + intro);
    }

    private void putAlternativeHeadline(Map<String, Object> jsonLd, String title) {
        String alternativeHeadline = leadingHeadline(title);
        if (StringUtils.isNotBlank(alternativeHeadline) && !StringUtils.equals(alternativeHeadline, title)) {
            jsonLd.put("alternativeHeadline", alternativeHeadline);
        }
    }

    private boolean isPaywalledArticle(ArticleDetailVo detail) {
        if (detail == null || detail.getArticle() == null) {
            return false;
        }
        Integer readType = detail.getArticle().getReadType();
        boolean paywallReadType = ArticleReadTypeEnum.PAY_READ.getType().equals(readType)
                || ArticleReadTypeEnum.STAR_READ.getType().equals(readType);
        return paywallReadType && !Boolean.TRUE.equals(detail.getArticle().getCanRead());
    }

    private boolean isPaywalledColumnArticle(ColumnArticlesDTO detail) {
        if (detail == null || detail.getOther() == null) {
            return false;
        }
        return Integer.valueOf(ColumnTypeEnum.STAR_READ.getType()).equals(detail.getOther().getReadType());
    }

    private void markPaywalledContent(Map<String, Object> jsonLd) {
        jsonLd.put("isAccessibleForFree", false);

        Map<String, Object> paywalledPart = new HashMap<>();
        paywalledPart.put("@type", "WebPageElement");
        paywalledPart.put("isAccessibleForFree", false);
        paywalledPart.put("cssSelector", PAYWALL_SELECTOR);
        jsonLd.put("hasPart", paywalledPart);
    }

    private String leadingHeadline(String title) {
        String normalized = normalizeHeadline(title);
        if (StringUtils.isBlank(normalized)) {
            return normalized;
        }
        String[] parts = normalized.split("[，,。；;：:！？!?]", 2);
        return StringUtils.trim(parts[0]);
    }

    private String normalizeHeadline(String title) {
        return cleanSeoKeyword(StringUtils.stripEnd(title, "，,。；;：:！？!? "));
    }

    private String cleanSeoText(String text) {
        String val = StringUtils.defaultString(text);
        val = val.replaceAll("<[^>]+>", " ");
        val = val.replaceAll("!\\[[^\\]]*]\\([^)]*\\)", " ");
        val = val.replaceAll("\\[([^\\]]+)]\\([^)]*\\)", "$1");
        val = val.replaceAll("[*_`#>~]+", "");
        val = val.replaceAll("\\s+", " ");
        return StringUtils.trim(val);
    }

    private String abbreviateDescription(String description) {
        String val = StringUtils.trim(description);
        if (StringUtils.length(val) <= DESCRIPTION_MAX_LENGTH) {
            return val;
        }
        return StringUtils.substring(val, 0, DESCRIPTION_MAX_LENGTH) + "...";
    }

    private void addKeyword(Set<String> keywords, String keyword) {
        String val = cleanSeoKeyword(keyword);
        if (isValidSeoText(val)) {
            keywords.add(val);
        }
    }

    private boolean isValidSeoText(String text) {
        String val = StringUtils.trim(text);
        return StringUtils.isNotBlank(val) && !"null".equalsIgnoreCase(val);
    }

    private String cleanSeoKeyword(String text) {
        String val = StringUtils.trim(StringUtils.defaultString(text));
        val = StringUtils.stripEnd(val, "，,。；;：:！？!? ");
        val = val.replaceAll("^[\\s\\p{P}\\p{S}]+", "");
        val = val.replaceAll("\\s+", " ");
        return StringUtils.trim(val);
    }

}
