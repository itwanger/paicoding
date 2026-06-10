package com.github.paicoding.forum.web.front.article.view;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ArticleReadTypeEnum;
import com.github.paicoding.forum.api.model.enums.column.ColumnArticleReadEnum;
import com.github.paicoding.forum.api.model.enums.column.ColumnStatusEnum;
import com.github.paicoding.forum.api.model.enums.column.ColumnTypeEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleOtherDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleFlipDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticlesDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.api.model.vo.recommend.SideBarDTO;
import com.github.paicoding.forum.core.util.MarkdownConverter;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.core.util.StrUtil;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.service.ArticlePayService;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.ColumnService;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.sidebar.service.SidebarService;
import com.github.paicoding.forum.web.config.GlobalViewConfig;
import com.github.paicoding.forum.web.front.article.vo.ColumnVo;
import com.github.paicoding.forum.web.global.GlobalInitService;
import com.github.paicoding.forum.web.global.SeoInjectService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 专栏入口
 *
 * @author YiHui
 * @date 2022/9/15
 */
@Controller
@RequestMapping(path = "column")
public class ColumnViewController {
    @Autowired
    private ColumnService columnService;
    @Autowired
    private ArticleReadService articleReadService;

    @Autowired
    private CommentReadService commentReadService;

    @Autowired
    private SidebarService sidebarService;

    @Resource
    private GlobalViewConfig globalViewConfig;
    @Autowired
    private ArticlePayService articlePayService;

    private static final long COLUMN_HOME_PAGE_SIZE = 100L;
    /**
     * 专栏主页，展示专栏列表
     *
     * @param model
     * @return
     */
    @GetMapping(path = {"list", "/", "", "home"})
    public String list(Model model) {
        PageListVo<ColumnDTO> columns = columnService.listColumn(PageParam.newPageInstance(1L, COLUMN_HOME_PAGE_SIZE));
        List<SideBarDTO> sidebars = sidebarService.queryColumnSidebarList();
        ColumnVo vo = new ColumnVo();
        vo.setColumns(columns);
        vo.setSideBarItems(sidebars);
        model.addAttribute("vo", vo);
        model.addAttribute("columnPageSize", COLUMN_HOME_PAGE_SIZE);
        SpringUtil.getBean(SeoInjectService.class).initColumnHomeSeo(columns.getList());
        return "views/column-home/index";
    }

    /**
     * 专栏详情
     *
     * @param columnId
     * @return
     */
    @GetMapping(path = "{columnKey}")
    public ModelAndView column(@PathVariable("columnKey") String columnKey, Model model) {
        return columnLanding(columnKey, model);
    }

    /**
     * 根路径教程 slug 兼容入口。统一跳转到 /column/{columnSlug}，避免重复收录。
     *
     * @param columnKey
     * @param model
     * @return
     */
    public ModelAndView columnByRootSlug(String columnKey, Model model) {
        ColumnDTO column = columnService.queryColumnInfo(columnKey);
        if (isColumnForbidden(column, model)) {
            return new ModelAndView("views/error/403");
        }
        return new ModelAndView(permanentRedirect(buildColumnRootUrl(column)));
    }

    /**
     * 兼容历史说明页入口，统一跳转到教程介绍页。
     *
     * @param columnKey
     * @param model
     * @return
     */
    public ModelAndView columnReadmeByRootSlug(String columnKey, Model model) {
        ColumnDTO column = columnService.queryColumnInfo(columnKey);
        if (isColumnForbidden(column, model)) {
            return new ModelAndView("views/error/403");
        }
        return new ModelAndView(permanentRedirect(buildColumnRootUrl(column)));
    }

