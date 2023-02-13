package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticlesDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
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
import java.util.List;
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
    private static final String KEYWORDS = "技术派,开源社区,java,springboot,IT,程序员,开发者,mysql,redis,Java基础,多线程,JVM,虚拟机,数据库,MySQL,Spring,Redis,MyBatis,系统设计,分布式,RPC,高可用,高并发";
    private static final String DES = "一款好用又强大的开源社区，附详细教程，包括Java、Spring、MySQL、Redis、操作系统、计算机网络、数据结构与算法等计算机专业核心知识点。学编程，就上技术派";

    @Resource
    private GlobalViewConfig globalViewConfig;

    /**
     * 文章详情页的seo标签
     *
     * @param detail
     */
    public void initColumnSeo(ArticleDetailVo detail) {
        List<SeoTagVo> list = initBasicSeoTag();
        list.add(new SeoTagVo("og:title", detail.getArticle().getTitle()));
        list.add(new SeoTagVo("og:description", detail.getArticle().getSummary()));
        list.add(new SeoTagVo("og:type", "article"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));
        String updateTime = DateUtil.time2LocalTime(detail.getArticle().getLastUpdateTime()).toString();
        list.add(new SeoTagVo("og:updated_time", updateTime));
        if (StringUtils.isNotBlank(detail.getArticle().getCover())) {
            list.add(new SeoTagVo("og:image", detail.getArticle().getCover()));
        }

        list.add(new SeoTagVo("article:modified_time", updateTime));
        list.add(new SeoTagVo("article:published_time", DateUtil.time2LocalTime(detail.getArticle().getCreateTime()).toString()));
        list.add(new SeoTagVo("article:tag", detail.getArticle().getTags().stream().map(TagDTO::getTag).collect(Collectors.joining(","))));
        list.add(new SeoTagVo("article:section", detail.getArticle().getCategory().getCategory()));
        list.add(new SeoTagVo("article:author", detail.getAuthor().getUserName()));

        list.add(new SeoTagVo("author", detail.getAuthor().getUserName()));
        list.add(new SeoTagVo("title", detail.getArticle().getTitle()));
        list.add(new SeoTagVo("description", detail.getArticle().getSummary()));
        list.add(new SeoTagVo("keywords", detail.getArticle().getCategory().getCategory() + "," + detail.getArticle().getTags().stream().map(TagDTO::getTag).collect(Collectors.joining(","))));

        ReqInfoContext.getReqInfo().setSeoList(list);
    }

    public void initColumnSeo(ColumnArticlesDTO detail, ColumnDTO column) {
        List<SeoTagVo> list = initBasicSeoTag();
        list.add(new SeoTagVo("og:title", detail.getArticle().getTitle()));
        list.add(new SeoTagVo("og:description", detail.getArticle().getSummary()));
        list.add(new SeoTagVo("og:type", "article"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));
        String updateTime = DateUtil.time2LocalTime(detail.getArticle().getLastUpdateTime()).toString();
        list.add(new SeoTagVo("og:updated_time", updateTime));
        list.add(new SeoTagVo("og:image", column.getCover()));

        list.add(new SeoTagVo("article:modified_time", updateTime));
        list.add(new SeoTagVo("article:published_time", DateUtil.time2LocalTime(detail.getArticle().getCreateTime()).toString()));
        list.add(new SeoTagVo("article:tag", detail.getArticle().getTags().stream().map(TagDTO::getTag).collect(Collectors.joining(","))));
        list.add(new SeoTagVo("article:section", column.getColumn()));
        list.add(new SeoTagVo("article:author", column.getAuthorName()));

        list.add(new SeoTagVo("author", column.getAuthorName()));
        list.add(new SeoTagVo("title", detail.getArticle().getTitle()));
        list.add(new SeoTagVo("description", detail.getArticle().getSummary()));
        list.add(new SeoTagVo("keywords", detail.getArticle().getCategory().getCategory() + "," + detail.getArticle().getTags().stream().map(TagDTO::getTag).collect(Collectors.joining(","))));

        ReqInfoContext.getReqInfo().setSeoList(list);
    }

    /**
     * 用户主页的seo标签
     *
     * @param user
     */
    public void initUserSeo(UserHomeVo user) {
        List<SeoTagVo> list = initBasicSeoTag();
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

        ReqInfoContext.getReqInfo().setSeoList(list);
    }


    public List<SeoTagVo> defaultSeo() {
        List<SeoTagVo> list = initBasicSeoTag();
        list.add(new SeoTagVo("og:title", "技术派"));
        list.add(new SeoTagVo("og:description", DES));
        list.add(new SeoTagVo("og:type", "article"));
        list.add(new SeoTagVo("og:locale", "zh-CN"));

        list.add(new SeoTagVo("article:tag", "后端,前端,Java,Spring,计算机"));
        list.add(new SeoTagVo("article:section", "开源社区"));
        list.add(new SeoTagVo("article:author", "技术派"));

        list.add(new SeoTagVo("author", "技术派"));
        list.add(new SeoTagVo("title", "技术派"));
        list.add(new SeoTagVo("description", DES));
        list.add(new SeoTagVo("keywords", KEYWORDS));
        ReqInfoContext.getReqInfo().setSeoList(list);
        return list;
    }


    private List<SeoTagVo> initBasicSeoTag() {
        List<SeoTagVo> list = new ArrayList<>();

        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        list.add(new SeoTagVo("og:url", globalViewConfig.getHost() + request.getRequestURI()));

        return list;
    }
}
