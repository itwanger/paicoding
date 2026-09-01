package com.github.paicoding.forum.web.front.miniprogram.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ArticleReadTypeEnum;
import com.github.paicoding.forum.api.model.enums.ArticleTypeEnum;
import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.FollowTypeEnum;
import com.github.paicoding.forum.api.model.enums.HomeSelectEnum;
import com.github.paicoding.forum.api.model.enums.OperateTypeEnum;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.SourceTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.ArticlePostReq;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.api.model.vo.comment.dto.BaseCommentDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.SubCommentDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.api.model.vo.comment.vo.SubCommentListVO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserRelationReq;
import com.github.paicoding.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniArticleDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniArticleDetailDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniCommentDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniCommentPageDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniFollowReq;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniLoginReq;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniLoginRes;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniPwdLoginReq;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniProfileReq;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniSearchHintDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniUserDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniUserStatisticsDTO;
import com.github.paicoding.forum.core.mdc.MdcDot;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.MarkdownConverter;
import com.github.paicoding.forum.core.util.StrUtil;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.ArticleWriteService;
import com.github.paicoding.forum.service.article.service.CategoryService;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.comment.service.CommentWriteService;
import com.github.paicoding.forum.service.user.service.UserFootService;
import com.github.paicoding.forum.service.user.service.UserRelationService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.front.article.extra.ArticleReadViewServiceExtend;
import com.github.paicoding.forum.web.front.miniprogram.service.WxMiniProgramAuthService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 微信原生小程序本地/生产共用 API。
 */
@RestController
@RequestMapping(path = "mini/api")
public class WxMiniProgramRestController {
    private static final int MAX_SEARCH_KEY_LENGTH = 64;

    private final WxMiniProgramAuthService authService;
    private final ArticleReadService articleReadService;
    private final ArticleWriteService articleWriteService;
    private final CategoryService categoryService;
    private final CommentReadService commentReadService;
    private final CommentWriteService commentWriteService;
    private final UserFootService userFootService;
    private final UserRelationService userRelationService;
    private final UserService userService;
    private final ArticleReadViewServiceExtend articleReadViewServiceExtend;

    public WxMiniProgramRestController(WxMiniProgramAuthService authService,
                                       ArticleReadService articleReadService,
                                       ArticleWriteService articleWriteService,
                                       CategoryService categoryService,
                                       CommentReadService commentReadService,
                                       CommentWriteService commentWriteService,
                                       UserFootService userFootService,
                                       UserRelationService userRelationService,
                                       UserService userService,
                                       ArticleReadViewServiceExtend articleReadViewServiceExtend) {
        this.authService = authService;
        this.articleReadService = articleReadService;
        this.articleWriteService = articleWriteService;
        this.categoryService = categoryService;
        this.commentReadService = commentReadService;
        this.commentWriteService = commentWriteService;
        this.userFootService = userFootService;
        this.userRelationService = userRelationService;
        this.userService = userService;
        this.articleReadViewServiceExtend = articleReadViewServiceExtend;
    }

    @PostMapping(path = "auth/login")
    public ResVo<WxMiniLoginRes> login(@RequestBody WxMiniLoginReq req) {
        return ResVo.ok(authService.login(req));
    }