    /**
     * 专栏的文章阅读界面
     *
     * @param columnKey 专栏ID或URL友好的教程标识
     * @param section  旧URL中的节数，从1开始
     * @param model
     * @return
     */
    @GetMapping(path = "{columnKey}/{section:\\d+}")
    public ModelAndView articleBySection(@PathVariable("columnKey") String columnKey,
                                         @PathVariable("section") Integer section,
                                         Model model) {
        if (section <= 0) section = 1;
        ColumnDTO column = columnService.queryBasicColumnInfo(columnKey);
        if (isColumnForbidden(column, model)) {
            return new ModelAndView("views/error/403");
        }

        ColumnArticleDO columnArticle = columnService.queryColumnArticle(column.getColumnId(), section);
        Long articleId = columnArticle.getArticleId();
        ArticleDTO articleDTO = articleReadService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        if (StringUtils.isNotBlank(articleDTO.getUrlSlug())) {
            return new ModelAndView(permanentRedirect(buildColumnArticleUrl(column, columnArticle, articleDTO)));
        }
        return buildColumnArticleView(column, columnArticle, articleDTO, model);
    }

    /**
     * 兼容 /column/{columnSlug}/{articleSlug}。
     *
     * @param columnKey
     * @param articleSlug
     * @param model
     * @return
     */
    @GetMapping(path = "{columnKey}/{articleSlug:[a-z0-9][a-z0-9-]*}")
    public ModelAndView articleBySlugUnderColumn(@PathVariable("columnKey") String columnKey,
                                                 @PathVariable("articleSlug") String articleSlug,
                                                 Model model) {
        return articleByArticleSlug(columnKey, articleSlug, model);
    }

    /**
     * 兼容上一版包含文章ID的URL，永久跳转到旧教程章节地址。
     *
     * @param columnKey 专栏ID或URL友好的教程标识
     * @param articleId 文章id
     * @param urlSlug  SEO友好的文章标识
     * @param model
     * @return
     */
    @GetMapping(path = "{columnKey}/{articleId}/{urlSlug}")
    public ModelAndView articleByArticleId(@PathVariable("columnKey") String columnKey,
                                           @PathVariable("articleId") Long articleId,
                                           @PathVariable("urlSlug") String urlSlug,
                                           Model model) {
        ColumnDTO column = columnService.queryBasicColumnInfo(columnKey);
        if (isColumnForbidden(column, model)) {
            return new ModelAndView("views/error/403");
        }

        ColumnArticleDO columnArticle = columnService.queryColumnArticle(column.getColumnId(), articleId);
        ArticleDTO articleDTO = articleReadService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        return new ModelAndView(permanentRedirect(buildColumnArticleUrl(column, columnArticle, articleDTO)));
    }

    /**
     * 兼容教程文章短地址。
     *
     * @param columnSlug 教程URL友好标识
     * @param articleSlug 文章URL友好标识
     * @param model
     * @return
     */
    public ModelAndView articleByArticleSlug(String columnSlug, String articleSlug, Model model) {
        ColumnDTO column = columnService.queryBasicColumnInfo(columnSlug);
        if (isColumnForbidden(column, model)) {
            return new ModelAndView("views/error/403");
        }

        ColumnArticleDO columnArticle = columnService.queryColumnArticle(column.getColumnId(), articleSlug);
        ArticleDTO articleDTO = articleReadService.queryFullArticleInfo(columnArticle.getArticleId(), ReqInfoContext.getReqInfo().getUserId());
        if (StringUtils.isNotBlank(articleDTO.getUrlSlug())) {
            return new ModelAndView(permanentRedirect(buildColumnArticleUrl(column, columnArticle, articleDTO)));
        }
        return buildColumnArticleView(column, columnArticle, articleDTO, model);
    }

    /**
     * 在文章固定短地址下渲染教程阅读上下文。
     *
     * @param articleId 文章id
     * @param model
     * @return
     */
    public ModelAndView articleByRootArticleSlug(Long articleId, Model model) {
        ColumnArticleDO columnArticle = columnService.getColumnArticleRelation(articleId);
        if (columnArticle == null) {
            return new ModelAndView("error/404");
        }
        ColumnDTO column = columnService.queryBasicColumnInfo(columnArticle.getColumnId());
        if (isColumnForbidden(column, model)) {
            return new ModelAndView("views/error/403");
        }

        ArticleDTO articleDTO = articleReadService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        return buildColumnArticleView(column, columnArticle, articleDTO, model);
    }

