package com.github.paicoding.forum.web.controller.article.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.enums.OperateTypeEnum;
import com.github.paicoding.forum.api.model.event.MessageQueueEvent;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.ArticlePostReq;
import com.github.paicoding.forum.api.model.vo.article.ContentPostReq;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleOtherDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.api.model.vo.article.response.CategoryArticlesResponseDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.paicoding.forum.api.model.vo.recommend.SideBarDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.core.common.CommonConstants;
import com.github.paicoding.forum.core.mdc.MdcDot;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.service.*;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.notify.service.RabbitmqService;
import com.github.paicoding.forum.service.sidebar.service.SidebarService;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.github.paicoding.forum.service.user.service.UserFootService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.controller.article.vo.ArticleDetailVo;
import com.github.paicoding.forum.web.controller.article.vo.ArticleEditVo;
import com.github.paicoding.forum.web.controller.home.helper.IndexRecommendHelper;
import com.github.paicoding.forum.web.global.vo.ResultVo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * 返回json格式数据
 *
 * @author XuYifei
 * @date 2024/7/6
 */
@Slf4j
@RequestMapping(path = "article/api")
@RestController
public class ArticleRestController {
    @Autowired
    private ArticleReadService articleReadService;
    @Autowired
    private UserFootService userFootService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TagService tagService;
    @Autowired
    private ArticleReadService articleService;
    @Autowired
    private ArticleWriteService articleWriteService;

    @Autowired
    private ArticleRecommendService articleRecommendService;

    @Autowired
    private RabbitmqService rabbitmqService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentReadService commentService;

    @Autowired
    private SidebarService sidebarService;

    @Autowired
    private ColumnService columnService;

    @Autowired
    IndexRecommendHelper indexRecommendHelper;

    /**
     * 文章详情页
     * - 参数解析知识点
     *
     * @param articleId
     * @return
     */
//    @GetMapping("/data/detail/{articleId}")
//    public ResultVo<ArticleDetailVo> detail(@PathVariable(name = "articleId") Long articleId) throws IOException {
//        ArticleDetailVo vo = new ArticleDetailVo();
//        // 文章相关信息
//        ArticleDTO articleDTO = articleService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
//        // 返回给前端页面时，转换为html格式
//        articleDTO.setContent(MarkdownConverter.markdownToHtml(articleDTO.getContent()));
//        vo.setArticle(articleDTO);
//
//        // 作者信息
//        BaseUserInfoDTO user = userService.queryBasicUserInfo(articleDTO.getAuthor());
//        articleDTO.setAuthorName(user.getUserName());
//        articleDTO.setAuthorAvatar(user.getPhoto());
//        return ResultVo.ok(vo);
//    }

    /**
     * 文章详情页
     *
     * @param articleId
     * @return
     */
    @GetMapping("/data/detail/{articleId}")
    public ResultVo<ArticleDetailVo> detailOriginalMarkdown(@PathVariable(name = "articleId") Long articleId) throws IOException {
        // 针对专栏文章，做一个重定向
        ColumnArticleDO columnArticle = columnService.getColumnArticleRelation(articleId);
        ArticleDetailVo vo = new ArticleDetailVo();

        if (columnArticle != null) {
            vo.setColumnId(columnArticle.getColumnId());
            vo.setSectionId(columnArticle.getSection());
            return ResultVo.ok(vo, true);
        }

        // 文章相关信息
        ArticleDTO articleDTO = articleService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        // 返回给前端页面时，转换为html格式
        articleDTO.setContent(articleDTO.getContent());
        vo.setArticle(articleDTO);

        // 评论信息
        List<TopCommentDTO> comments = commentService.getArticleComments(articleId, PageParam.newPageInstance(1L, 10L));
        vo.setComments(comments);

        // 热门评论
        TopCommentDTO hotComment = commentService.queryHotComment(articleId);
        vo.setHotComment(hotComment);

        // 其他信息封装
        ArticleOtherDTO other = new ArticleOtherDTO();
        // 作者信息
        UserStatisticInfoDTO user = userService.queryUserInfoWithStatistic(articleDTO.getAuthor());
        articleDTO.setAuthorName(user.getUserName());
        articleDTO.setAuthorAvatar(user.getPhoto());
        vo.setAuthor(user);

        vo.setOther(other);

        // 详情页的侧边推荐信息
        List<SideBarDTO> sideBars = sidebarService.queryArticleDetailSidebarList(articleDTO.getAuthor(), articleDTO.getArticleId());
        vo.setSideBarItems(sideBars);
        return ResultVo.ok(vo);
    }

