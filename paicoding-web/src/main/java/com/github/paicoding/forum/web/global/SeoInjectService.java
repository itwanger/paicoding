package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticlesDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
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
import java.util.List;
import java.util.Map;
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

    @Resource
    private GlobalViewConfig globalViewConfig;

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
        String description = detail.getArticle().getSummary();
        String authorName = detail.getAuthor().getUserName();
        String updateTime = DateUtil.time2LocalTime(detail.getArticle().getLastUpdateTime()).toString();
        String publishedTime = DateUtil.time2LocalTime(detail.getArticle().getCreateTime()).toString();
        String image = detail.getArticle().getCover();

        list.add(new SeoTagVo("og:title", title));
        list.add(new SeoTagVo("og:description", detail.getArticle().getSummary()));
        list.add(new SeoTagVo("og:type", "article"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));
        list.add(new SeoTagVo("og:updated_time", updateTime));

        list.add(new SeoTagVo("article:modified_time", updateTime));
        list.add(new SeoTagVo("article:published_time", publishedTime));
        list.add(new SeoTagVo("article:tag", detail.getArticle().getTags().stream().map(TagDTO::getTag).collect(Collectors.joining(","))));
        list.add(new SeoTagVo("article:section", detail.getArticle().getCategory().getCategory()));
        list.add(new SeoTagVo("article:author", authorName));

        list.add(new SeoTagVo("author", authorName));
        list.add(new SeoTagVo("title", title));
        list.add(new SeoTagVo("description", description));
        list.add(new SeoTagVo("keywords", detail.getArticle().getCategory().getCategory() + "," + detail.getArticle().getTags().stream().map(TagDTO::getTag).collect(Collectors.joining(","))));

        if (StringUtils.isNotBlank(image)) {
            list.add(new SeoTagVo("og:image", image));
            jsonLd.put("image", image);
        }

        // 优化 JSON-LD 为 TechArticle 类型
        jsonLd.put("@type", "TechArticle");
        jsonLd.put("headline", title);
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
        String description = detail.getArticle().getSummary();
        String authorName = column.getAuthorName();
        String updateTime = DateUtil.time2LocalTime(detail.getArticle().getLastUpdateTime()).toString();
        String publishedTime = DateUtil.time2LocalTime(detail.getArticle().getCreateTime()).toString();
        String image = column.getCover();

        list.add(new SeoTagVo("og:title", title));
        list.add(new SeoTagVo("og:description", description));
        list.add(new SeoTagVo("og:type", "article"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));

        list.add(new SeoTagVo("og:updated_time", updateTime));
        list.add(new SeoTagVo("og:image", image));

        list.add(new SeoTagVo("article:modified_time", updateTime));
        list.add(new SeoTagVo("article:published_time", publishedTime));
        list.add(new SeoTagVo("article:tag", detail.getArticle().getTags().stream().map(TagDTO::getTag).collect(Collectors.joining(","))));
        list.add(new SeoTagVo("article:section", column.getColumn()));
        list.add(new SeoTagVo("article:author", authorName));

        list.add(new SeoTagVo("author", authorName));
        list.add(new SeoTagVo("title", title));
        list.add(new SeoTagVo("description", detail.getArticle().getSummary()));
        list.add(new SeoTagVo("keywords", detail.getArticle().getCategory().getCategory() + "," + detail.getArticle().getTags().stream().map(TagDTO::getTag).collect(Collectors.joining(","))));

        // 优化 JSON-LD 为 TechArticle 类型（教程也是技术文章）
        jsonLd.put("@type", "TechArticle");
        jsonLd.put("headline", title);
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

        if (ReqInfoContext.getReqInfo() != null) ReqInfoContext.getReqInfo().setSeo(seo);
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
        potentialAction.put("target", globalViewConfig.getHost() + "/search?q={search_term_string}");
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
        String url = globalViewConfig.getHost() + request.getRequestURI();

        list.add(new SeoTagVo("og:url", url));
        map.put("url", url);

        return Seo.builder().jsonLd(map).ogp(list).build();
    }

}