    private ModelAndView columnLanding(String columnKey, Model model) {
        ColumnDTO dto = columnService.queryColumnInfo(columnKey);
        if (isColumnForbidden(dto, model)) {
            return new ModelAndView("views/error/403");
        }

        List<SimpleArticleDTO> articles = columnService.queryColumnArticles(dto.getColumnId());
        String startArticleUrl = articles.isEmpty()
                ? "#course-catalog"
                : buildColumnArticleUrl(dto, articles.get(0));
        String readmeUrl = buildColumnRootUrl(dto);
        List<ColumnDTO> recommendColumns = recommendColumns(dto);
        String courseIntroHtml = buildCourseIntroHtml(dto);
        model.addAttribute("vo", dto);
        model.addAttribute("articleList", articles);
        model.addAttribute("recommendColumns", recommendColumns);
        model.addAttribute("courseIntroHtml", courseIntroHtml);
        model.addAttribute("startArticleUrl", startArticleUrl);
        model.addAttribute("readmeUrl", readmeUrl);
        model.addAttribute("showReadmeAction", false);
        SpringUtil.getBean(SeoInjectService.class).initColumnLandingSeo(dto, articles);
        markColumnDomain();
        return new ModelAndView("/views/column-index/index");
    }

    private String buildCourseIntroHtml(ColumnDTO column) {
        if (StringUtils.isNotBlank(column.getReadmeContent())) {
            String introMarkdown = normalizeReadmeMarkdown(column.getReadmeContent());
            if (StringUtils.isNotBlank(introMarkdown)) {
                return StrUtil.stabilizeHtmlImages(MarkdownConverter.markdownToHtml(introMarkdown));
            }
        }

        if (StringUtils.isNotBlank(column.getIntroduction())) {
            return MarkdownConverter.markdownToHtml(column.getIntroduction());
        }

        return "<p>这套教程围绕真实项目展开，覆盖项目背景、业务拆解、架构设计、核心技术实现、部署运行、简历写法和面试复盘。适合想用真实项目补齐工程经验、AI 应用经验和面试表达的同学系统学习。</p>";
    }