    /**
     * 查询所有的标签
     *
     * @return
     */
    @PostMapping(path = "generateSummary")
    public ResVo<String> generateSummary(@RequestBody ContentPostReq req) {
        return ResVo.ok(articleService.generateSummary(req.getContent()));
    }

    /**
     * 查询所有的标签
     *
     * @return
     */
    @GetMapping(path = "tag/list")
    public ResVo<PageVo<TagDTO>> queryTags(@RequestParam(name = "key", required = false) String key,
                                           @RequestParam(name = "pageNumber", required = false, defaultValue = "1") Integer pageNumber,
                                           @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        PageVo<TagDTO> tagDTOPageVo = tagService.queryTags(key, PageParam.newPageInstance(pageNumber, pageSize));
        return ResVo.ok(tagDTOPageVo);
    }

    /**
     * 获取所有的分类
     *
     * @return
     */
    @GetMapping(path = "category/list")
    public ResVo<List<CategoryDTO>> getCategoryList(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                                    @RequestParam(name = "ignoreNoArticles", required = false) Boolean ignoreNoArticles) {
        List<CategoryDTO> list = categoryService.loadAllCategories();
        if (Objects.equals(Boolean.TRUE, ignoreNoArticles)) {
            // 查询所有分类的对应的文章数
            Map<Long, Long> articleCnt = articleService.queryArticleCountsByCategory();
            // 过滤掉文章数为0的分类
            list.removeIf(c -> articleCnt.getOrDefault(c.getCategoryId(), 0L) <= 0L);
        }
//        list.forEach(c -> c.setSelected(c.getCategoryId().equals(categoryId)));
        return ResVo.ok(list);
    }