    /**
     * 小程序账号密码登录。
     * <p>
     * 入参：{@link WxMiniPwdLoginReq}（username + password）。
     * 返回结构与微信登录 {@link #login(WxMiniLoginReq)} 完全一致。
     */
    @PostMapping(path = "auth/login/pwd")
    public ResVo<WxMiniLoginRes> loginByPassword(@RequestBody WxMiniPwdLoginReq req) {
        return ResVo.ok(authService.loginByPassword(req.getUsername(), req.getPassword()));
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "auth/logout")
    public ResVo<String> logout() {
        authService.logout();
        return ResVo.ok();
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "user/me")
    public ResVo<WxMiniUserDTO> currentUser() {
        return ResVo.ok(authService.currentUser());
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "user/statistics")
    public ResVo<WxMiniUserStatisticsDTO> currentUserStatistics() {
        Long currentUser = ReqInfoContext.getReqInfo().getUserId();
        UserStatisticInfoDTO statistics = userService.queryUserInfoWithStatistic(currentUser);
        return ResVo.ok(toMiniUserStatistics(statistics));
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "user/follows")
    public ResVo<PageListVo<FollowUserInfoDTO>> userFollows(@RequestParam(name = "type") String type,
                                                            @RequestParam(name = "page", required = false, defaultValue = "1") Long page,
                                                            @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        if (!FollowTypeEnum.FOLLOW.getCode().equals(type) && !FollowTypeEnum.FANS.getCode().equals(type)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "关注类型不合法");
        }
        Long currentUser = ReqInfoContext.getReqInfo().getUserId();
        PageParam pageParam = buildPageParam(page, size);
        if (FollowTypeEnum.FOLLOW.getCode().equals(type)) {
            return ResVo.ok(userRelationService.getUserFollowList(currentUser, pageParam));
        }
        PageListVo<FollowUserInfoDTO> fans = userRelationService.getUserFansList(currentUser, pageParam);
        userRelationService.updateUserFollowRelationId(fans, currentUser);
        return ResVo.ok(fans);
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "user/collections")
    public ResVo<PageListVo<WxMiniArticleDTO>> userCollections(@RequestParam(name = "page", required = false, defaultValue = "1") Long page,
                                                               @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        Long currentUser = ReqInfoContext.getReqInfo().getUserId();
        PageListVo<ArticleDTO> articles = articleReadService.queryArticlesByUserAndType(currentUser, buildPageParam(page, size), HomeSelectEnum.COLLECTION);
        return ResVo.ok(toMiniPage(articles));
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "user/reads")
    public ResVo<PageListVo<WxMiniArticleDTO>> userReads(@RequestParam(name = "page", required = false, defaultValue = "1") Long page,
                                                         @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        Long currentUser = ReqInfoContext.getReqInfo().getUserId();
        PageListVo<ArticleDTO> articles = articleReadService.queryArticlesByUserAndType(currentUser, buildPageParam(page, size), HomeSelectEnum.READ);
        return ResVo.ok(toMiniPage(articles));
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "user/articles")
    public ResVo<PageListVo<WxMiniArticleDTO>> userArticles(@RequestParam(name = "type") String type,
                                                            @RequestParam(name = "page", required = false, defaultValue = "1") Long page,
                                                            @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        List<Integer> statuses = resolveOwnerArticleStatuses(type);
        if (statuses == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章类型不合法");
        }
        Long currentUser = ReqInfoContext.getReqInfo().getUserId();
        PageListVo<ArticleDTO> articles = articleReadService.queryArticlesByUserAndStatuses(currentUser, buildPageParam(page, size), statuses);
        return ResVo.ok(toMiniPage(articles));
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "user/articles/{articleId}/publish")
    public ResVo<Map<String, Object>> publishDraftArticle(@PathVariable(name = "articleId") Long articleId) {
        ArticleDO basic = articleReadService.queryBasicArticle(articleId);
        ResVo<Map<String, Object>> invalid = validateOwnedDraftForPublish(basic);
        if (invalid != null) {
            return invalid;
        }
        ArticleDTO article = articleReadService.queryDetailArticleInfo(articleId);
        Long savedArticleId = articleWriteService.saveArticle(buildDraftPublishReq(article, basic), ReqInfoContext.getReqInfo().getUserId());
        ArticleDO saved = articleReadService.queryBasicArticle(savedArticleId);
        Map<String, Object> result = new HashMap<>();
        result.put("articleId", savedArticleId);
        result.put("status", saved == null ? null : saved.getStatus());
        result.put("statusText", saved == null ? null : statusText(saved.getStatus()));
        return ResVo.ok(result);
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "user/profile")
    public ResVo<WxMiniUserDTO> updateProfile(@RequestBody WxMiniProfileReq req) {
        return ResVo.ok(authService.updateProfile(req));
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "user/avatar")
    public ResVo<WxMiniUserDTO> uploadAvatar(HttpServletRequest request) {
        return ResVo.ok(authService.uploadAvatar(request));
    }

    @GetMapping(path = "categories")
    public ResVo<List<CategoryDTO>> categories() {
        List<CategoryDTO> list = new ArrayList<>(Optional.ofNullable(categoryService.loadAllCategories()).orElse(Collections.emptyList()));
        Map<Long, Long> articleCounts = Optional.ofNullable(articleReadService.queryArticleCountsByCategory()).orElse(Collections.emptyMap());
        list.removeIf(c -> c == null || articleCounts.getOrDefault(c.getCategoryId(), 0L) <= 0L);
        list.add(0, new CategoryDTO(0L, CategoryDTO.DEFAULT_TOTAL_CATEGORY));
        return ResVo.ok(list);
    }

    @GetMapping(path = "articles")
    public ResVo<PageListVo<WxMiniArticleDTO>> articleList(@RequestParam(name = "categoryId", required = false, defaultValue = "0") Long categoryId,
                                                           @RequestParam(name = "page", required = false, defaultValue = "1") Long page,
                                                           @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        Long queryCategoryId = categoryId == null || categoryId <= 0 ? null : categoryId;
        PageListVo<ArticleDTO> articles = articleReadService.queryArticlesByCategory(queryCategoryId, buildPageParam(page, size));
        return ResVo.ok(toMiniPage(articles));
    }

    @GetMapping(path = "articles/{articleId}")
    @MdcDot(bizCode = "#articleId")
    public ResVo<WxMiniArticleDetailDTO> articleDetail(@PathVariable(name = "articleId") Long articleId) {
        Long currentUser = ReqInfoContext.getReqInfo() == null ? null : ReqInfoContext.getReqInfo().getUserId();
        ArticleDTO article = articleReadService.queryFullArticleInfo(articleId, currentUser);
        if (article == null) {
            return ResVo.fail(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
        fillAuthorInfo(article);
        // 根据文章阅读权限（登录/付费/星球）截断内容：未达权限时返回预览片段并置 canRead=false
        String content = articleReadViewServiceExtend.formatArticleReadType(article);
        article.setContent(content);
        WxMiniArticleDetailDTO detail = toMiniDetail(article);
        detail.setFollowed(isFollowedByCurrentUser(article.getAuthor(), currentUser));
        return ResVo.ok(detail);
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "users/{authorId}/follow")
    public ResVo<Boolean> followAuthor(@PathVariable(name = "authorId") Long authorId,
                                       @RequestBody WxMiniFollowReq req) {
        if (authorId == null || authorId <= 0 || req == null || req.getFollowed() == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "关注参数不合法");
        }
        Long currentUserId = ReqInfoContext.getReqInfo().getUserId();
        if (Objects.equals(authorId, currentUserId)) {
            return ResVo.fail(StatusEnum.FORBID_ERROR_MIXED, "不能关注自己");
        }
        if (userService.queryBasicUserInfo(authorId) == null) {
            return ResVo.fail(StatusEnum.USER_NOT_EXISTS, authorId);
        }

        UserRelationReq relation = new UserRelationReq();
        relation.setUserId(authorId);
        relation.setFollowed(req.getFollowed());
        userRelationService.saveUserRelation(relation);
        return ResVo.ok(true);
    }

    @GetMapping(path = "search")
    public ResVo<PageListVo<WxMiniArticleDTO>> search(@RequestParam(name = "key", required = false) String key,
                                                      @RequestParam(name = "page", required = false, defaultValue = "1") Long page,
                                                      @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        String searchKey = normalizeSearchKey(key);
        if (StringUtils.isBlank(searchKey)) {
            return ResVo.ok(PageListVo.emptyVo());
        }
        if (StringUtils.length(searchKey) > MAX_SEARCH_KEY_LENGTH) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "搜索关键词过长");
        }
        PageListVo<ArticleDTO> articles = articleReadService.queryArticlesBySearchKey(searchKey, buildPageParam(page, size));
        return ResVo.ok(toMiniPage(articles));
    }

    @GetMapping(path = "search/hint")
    public ResVo<List<WxMiniSearchHintDTO>> searchHint(@RequestParam(name = "key", required = false) String key) {
        String searchKey = normalizeSearchKey(key);
        if (StringUtils.isBlank(searchKey)) {
            return ResVo.ok(Collections.emptyList());
        }
        if (StringUtils.length(searchKey) > MAX_SEARCH_KEY_LENGTH) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "搜索关键词过长");
        }
        List<SimpleArticleDTO> list = Optional.ofNullable(articleReadService.querySimpleArticleBySearchKey(searchKey)).orElse(Collections.emptyList());
        return ResVo.ok(list.stream()
                .filter(Objects::nonNull)
                .map(item -> new WxMiniSearchHintDTO()
                        .setArticleId(item.getId())
                        .setTitle(item.getTitle())
                        .setUrlSlug(item.getUrlSlug()))
                .collect(Collectors.toList()));
    }

    private String normalizeSearchKey(String key) {
        return StringUtils.trim(key);
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "articles/{articleId}/favor")
    @MdcDot(bizCode = "#articleId")
    public ResVo<Boolean> favorArticle(@PathVariable(name = "articleId") Long articleId,
                                       @RequestParam(name = "type") Integer type) throws IOException, TimeoutException {
        OperateTypeEnum operate = OperateTypeEnum.fromCode(type);
        if (!isArticleFavorOperate(operate)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, type + "非法");
        }

        ArticleDO article = articleReadService.queryBasicArticle(articleId);
        if (article == null) {
            return ResVo.fail(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }

        userFootService.favorArticleComment(DocumentTypeEnum.ARTICLE, articleId, article.getUserId(),
                ReqInfoContext.getReqInfo().getUserId(), operate);
        return ResVo.ok(true);
    }

    @GetMapping(path = "articles/{articleId}/comments")
    @MdcDot(bizCode = "#articleId")
    public ResVo<PageListVo<WxMiniCommentDTO>> articleComments(@PathVariable(name = "articleId") Long articleId,
                                                               @RequestParam(name = "page", required = false, defaultValue = "1") Long page,
                                                               @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        if (articleId == null || articleId <= 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        PageParam pageParam = buildPageParam(page, size);
        List<TopCommentDTO> comments = Optional.ofNullable(commentReadService.getArticleComments(articleId, pageParam)).orElse(Collections.emptyList());
        int topCommentTotal = commentReadService.queryTopCommentCount(articleId);

        return ResVo.ok(toMiniCommentPage(comments, pageParam.getPageNum() * pageParam.getPageSize() < topCommentTotal, null));
    }

    @GetMapping(path = "articles/{articleId}/comments/{topCommentId}/children")
    @MdcDot(bizCode = "#articleId")
    public ResVo<PageListVo<WxMiniCommentDTO>> articleCommentChildren(@PathVariable(name = "articleId") Long articleId,
                                                                      @PathVariable(name = "topCommentId") Long topCommentId,
                                                                      @RequestParam(name = "page", required = false, defaultValue = "1") Long page,
                                                                      @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        if (articleId == null || articleId <= 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        if (topCommentId == null || topCommentId <= 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "顶级评论id为空");
        }
        CommentDO top = commentReadService.queryComment(topCommentId);
        if (top == null) {
            return ResVo.fail(StatusEnum.COMMENT_NOT_EXISTS, topCommentId);
        }
        if (!Objects.equals(top.getArticleId(), articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "顶级评论不属于当前文章");
        }
        if (top.getParentCommentId() != null && top.getParentCommentId() > 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "顶级评论不合法");
        }

        PageParam pageParam = buildPageParam(page, size);
        SubCommentListVO children = commentReadService.getSubComments(topCommentId, pageParam);
        PageListVo<WxMiniCommentDTO> result = new PageListVo<>();
        result.setList(Optional.ofNullable(children == null ? null : children.getList()).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .map(this::toMiniSubComment)
                .collect(Collectors.toList()));
        result.setHasMore(children != null && Boolean.TRUE.equals(children.getHasMore()));
        return ResVo.ok(result);
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "articles/{articleId}/comments")
    @MdcDot(bizCode = "#articleId")
    public ResVo<PageListVo<WxMiniCommentDTO>> postArticleComment(@PathVariable(name = "articleId") Long articleId,
                                                                  @RequestBody CommentSaveReq req) {
        if (articleId == null || articleId <= 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        ArticleDO article = articleReadService.queryBasicArticle(articleId);
        if (article == null) {
            return ResVo.fail(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }

        String content = req == null ? null : StringUtils.trim(req.getCommentContent());
        if (StringUtils.isBlank(content)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评论内容为空");
        }
        if (StringUtils.length(content) > 1000) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评论内容过长");
        }

        CommentSaveReq save = new CommentSaveReq();
        save.setArticleId(articleId);
        save.setUserId(ReqInfoContext.getReqInfo().getUserId());
        save.setCommentContent(StringEscapeUtils.escapeHtml3(content));
        ResVo<PageListVo<WxMiniCommentDTO>> invalidReply = fillAndValidateReplyTarget(articleId, req, save);
        if (invalidReply != null) {
            return invalidReply;
        }
        save.setSkipAiTrigger(true);
        Long submittedCommentId = commentWriteService.saveComment(save);
        PageParam pageParam = buildPageParam(1L, 10L);
        List<TopCommentDTO> comments = Optional.ofNullable(commentReadService.getArticleComments(articleId, pageParam)).orElse(Collections.emptyList());
        int topCommentTotal = commentReadService.queryTopCommentCount(articleId);
        return ResVo.ok(toMiniCommentPage(comments, pageParam.getPageNum() * pageParam.getPageSize() < topCommentTotal, submittedCommentId));
    }

    private ResVo<PageListVo<WxMiniCommentDTO>> fillAndValidateReplyTarget(Long articleId, CommentSaveReq source, CommentSaveReq target) {
        Long parentCommentId = source == null ? null : source.getParentCommentId();
        Long topCommentId = source == null ? null : source.getTopCommentId();
        boolean hasParent = parentCommentId != null && parentCommentId > 0;
        boolean hasTop = topCommentId != null && topCommentId > 0;
        if (!hasParent && !hasTop) {
            return null;
        }
        if (!hasParent) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "父评论id为空");
        }

        CommentDO parent = commentReadService.queryComment(parentCommentId);
        if (parent == null) {
            return ResVo.fail(StatusEnum.COMMENT_NOT_EXISTS, parentCommentId);
        }
        if (!Objects.equals(parent.getArticleId(), articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "父评论不属于当前文章");
        }

        Long expectedTopCommentId = parent.getTopCommentId() == null || parent.getTopCommentId() <= 0 ? parent.getId() : parent.getTopCommentId();
        if (hasTop && !Objects.equals(topCommentId, expectedTopCommentId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "顶级评论不匹配");
        }
        CommentDO top = commentReadService.queryComment(expectedTopCommentId);
        if (top == null) {
            return ResVo.fail(StatusEnum.COMMENT_NOT_EXISTS, expectedTopCommentId);
        }
        if (!Objects.equals(top.getArticleId(), articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "顶级评论不属于当前文章");
        }
        if (top.getParentCommentId() != null && top.getParentCommentId() > 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "顶级评论不合法");
        }

        target.setParentCommentId(parentCommentId);
        target.setTopCommentId(expectedTopCommentId);
        return null;
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "articles/{articleId}/comments/{commentId}/delete")
    @MdcDot(bizCode = "#articleId")
    public ResVo<PageListVo<WxMiniCommentDTO>> deleteArticleComment(@PathVariable(name = "articleId") Long articleId,
                                                                    @PathVariable(name = "commentId") Long commentId) {
        if (articleId == null || articleId <= 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        if (commentId == null || commentId <= 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评论id为空");
        }
        CommentDO comment = commentReadService.queryComment(commentId);
        if (comment == null) {
            return ResVo.fail(StatusEnum.COMMENT_NOT_EXISTS, commentId);
        }
        if (!Objects.equals(comment.getArticleId(), articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评论不属于当前文章");
        }
        commentWriteService.deleteComment(commentId, ReqInfoContext.getReqInfo().getUserId());
        return articleComments(articleId, 1L, 10L);
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "articles/{articleId}/comments/{commentId}/favor")
    @MdcDot(bizCode = "#articleId")
    public ResVo<PageListVo<WxMiniCommentDTO>> favorArticleComment(@PathVariable(name = "articleId") Long articleId,
                                                                   @PathVariable(name = "commentId") Long commentId,
                                                                   @RequestParam(name = "type") Integer type) throws IOException, TimeoutException {
        if (articleId == null || articleId <= 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        if (commentId == null || commentId <= 0) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评论id为空");
        }
        OperateTypeEnum operate = OperateTypeEnum.fromCode(type);
        if (!isCommentFavorOperate(operate)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, type + "非法");
        }
        CommentDO comment = commentReadService.queryComment(commentId);
        if (comment == null) {
            return ResVo.fail(StatusEnum.COMMENT_NOT_EXISTS, commentId);
        }
        if (!Objects.equals(comment.getArticleId(), articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评论不属于当前文章");
        }
        userFootService.favorArticleComment(DocumentTypeEnum.COMMENT, commentId, comment.getUserId(),
                ReqInfoContext.getReqInfo().getUserId(), operate);
        return articleComments(articleId, 1L, 10L);
    }

    private boolean isArticleFavorOperate(OperateTypeEnum operate) {
        return operate == OperateTypeEnum.PRAISE
                || operate == OperateTypeEnum.CANCEL_PRAISE
                || operate == OperateTypeEnum.COLLECTION
                || operate == OperateTypeEnum.CANCEL_COLLECTION;
    }

    private boolean isCommentFavorOperate(OperateTypeEnum operate) {
        return operate == OperateTypeEnum.PRAISE
                || operate == OperateTypeEnum.CANCEL_PRAISE;
    }

    private List<Integer> resolveOwnerArticleStatuses(String type) {
        if ("article".equals(type)) {
            return Arrays.asList(PushStatusEnum.ONLINE.getCode(), PushStatusEnum.REVIEW.getCode());
        }
        if ("draft".equals(type)) {
            return Collections.singletonList(PushStatusEnum.OFFLINE.getCode());
        }
        return null;
    }

    private ResVo<Map<String, Object>> validateOwnedDraftForPublish(ArticleDO article) {
        if (article == null) {
            return ResVo.fail(StatusEnum.ARTICLE_NOT_EXISTS, "文章不存在");
        }
        Long currentUser = ReqInfoContext.getReqInfo().getUserId();
        if (!Objects.equals(article.getUserId(), currentUser)) {
            return ResVo.fail(StatusEnum.FORBID_ERROR_MIXED, "请确认文章是否属于您!");
        }
        if (!Objects.equals(article.getStatus(), PushStatusEnum.OFFLINE.getCode())) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "只有草稿可以发布");
        }
        return null;
    }

    private ArticlePostReq buildDraftPublishReq(ArticleDTO article, ArticleDO basic) {
        ArticlePostReq req = new ArticlePostReq();
        req.setArticleId(article.getArticleId());
        req.setTitle(StringUtils.trim(article.getTitle()));
        req.setShortTitle(StringUtils.trimToEmpty(article.getShortTitle()));
        req.setSummary(StringUtils.trimToEmpty(article.getSummary()));
        req.setCover(StringUtils.trimToEmpty(article.getCover()));
        req.setUrlSlug(article.getUrlSlug());
        req.setCategoryId(article.getCategory() == null ? null : article.getCategory().getCategoryId());
        req.setContent(article.getContent());
        req.setSourceUrl(StringUtils.trimToEmpty(article.getSourceUrl()));
        req.setArticleType(ArticleTypeEnum.BLOG.name());
        req.setSource(basic == null || basic.getSource() == null ? SourceTypeEnum.ORIGINAL.getCode() : basic.getSource());
        req.setReadType(article.getReadType() == null ? ArticleReadTypeEnum.NORMAL.getType() : article.getReadType());
        Set<Long> tagIds = article.getTags() == null
                ? Collections.emptySet()
                : article.getTags().stream().filter(Objects::nonNull).map(TagDTO::getTagId).collect(Collectors.toSet());
        req.setTagIds(tagIds);
        req.setActionType("post");
        return req;
    }

    private PageParam buildPageParam(Long page, Long size) {
        long safePage = page == null || page <= 0 ? PageParam.DEFAULT_PAGE_NUM : page;
        long safeSize = size == null || size <= 0 ? PageParam.DEFAULT_PAGE_SIZE : Math.min(size, 20L);
        return PageParam.newPageInstance(safePage, safeSize);
    }

    private PageListVo<WxMiniArticleDTO> toMiniPage(PageListVo<ArticleDTO> source) {
        if (source == null) {
            return PageListVo.emptyVo();
        }
        PageListVo<WxMiniArticleDTO> result = new PageListVo<>();
        result.setList(Optional.ofNullable(source.getList()).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .map(this::toMiniArticle)
                .collect(Collectors.toList()));
        result.setHasMore(Boolean.TRUE.equals(source.getHasMore()));
        return result;
    }

    private WxMiniCommentPageDTO toMiniCommentPage(List<TopCommentDTO> comments, boolean hasMore, Long submittedCommentId) {
        WxMiniCommentPageDTO result = new WxMiniCommentPageDTO();
        result.setList(Optional.ofNullable(comments).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .map(this::toMiniComment)
                .collect(Collectors.toList()));
        result.setHasMore(hasMore);
        result.setSubmittedCommentId(submittedCommentId);
        return result;
    }

    private WxMiniCommentDTO toMiniComment(TopCommentDTO comment) {
        WxMiniCommentDTO result = fillMiniCommentBase(new WxMiniCommentDTO(), comment)
                .setChildCommentCount(comment.getChildCommentCount())
                .setHasMoreChild(Boolean.TRUE.equals(comment.getHasMoreChild()));
        result.setChildComments(Optional.ofNullable(comment.getChildComments()).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .map(this::toMiniSubComment)
                .collect(Collectors.toList()));
        return result;
    }

    private WxMiniCommentDTO toMiniSubComment(SubCommentDTO comment) {
        return fillMiniCommentBase(new WxMiniCommentDTO(), comment)
                .setChildCommentCount(comment.getCommentCount())
                .setHasMoreChild(false)
                .setChildComments(Collections.emptyList());
    }

    private WxMiniCommentDTO fillMiniCommentBase(WxMiniCommentDTO target, BaseCommentDTO source) {
        return target.setCommentId(source.getCommentId())
                .setUserId(source.getUserId())
                .setUserName(source.getUserName())
                .setUserPhoto(source.getUserPhoto())
                .setCommentTime(source.getCommentTime())
                .setCommentTimeStr(source.getCommentTimeStr())
                .setCommentContent(source.getCommentContent())
                .setPraiseCount(source.getPraiseCount())
                .setPraised(Boolean.TRUE.equals(source.getPraised()));
    }

    private WxMiniArticleDetailDTO toMiniDetail(ArticleDTO article) {
        WxMiniArticleDTO base = toMiniArticle(article);
        WxMiniArticleDetailDTO detail = new WxMiniArticleDetailDTO();
        detail.setArticleId(base.getArticleId());
        detail.setTitle(base.getTitle());
        detail.setShortTitle(base.getShortTitle());
        detail.setSummary(base.getSummary());
        detail.setCover(base.getCover());
        detail.setUrlSlug(base.getUrlSlug());
        detail.setAuthorId(base.getAuthorId());
        detail.setAuthorName(base.getAuthorName());
        detail.setAuthorAvatar(base.getAuthorAvatar());
        detail.setCategoryId(base.getCategoryId());
        detail.setCategory(base.getCategory());
        detail.setTags(base.getTags());
        detail.setReadCount(base.getReadCount());
        detail.setPraiseCount(base.getPraiseCount());
        detail.setCollectionCount(base.getCollectionCount());
        detail.setCommentCount(base.getCommentCount());
        detail.setCreateTime(base.getCreateTime());
        detail.setLastUpdateTime(base.getLastUpdateTime());
        detail.setSearchHit(base.getSearchHit());
        // 只压图片体积、不写宽高：mp-html 自带排版，原图宽高会撑破容器
        detail.setContentHtml(StrUtil.compressHtmlImages(MarkdownConverter.markdownToHtml(article.getContent())));
        detail.setPraised(Boolean.TRUE.equals(article.getPraised()));
        detail.setCollected(Boolean.TRUE.equals(article.getCollected()));
        detail.setCommented(Boolean.TRUE.equals(article.getCommented()));
        detail.setCanRead(Boolean.TRUE.equals(article.getCanRead()));
        detail.setReadType(article.getReadType());
        detail.setSourceType(article.getSourceType());
        detail.setSourceUrl(article.getSourceUrl());
        return detail;
    }

    private Boolean isFollowedByCurrentUser(Long authorId, Long currentUserId) {
        if (authorId == null || currentUserId == null || Objects.equals(authorId, currentUserId)) {
            return false;
        }
        return userRelationService.getFollowedUserId(Collections.singletonList(authorId), currentUserId).contains(authorId);
    }

    private WxMiniArticleDTO toMiniArticle(ArticleDTO article) {
        ArticleFootCountDTO count = article.getCount();
        return new WxMiniArticleDTO()
                .setArticleId(article.getArticleId())
                .setTitle(article.getTitle())
                .setShortTitle(article.getShortTitle())
                .setSummary(article.getSummary())
                .setCover(article.getCover())
                .setUrlSlug(article.getUrlSlug())
                .setAuthorId(article.getAuthor())
                .setAuthorName(article.getAuthorName())
                .setAuthorAvatar(article.getAuthorAvatar())
                .setCategoryId(article.getCategory() == null ? null : article.getCategory().getCategoryId())
                .setCategory(article.getCategory() == null ? null : article.getCategory().getCategory())
                .setTags(article.getTags() == null ? Collections.emptyList() : article.getTags().stream().map(TagDTO::getTag).collect(Collectors.toList()))
                .setStatus(article.getStatus())
                .setStatusText(statusText(article.getStatus()))
                .setReadCount(count == null ? 0 : count.getReadCount())
                .setPraiseCount(count == null ? 0 : count.getPraiseCount())
                .setCollectionCount(count == null ? 0 : count.getCollectionCount())
                .setCommentCount(count == null ? 0 : count.getCommentCount())
                .setCreateTime(article.getCreateTime())
                .setLastUpdateTime(article.getLastUpdateTime())
                .setSearchHit(article.getSearchHit());
    }

    private String statusText(Integer status) {
        if (Objects.equals(status, PushStatusEnum.REVIEW.getCode())) {
            return "审核中";
        }
        return status == null ? "" : PushStatusEnum.formCode(status).getDesc();
    }

    private WxMiniUserStatisticsDTO toMiniUserStatistics(UserStatisticInfoDTO statistics) {
        if (statistics == null) {
            return new WxMiniUserStatisticsDTO();
        }
        return new WxMiniUserStatisticsDTO()
                .setJoinDayCount(valueOrZero(statistics.getJoinDayCount()))
                .setFollowCount(valueOrZero(statistics.getFollowCount()))
                .setFansCount(valueOrZero(statistics.getFansCount()))
                .setArticleCount(valueOrZero(statistics.getArticleCount()))
                .setPraiseCount(valueOrZero(statistics.getPraiseCount()))
                .setReadCount(valueOrZero(statistics.getReadCount()))
                .setCollectionCount(valueOrZero(statistics.getCollectionCount()));
    }

    private Integer valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private void fillAuthorInfo(ArticleDTO article) {
        if (article == null || article.getAuthor() == null || StringUtils.isNotBlank(article.getAuthorName())) {
            return;
        }
        BaseUserInfoDTO author = userService.queryBasicUserInfo(article.getAuthor());
        if (author != null) {
            article.setAuthorName(author.getUserName());
            article.setAuthorAvatar(author.getPhoto());
        }
    }
}