    private String normalizeReadmeMarkdown(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        String[] lines = markdown.split("\\r?\\n");
        boolean inFrontMatter = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i] == null ? "" : lines[i];
            if (i == 0 && line.equals("---")) {
                inFrontMatter = true;
                continue;
            }
            if (inFrontMatter) {
                if (line.equals("---")) {
                    inFrontMatter = false;
                }
                continue;
            }
            if (line.startsWith("# ")) {
                line = "#" + line;
            }
            builder.append(line).append('\n');
        }

        return builder.toString().trim();
    }

    private List<ColumnDTO> recommendColumns(ColumnDTO current) {
        PageListVo<ColumnDTO> columns = columnService.listColumn(PageParam.newPageInstance(1L, 8L));
        List<ColumnDTO> recommendations = new ArrayList<>();
        for (ColumnDTO column : columns.getList()) {
            if (column == null || Objects.equals(column.getColumnId(), current.getColumnId())) {
                continue;
            }
            if (column.getCount() == null || Objects.equals(column.getCount().getArticleCount(), 0)) {
                continue;
            }
            recommendations.add(column);
            if (recommendations.size() >= 3) {
                break;
            }
        }
        return recommendations;
    }

    private ModelAndView buildColumnArticleView(ColumnDTO column,
                                                ColumnArticleDO columnArticle,
                                                ArticleDTO articleDTO,
                                                Model model) {
        Long articleId = articleDTO.getArticleId();
        // 返回html格式的文档内容
        articleDTO.setContent(StrUtil.stabilizeHtmlImages(MarkdownConverter.markdownToHtml(articleDTO.getContent())));
        // 评论信息
        List<TopCommentDTO> comments = commentReadService.getArticleComments(articleId, PageParam.newPageInstance());
        Integer topCommentTotal = commentReadService.queryTopCommentCount(articleId);

        // 热门评论
        TopCommentDTO hotComment = commentReadService.queryHotComment(articleId);

        List<TopCommentDTO> highlightComment = commentReadService.queryHighlightComments(articleId);

        // 文章列表
        List<SimpleArticleDTO> articles = columnService.queryColumnArticles(column.getColumnId());

        ColumnArticlesDTO vo = new ColumnArticlesDTO();
        vo.setArticle(articleDTO);
        vo.setComments(comments);
        vo.setTopCommentTotal(topCommentTotal);
        vo.setHotComment(hotComment);
        vo.setHighlightComments(highlightComment);
        vo.setColumn(column.getColumnId());
        vo.setColumnSlug(column.getUrlSlug());
        int currentIndex = findArticleIndex(articles, articleId);
        vo.setSection(currentIndex >= 0 ? currentIndex + 1 : columnArticle.getSection());
        vo.setArticleList(articles);

        ArticleOtherDTO other = new ArticleOtherDTO();

        // 教程类型
        updateReadType(other, column, columnArticle, articleDTO, ColumnArticleReadEnum.valueOf(columnArticle.getReadType()));


        // 把是文章翻页的参数封装到这里
        // prev 的 href 和 是否显示的 flag
        ColumnArticleFlipDTO flip = new ColumnArticleFlipDTO();
        flip.setPrevShow(currentIndex > 0);
        if (Boolean.TRUE.equals(flip.getPrevShow())) {
            SimpleArticleDTO prev = articles.get(currentIndex - 1);
            flip.setPrevHref(buildColumnArticleUrl(column, prev));
        }
        // next 的 href 和 是否显示的 flag
        flip.setNextShow(currentIndex >= 0 && currentIndex < articles.size() - 1);
        if (Boolean.TRUE.equals(flip.getNextShow())) {
            SimpleArticleDTO next = articles.get(currentIndex + 1);
            flip.setNextHref(buildColumnArticleUrl(column, next));
        }
        other.setFlip(flip);

        // 放入 model 中
        vo.setOther(other);

        // 打赏用户列表
        if (Objects.equals(articleDTO.getReadType(), ArticleReadTypeEnum.PAY_READ.getType())) {
            vo.setPayUsers(articlePayService.queryPayUsers(articleId));
        } else {
            vo.setPayUsers(Collections.emptyList());
        }
        model.addAttribute("vo", vo);

        SpringUtil.getBean(SeoInjectService.class).initColumnSeo(vo, column);
        markColumnDomain();
        return new ModelAndView("views/column-detail/index");
    }

    private void markColumnDomain() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        request.setAttribute(GlobalInitService.CURRENT_DOMAIN_ATTRIBUTE, "column");
    }

    private boolean isColumnForbidden(ColumnDTO column, Model model) {
        if (column.getState() != ColumnStatusEnum.OFFLINE.getCode()) {
            return false;
        }
        Long loginUserId = ReqInfoContext.getReqInfo().getUserId();
        if (loginUserId != null && loginUserId.equals(column.getAuthor())) {
            return false;
        }
        model.addAttribute("toast", "教程未发布,暂时无法访问");
        return true;
    }

    private int findArticleIndex(List<SimpleArticleDTO> articles, Long articleId) {
        for (int i = 0; i < articles.size(); i++) {
            if (Objects.equals(articles.get(i).getId(), articleId)) {
                return i;
            }
        }
        return -1;
    }

    private String buildColumnRootUrl(ColumnDTO column) {
        String columnSlug = canonicalColumnSlug(column);
        if (StringUtils.isNotBlank(columnSlug)) {
            return "/column/" + columnSlug;
        }
        return "/column/" + column.getColumnId();
    }

    private String buildColumnArticleUrl(ColumnDTO column, SimpleArticleDTO article) {
        if (article == null) {
            return "#course-catalog";
        }
        if (StringUtils.isNotBlank(article.getUrlSlug())) {
            return "/" + article.getUrlSlug();
        }
        if (article.getSort() == null) {
            return "#course-catalog";
        }
        return buildColumnArticleUrl(column, article.getSort());
    }

    private String buildColumnArticleUrl(ColumnDTO column, ColumnArticleDO article, ArticleDTO articleDTO) {
        if (articleDTO != null && StringUtils.isNotBlank(articleDTO.getUrlSlug())) {
            return "/" + articleDTO.getUrlSlug();
        }
        if (article == null || article.getSection() == null) {
            return "#course-catalog";
        }
        return buildColumnArticleUrl(column, article.getSection().intValue());
    }

    private String buildColumnArticleUrl(ColumnDTO column, Integer section) {
        String columnSlug = canonicalColumnSlug(column);
        String columnKey = StringUtils.isNotBlank(columnSlug) ? columnSlug : String.valueOf(column.getColumnId());
        return "/column/" + columnKey + "/" + section;
    }

    private RedirectView permanentRedirect(String url) {
        RedirectView redirectView = new RedirectView(url);
        redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        redirectView.setContextRelative(true);
        return redirectView;
    }

    private String canonicalColumnSlug(ColumnDTO column) {
        return StringUtils.isNotBlank(column.getUrlSlug()) ? column.getUrlSlug() : null;
    }

    /**
     * 对于要求登录阅读的文章进行进行处理
     *
     * @param vo
     * @param column
     * @param articleDTO
     */
    private void updateReadType(ArticleOtherDTO vo,
                                ColumnDTO column,
                                ColumnArticleDO columnArticle,
                                ArticleDTO articleDTO,
                                ColumnArticleReadEnum articleReadEnum) {
        Long loginUser = ReqInfoContext.getReqInfo().getUserId();
        if (loginUser != null && loginUser.equals(articleDTO.getAuthor())) {
            vo.setReadType(ColumnTypeEnum.FREE.getType());
            return;
        }

        if (articleReadEnum == ColumnArticleReadEnum.COLUMN_TYPE) {
            // 专栏中的文章，没有特殊指定时，直接沿用专栏的规则
            if (column.getType() == ColumnTypeEnum.TIME_FREE.getType()) {
                long now = System.currentTimeMillis();
                if (now > column.getFreeEndTime() || now < column.getFreeStartTime()) {
                    vo.setReadType(ColumnTypeEnum.LOGIN.getType());
                } else {
                    vo.setReadType(ColumnTypeEnum.FREE.getType());
                }
            } else {
                vo.setReadType(column.getType());
            }
        } else {
            // 直接使用文章特殊设置的规则
            vo.setReadType(articleReadEnum.getRead());
        }
        // 如果是星球 or 登录阅读时，不返回全量的文章内容
        articleDTO.setContent(trimContent(vo.getReadType(), articleDTO.getContent(), columnArticle.getPreviewPercent()));
        // fix 关于 cover 封面，文章详情的前端已经不显示了，这里直接删除
    }

    /**
     * 文章内容隐藏
     *
     * @param readType
     * @param content
     * @return
     */
    private String trimContent(int readType, String content, Integer previewPercent) {
        if (readType == ColumnTypeEnum.STAR_READ.getType()) {
            // 判断登录用户是否绑定了星球，如果是，则直接阅读完整的专栏内容
            if (ReqInfoContext.getReqInfo().getUser() != null && ReqInfoContext.getReqInfo().getUser().getStarStatus() == UserAIStatEnum.FORMAL) {
                return content;
            }

            // 如果没有绑定星球，则按配置字数返回试看内容
            return StrUtil.safeSubstringHtml(content, previewLengthConfig(previewPercent, globalViewConfig.getZsxqArticleReadCount()));
        }

        if ((readType == ColumnTypeEnum.LOGIN.getType() && ReqInfoContext.getReqInfo().getUserId() == null)) {
            // 如果是登录阅读，但是用户没有登录，则按配置字数返回试看内容
            return StrUtil.safeSubstringHtml(content, previewLengthConfig(previewPercent, globalViewConfig.getNeedLoginArticleReadCount()));
        }

        return content;
    }

    private String previewLengthConfig(Integer previewPercent, String fallbackConfig) {
        if (previewPercent == null || previewPercent <= 0) {
            return fallbackConfig;
        }
        return Math.min(previewPercent, 100) + "%";
    }

}