    /**
     * 获取指定分类下的文章信息
     */
    @GetMapping("/articles/category")
    public ResultVo<CategoryArticlesResponseDTO> getArticlesByCategory(@RequestParam(name = "category", required = false) String category,
                                                                       @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                                       @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {

        // 搜索对应的文章
        IPage<ArticleDTO> articles = articleService.queryArticlesByCategoryPagination(currentPage, pageSize, category);

        List<CategoryDTO> categories = categoryService.loadAllCategories();
        // 查询所有分类的对应的文章数
        Map<Long, Long> articleCnt = articleService.queryArticleCountsByCategory();
        // 过滤掉文章数为0的分类
        categories.removeIf(c -> articleCnt.getOrDefault(c.getCategoryId(), 0L) <= 0L);

        CategoryDTO selectedCategory = categories.stream().filter(c -> c.getCategory().equals(category)).findFirst().orElse(null);
        selectedCategory = selectedCategory == null ? CategoryDTO.DEFAULT_CATEGORY : selectedCategory;
        List<ArticleDTO> topArticles = indexRecommendHelper.topArticleList(selectedCategory);

        CategoryArticlesResponseDTO responseDTO = new CategoryArticlesResponseDTO(articles, categories, topArticles);
        return ResultVo.ok(responseDTO);
    }

    /**
     * 获取指定分类下的文章信息
     */
    @GetMapping("/articles/tag")
    public ResultVo<IPage<ArticleDTO>> getArticlesByTag(@RequestParam(name = "tagId", required = false) Long tagId,
                                                             @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                             @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        IPage<ArticleDTO> articles = articleService.queryArticlesByTagPagination(currentPage, pageSize, tagId);
        return ResultVo.ok(articles);
    }


    /**
     * 收藏、点赞等相关操作
     *
     * @param articleId
     * @param type      取值来自于 OperateTypeEnum#code
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "favor")
    @MdcDot(bizCode = "#articleId")
    public ResVo<Boolean> favor(@RequestParam(name = "articleId") Long articleId,
                                @RequestParam(name = "type") Integer type) throws IOException, TimeoutException {
        if (log.isDebugEnabled()) {
            log.debug("开始点赞: {}", type);
        }
        OperateTypeEnum operate = OperateTypeEnum.fromCode(type);
        if (operate == OperateTypeEnum.EMPTY) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, type + "非法");
        }

        // 要求文章必须存在
        ArticleDO article = articleReadService.queryBasicArticle(articleId);
        if (article == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在!");
        }

        UserFootDO foot = userFootService.saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleId, article.getUserId(),
                ReqInfoContext.getReqInfo().getUserId(),
                operate);
        // 点赞、收藏消息
        NotifyTypeEnum notifyType = OperateTypeEnum.getNotifyType(operate);

        // 点赞消息走 RabbitMQ，其它走 Java 内置消息机制
        if ((notifyType.equals(NotifyTypeEnum.PRAISE) || notifyType.equals(NotifyTypeEnum.CANCEL_PRAISE)) && rabbitmqService.enabled()) {
            rabbitmqService.publishDirectMsg(new MessageQueueEvent<>(notifyType, foot), CommonConstants.MESSAGE_QUEUE_KEY_NOTIFY);
        } else {
            Optional.ofNullable(notifyType).ifPresent(notify -> SpringUtil.publishEvent(new NotifyMsgEvent<>(this, notify, foot)));
        }

        if (log.isDebugEnabled()) {
            log.info("点赞结束: {}", type);
        }
        return ResVo.ok(true);
    }


    /**
     * 发布文章，完成后跳转到详情页
     *
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "post")
    @MdcDot(bizCode = "#req.articleId")
    public ResVo<Long> post(@RequestBody ArticlePostReq req, HttpServletResponse response) throws IOException {
        Long id = articleWriteService.saveArticle(req, ReqInfoContext.getReqInfo().getUserId());
        // 如果使用后端重定向，可以使用下面两种策略
//        return "redirect:/article/detail/" + id;
//        response.sendRedirect("/article/detail/" + id);
        // 这里采用前端重定向策略
        return ResVo.ok(id);
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "update/{articleId}")
    public ResultVo<ArticleEditVo> update(@PathVariable(name = "articleId") Long articleId) {
        ArticleEditVo vo = new ArticleEditVo();
        if (articleId != null) {
            ArticleDTO article = articleService.queryDetailArticleInfo(articleId);
            vo.setArticle(article);
            if (!Objects.equals(article.getAuthor(), ReqInfoContext.getReqInfo().getUserId())) {
                // 没有权限
                return ResultVo.fail(StatusEnum.NO_PERMISSION, "没有权限");
            }

            List<CategoryDTO> categoryList = categoryService.loadAllCategories();
//            categoryList.forEach(s -> {
//                s.setSelected(s.getCategoryId().equals(article.getCategory().getCategoryId()));
//            });
            vo.setCategories(categoryList);
            vo.setTags(article.getTags());
        } else {
            List<CategoryDTO> categoryList = categoryService.loadAllCategories();
            vo.setCategories(categoryList);
            vo.setTags(Collections.emptyList());
        }
        return ResultVo.ok(vo);
    }


    /**
     * 文章删除
     *
     * @param articleId
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @RequestMapping(path = "delete")
    @MdcDot(bizCode = "#articleId")
    public ResVo<Boolean> delete(@RequestParam(value = "articleId") Long articleId) {
        articleWriteService.deleteArticle(articleId, ReqInfoContext.getReqInfo().getUserId());
        return ResVo.ok(true);
    }
}
