package com.github.paicoding.forum.test.miniprogram;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.HomeSelectEnum;
import com.github.paicoding.forum.api.model.enums.OperateTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.api.model.vo.comment.dto.SubCommentDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.api.model.vo.comment.vo.SubCommentListVO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniArticleDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniArticleDetailDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniCommentDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniCommentPageDTO;
import com.github.paicoding.forum.api.model.vo.wx.mini.WxMiniSearchHintDTO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.CategoryService;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.comment.service.CommentWriteService;
import com.github.paicoding.forum.service.user.service.UserFootService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.front.miniprogram.rest.WxMiniProgramRestController;
import com.github.paicoding.forum.web.front.miniprogram.service.WxMiniProgramAuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class WxMiniProgramRestControllerTest {
    @AfterEach
    public void tearDown() {
        ReqInfoContext.clear();
    }

    @Test
    public void shouldReturnTotalCategoryWhenCategorySourceIsEmpty() {
        CategoryService categoryService = mock(CategoryService.class);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        when(categoryService.loadAllCategories()).thenReturn(null);
        when(articleReadService.queryArticleCountsByCategory()).thenReturn(null);

        ResVo<List<CategoryDTO>> res = newController(articleReadService, categoryService, mock(UserService.class)).categories();

        assertEquals(1, res.getResult().size());
        assertEquals(Long.valueOf(0L), res.getResult().get(0).getCategoryId());
        assertEquals(CategoryDTO.DEFAULT_TOTAL_CATEGORY, res.getResult().get(0).getCategory());
    }

    @Test
    public void shouldFilterNullArticleItemsInList() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        PageListVo<ArticleDTO> source = new PageListVo<>();
        ArticleDTO article = new ArticleDTO();
        article.setArticleId(100L);
        article.setTitle("Java 并发");
        source.setList(Arrays.asList(article, null));
        source.setHasMore(true);
        when(articleReadService.queryArticlesByCategory(isNull(), any())).thenReturn(source);

        ResVo<PageListVo<WxMiniArticleDTO>> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .articleList(0L, 1L, 10L);

        assertEquals(1, res.getResult().getList().size());
        assertEquals(Long.valueOf(100L), res.getResult().getList().get(0).getArticleId());
        assertTrue(res.getResult().getHasMore());
    }

    @Test
    public void shouldReturnEmptyArticlePageWhenServiceListIsNull() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        PageListVo<ArticleDTO> source = new PageListVo<>();
        source.setList(null);
        source.setHasMore(false);
        when(articleReadService.queryArticlesByCategory(isNull(), any())).thenReturn(source);

        ResVo<PageListVo<WxMiniArticleDTO>> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .articleList(0L, 1L, 10L);

        assertTrue(res.getResult().getList().isEmpty());
        assertFalse(res.getResult().getHasMore());
    }

    @Test
    public void shouldTreatNegativeCategoryAsTotalCategory() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        when(articleReadService.queryArticlesByCategory(isNull(), any())).thenReturn(PageListVo.emptyVo());

        ResVo<PageListVo<WxMiniArticleDTO>> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .articleList(-1L, 1L, 10L);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        verify(articleReadService).queryArticlesByCategory(isNull(), any());
    }

    @Test
    public void shouldRenderDetailWhenAuthorProfileIsMissing() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        UserService userService = mock(UserService.class);
        ArticleDTO article = new ArticleDTO();
        article.setArticleId(200L);
        article.setAuthor(300L);
        article.setTitle("小程序上线前检查");
        article.setContent("正文");
        when(articleReadService.queryFullArticleInfo(eq(200L), isNull())).thenReturn(article);
        when(userService.queryBasicUserInfo(300L)).thenReturn(null);

        ResVo<WxMiniArticleDetailDTO> res = newController(articleReadService, mock(CategoryService.class), userService)
                .articleDetail(200L);

        assertEquals(Long.valueOf(200L), res.getResult().getArticleId());
        assertNull(res.getResult().getAuthorName());
        assertTrue(res.getResult().getContentHtml().contains("正文"));
    }

    @Test
    public void shouldReturnArticleMissingWhenDetailDoesNotExist() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        when(articleReadService.queryFullArticleInfo(eq(404L), isNull())).thenReturn(null);

        ResVo<WxMiniArticleDetailDTO> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .articleDetail(404L);

        assertEquals(StatusEnum.ARTICLE_NOT_EXISTS.getCode(), res.getStatus().getCode());
    }

    @Test
    public void shouldReturnEmptySearchHintsWhenServiceReturnsNull() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        when(articleReadService.querySimpleArticleBySearchKey("Java")).thenReturn(null);

        ResVo<?> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .searchHint("Java");

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        assertTrue(((List<?>) res.getResult()).isEmpty());
    }

    @Test
    public void shouldReturnEmptySearchPageForBlankKeyword() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);

        ResVo<PageListVo<WxMiniArticleDTO>> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .search("   ", 1L, 10L);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        assertTrue(res.getResult().getList().isEmpty());
        verifyNoInteractions(articleReadService);
    }

    @Test
    public void shouldRejectTooLongSearchKeyword() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        String tooLongKey = new String(new char[65]).replace('\0', 'a');

        ResVo<PageListVo<WxMiniArticleDTO>> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .search(tooLongKey, 1L, 10L);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(articleReadService);
    }

    @Test
    public void shouldTrimSearchKeywordBeforeQuery() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        when(articleReadService.queryArticlesBySearchKey(eq("Java"), any())).thenReturn(PageListVo.emptyVo());

        ResVo<PageListVo<WxMiniArticleDTO>> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .search(" Java ", 1L, 10L);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        verify(articleReadService).queryArticlesBySearchKey(eq("Java"), any());
    }

    @Test
    public void shouldRejectTooLongSearchHintKeyword() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        String tooLongKey = new String(new char[65]).replace('\0', 'a');

        ResVo<List<WxMiniSearchHintDTO>> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .searchHint(tooLongKey);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(articleReadService);
    }

    @Test
    public void shouldCopyCategoryListBeforeFiltering() {
        CategoryService categoryService = mock(CategoryService.class);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        when(categoryService.loadAllCategories()).thenReturn(Collections.singletonList(new CategoryDTO(1L, "Java")));
        when(articleReadService.queryArticleCountsByCategory()).thenReturn(Collections.singletonMap(1L, 3L));

        ResVo<List<CategoryDTO>> res = newController(articleReadService, categoryService, mock(UserService.class)).categories();

        assertEquals(2, res.getResult().size());
        assertEquals(Long.valueOf(0L), res.getResult().get(0).getCategoryId());
        assertEquals(Long.valueOf(1L), res.getResult().get(1).getCategoryId());
    }

    @Test
    public void shouldFilterNullCategoryItems() {
        CategoryService categoryService = mock(CategoryService.class);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        when(categoryService.loadAllCategories()).thenReturn(Arrays.asList(null, new CategoryDTO(2L, "Spring")));
        when(articleReadService.queryArticleCountsByCategory()).thenReturn(Collections.singletonMap(2L, 1L));

        ResVo<List<CategoryDTO>> res = newController(articleReadService, categoryService, mock(UserService.class)).categories();

        assertEquals(2, res.getResult().size());
        assertEquals(Long.valueOf(0L), res.getResult().get(0).getCategoryId());
        assertEquals(Long.valueOf(2L), res.getResult().get(1).getCategoryId());
    }

    @Test
    public void shouldRejectInvalidFavorType() throws IOException, TimeoutException {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        UserFootService userFootService = mock(UserFootService.class);

        ResVo<Boolean> res = newController(articleReadService, mock(CategoryService.class), userFootService, mock(UserService.class))
                .favorArticle(500L, 999);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(articleReadService, userFootService);
    }

    @Test
    public void shouldRejectNonFavorArticleOperateTypes() throws IOException, TimeoutException {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        UserFootService userFootService = mock(UserFootService.class);

        ResVo<Boolean> res = newController(articleReadService, mock(CategoryService.class), userFootService, mock(UserService.class))
                .favorArticle(500L, OperateTypeEnum.READ.getCode());

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(articleReadService, userFootService);
    }

    @Test
    public void shouldReturnArticleMissingWhenFavorTargetDoesNotExist() throws IOException, TimeoutException {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        UserFootService userFootService = mock(UserFootService.class);
        when(articleReadService.queryBasicArticle(501L)).thenReturn(null);

        ResVo<Boolean> res = newController(articleReadService, mock(CategoryService.class), userFootService, mock(UserService.class))
                .favorArticle(501L, OperateTypeEnum.PRAISE.getCode());

        assertEquals(StatusEnum.ARTICLE_NOT_EXISTS.getCode(), res.getStatus().getCode());
        verify(articleReadService).queryBasicArticle(501L);
        verifyNoInteractions(userFootService);
    }

    @Test
    public void shouldFavorArticleWithCurrentMiniProgramUser() throws IOException, TimeoutException {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(901L);
        ReqInfoContext.addReqInfo(reqInfo);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        UserFootService userFootService = mock(UserFootService.class);
        ArticleDO article = new ArticleDO();
        article.setUserId(902L);
        when(articleReadService.queryBasicArticle(502L)).thenReturn(article);

        ResVo<Boolean> res = newController(articleReadService, mock(CategoryService.class), userFootService, mock(UserService.class))
                .favorArticle(502L, OperateTypeEnum.COLLECTION.getCode());

        assertEquals(Boolean.TRUE, res.getResult());
        verify(userFootService).favorArticleComment(
                eq(DocumentTypeEnum.ARTICLE),
                eq(502L),
                eq(902L),
                eq(901L),
                eq(OperateTypeEnum.COLLECTION));
    }

    @Test
    public void shouldReturnCurrentUserCollections() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(901L);
        ReqInfoContext.addReqInfo(reqInfo);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        PageListVo<ArticleDTO> source = new PageListVo<>();
        ArticleDTO article = new ArticleDTO();
        article.setArticleId(601L);
        article.setTitle("收藏文章");
        source.setList(Collections.singletonList(article));
        source.setHasMore(false);
        when(articleReadService.queryArticlesByUserAndType(eq(901L), any(), eq(HomeSelectEnum.COLLECTION))).thenReturn(source);

        ResVo<PageListVo<WxMiniArticleDTO>> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .userCollections(1L, 10L);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        assertEquals(1, res.getResult().getList().size());
        assertEquals(Long.valueOf(601L), res.getResult().getList().get(0).getArticleId());
        assertFalse(res.getResult().getHasMore());
        verify(articleReadService).queryArticlesByUserAndType(eq(901L), any(), eq(HomeSelectEnum.COLLECTION));
    }

    @Test
    public void shouldReturnCurrentUserReadHistory() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(901L);
        ReqInfoContext.addReqInfo(reqInfo);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        PageListVo<ArticleDTO> source = new PageListVo<>();
        ArticleDTO article = new ArticleDTO();
        article.setArticleId(602L);
        article.setTitle("浏览过的文章");
        source.setList(Collections.singletonList(article));
        source.setHasMore(true);
        when(articleReadService.queryArticlesByUserAndType(eq(901L), any(), eq(HomeSelectEnum.READ))).thenReturn(source);

        ResVo<PageListVo<WxMiniArticleDTO>> res = newController(articleReadService, mock(CategoryService.class), mock(UserService.class))
                .userReads(1L, 10L);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        assertEquals(1, res.getResult().getList().size());
        assertEquals(Long.valueOf(602L), res.getResult().getList().get(0).getArticleId());
        assertTrue(res.getResult().getHasMore());
        verify(articleReadService).queryArticlesByUserAndType(eq(901L), any(), eq(HomeSelectEnum.READ));
    }

    @Test
    public void shouldReturnArticleCommentsWithChildrenAndHasMore() {
        CommentReadService commentReadService = mock(CommentReadService.class);
        TopCommentDTO top = new TopCommentDTO();
        top.setCommentId(701L);
        top.setUserId(801L);
        top.setUserName("沉默王二");
        top.setCommentContent("一级评论");
        top.setPraiseCount(3);
        top.setPraised(true);
        top.setChildCommentCount(2);
        top.setHasMoreChild(true);
        SubCommentDTO child = new SubCommentDTO();
        child.setCommentId(702L);
        child.setUserId(802L);
        child.setUserName("读者");
        child.setCommentContent("回复");
        child.setPraiseCount(1);
        top.setChildComments(Collections.singletonList(child));
        when(commentReadService.getArticleComments(eq(600L), any())).thenReturn(Collections.singletonList(top));
        when(commentReadService.queryTopCommentCount(600L)).thenReturn(12);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, mock(CommentWriteService.class), mock(UserService.class))
                .articleComments(600L, 1L, 10L);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        assertTrue(res.getResult().getHasMore());
        assertEquals(1, res.getResult().getList().size());
        assertEquals(Long.valueOf(701L), res.getResult().getList().get(0).getCommentId());
        assertEquals("一级评论", res.getResult().getList().get(0).getCommentContent());
        assertTrue(res.getResult().getList().get(0).getPraised());
        assertEquals(1, res.getResult().getList().get(0).getChildComments().size());
        assertEquals(Long.valueOf(702L), res.getResult().getList().get(0).getChildComments().get(0).getCommentId());
    }

    @Test
    public void shouldRejectInvalidCommentArticleId() {
        CommentReadService commentReadService = mock(CommentReadService.class);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, mock(CommentWriteService.class), mock(UserService.class))
                .articleComments(0L, 1L, 10L);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(commentReadService);
    }

    @Test
    public void shouldReturnMiniProgramCommentChildrenWithPagination() {
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentDO top = new CommentDO();
        top.setId(715L);
        top.setArticleId(615L);
        top.setParentCommentId(0L);
        when(commentReadService.queryComment(715L)).thenReturn(top);
        SubCommentDTO child = new SubCommentDTO();
        child.setCommentId(716L);
        child.setUserId(816L);
        child.setUserName("读者");
        child.setCommentContent("分页回复");
        child.setPraiseCount(2);
        child.setPraised(true);
        SubCommentListVO children = new SubCommentListVO();
        children.setList(Collections.singletonList(child));
        children.setTotal(3);
        children.setHasMore(true);
        when(commentReadService.getSubComments(eq(715L), any())).thenReturn(children);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, mock(CommentWriteService.class), mock(UserService.class))
                .articleCommentChildren(615L, 715L, 2L, 10L);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        assertTrue(res.getResult().getHasMore());
        assertEquals(1, res.getResult().getList().size());
        assertEquals(Long.valueOf(716L), res.getResult().getList().get(0).getCommentId());
        assertEquals("分页回复", res.getResult().getList().get(0).getCommentContent());
        assertTrue(res.getResult().getList().get(0).getPraised());
        verify(commentReadService).getSubComments(eq(715L), argThat(page -> page != null && page.getPageNum() == 2L && page.getPageSize() == 10L));
    }

    @Test
    public void shouldRejectMiniProgramCommentChildrenWhenTopBelongsToDifferentArticle() {
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentDO top = new CommentDO();
        top.setId(717L);
        top.setArticleId(616L);
        top.setParentCommentId(0L);
        when(commentReadService.queryComment(717L)).thenReturn(top);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, mock(CommentWriteService.class), mock(UserService.class))
                .articleCommentChildren(615L, 717L, 1L, 10L);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verify(commentReadService).queryComment(717L);
        verifyNoMoreInteractions(commentReadService);
    }

    @Test
    public void shouldRejectMiniProgramCommentChildrenWhenCommentIsNotTopLevel() {
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentDO child = new CommentDO();
        child.setId(718L);
        child.setArticleId(615L);
        child.setParentCommentId(715L);
        when(commentReadService.queryComment(718L)).thenReturn(child);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, mock(CommentWriteService.class), mock(UserService.class))
                .articleCommentChildren(615L, 718L, 1L, 10L);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verify(commentReadService).queryComment(718L);
        verifyNoMoreInteractions(commentReadService);
    }

    @Test
    public void shouldRejectBlankMiniProgramComment() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        CommentWriteService commentWriteService = mock(CommentWriteService.class);
        ArticleDO article = new ArticleDO();
        article.setUserId(1L);
        when(articleReadService.queryBasicArticle(620L)).thenReturn(article);
        CommentSaveReq req = new CommentSaveReq();
        req.setCommentContent("   ");

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(articleReadService, mock(CategoryService.class),
                mock(CommentReadService.class), commentWriteService, mock(UserService.class))
                .postArticleComment(620L, req);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(commentWriteService);
    }

    @Test
    public void shouldRejectCommentWhenArticleDoesNotExist() {
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        CommentWriteService commentWriteService = mock(CommentWriteService.class);
        when(articleReadService.queryBasicArticle(621L)).thenReturn(null);
        CommentSaveReq req = new CommentSaveReq();
        req.setCommentContent("文章不存在");

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(articleReadService, mock(CategoryService.class),
                mock(CommentReadService.class), commentWriteService, mock(UserService.class))
                .postArticleComment(621L, req);

        assertEquals(StatusEnum.ARTICLE_NOT_EXISTS.getCode(), res.getStatus().getCode());
        verifyNoInteractions(commentWriteService);
    }

    @Test
    public void shouldPostMiniProgramCommentWithCurrentUserAndEscapedContent() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(901L);
        ReqInfoContext.addReqInfo(reqInfo);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentWriteService commentWriteService = mock(CommentWriteService.class);
        ArticleDO article = new ArticleDO();
        article.setUserId(902L);
        when(articleReadService.queryBasicArticle(622L)).thenReturn(article);
        when(commentReadService.getArticleComments(eq(622L), any())).thenReturn(Collections.emptyList());
        when(commentReadService.queryTopCommentCount(622L)).thenReturn(0);
        CommentSaveReq req = new CommentSaveReq();
        req.setCommentContent(" <b>写得好</b> ");
        req.setCommentId(999L);
        when(commentWriteService.saveComment(any())).thenReturn(920L);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(articleReadService, mock(CategoryService.class),
                commentReadService, commentWriteService, mock(UserService.class))
                .postArticleComment(622L, req);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        assertEquals(Long.valueOf(920L), ((WxMiniCommentPageDTO) res.getResult()).getSubmittedCommentId());
        verify(commentWriteService).saveComment(argThat(save ->
                save != null
                        && save.getCommentId() == null
                        && Long.valueOf(622L).equals(save.getArticleId())
                        && Long.valueOf(901L).equals(save.getUserId())
                        && "&lt;b&gt;写得好&lt;/b&gt;".equals(save.getCommentContent())
                        && Boolean.TRUE.equals(save.getSkipAiTrigger())));
        verify(commentReadService).getArticleComments(eq(622L), any());
        verify(commentReadService).queryTopCommentCount(622L);
        verifyNoMoreInteractions(commentWriteService);
    }

    @Test
    public void shouldPostMiniProgramReplyWithParentAndTopCommentIds() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(903L);
        ReqInfoContext.addReqInfo(reqInfo);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentWriteService commentWriteService = mock(CommentWriteService.class);
        ArticleDO article = new ArticleDO();
        article.setUserId(904L);
        when(articleReadService.queryBasicArticle(623L)).thenReturn(article);
        CommentDO parent = new CommentDO();
        parent.setId(701L);
        parent.setArticleId(623L);
        parent.setTopCommentId(700L);
        CommentDO top = new CommentDO();
        top.setId(700L);
        top.setArticleId(623L);
        top.setParentCommentId(0L);
        when(commentReadService.queryComment(701L)).thenReturn(parent);
        when(commentReadService.queryComment(700L)).thenReturn(top);
        when(commentReadService.getArticleComments(eq(623L), any())).thenReturn(Collections.emptyList());
        when(commentReadService.queryTopCommentCount(623L)).thenReturn(0);
        CommentSaveReq req = new CommentSaveReq();
        req.setCommentContent("回复一下");
        req.setParentCommentId(701L);
        req.setTopCommentId(700L);
        when(commentWriteService.saveComment(any())).thenReturn(921L);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(articleReadService, mock(CategoryService.class),
                commentReadService, commentWriteService, mock(UserService.class))
                .postArticleComment(623L, req);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        assertEquals(Long.valueOf(921L), ((WxMiniCommentPageDTO) res.getResult()).getSubmittedCommentId());
        verify(commentWriteService).saveComment(argThat(save ->
                save != null
                        && save.getCommentId() == null
                        && Long.valueOf(623L).equals(save.getArticleId())
                        && Long.valueOf(903L).equals(save.getUserId())
                        && Long.valueOf(701L).equals(save.getParentCommentId())
                        && Long.valueOf(700L).equals(save.getTopCommentId())
                        && Boolean.TRUE.equals(save.getSkipAiTrigger())));
    }

    @Test
    public void shouldRejectMiniProgramReplyWhenParentBelongsToDifferentArticle() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(903L);
        ReqInfoContext.addReqInfo(reqInfo);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentWriteService commentWriteService = mock(CommentWriteService.class);
        when(articleReadService.queryBasicArticle(624L)).thenReturn(new ArticleDO());
        CommentDO parent = new CommentDO();
        parent.setId(710L);
        parent.setArticleId(625L);
        parent.setTopCommentId(710L);
        when(commentReadService.queryComment(710L)).thenReturn(parent);
        CommentSaveReq req = new CommentSaveReq();
        req.setCommentContent("跨文章回复");
        req.setParentCommentId(710L);
        req.setTopCommentId(710L);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(articleReadService, mock(CategoryService.class),
                commentReadService, commentWriteService, mock(UserService.class))
                .postArticleComment(624L, req);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(commentWriteService);
    }

    @Test
    public void shouldRejectMiniProgramReplyWhenTopCommentDoesNotMatchParent() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(903L);
        ReqInfoContext.addReqInfo(reqInfo);
        ArticleReadService articleReadService = mock(ArticleReadService.class);
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentWriteService commentWriteService = mock(CommentWriteService.class);
        when(articleReadService.queryBasicArticle(626L)).thenReturn(new ArticleDO());
        CommentDO parent = new CommentDO();
        parent.setId(711L);
        parent.setArticleId(626L);
        parent.setTopCommentId(700L);
        when(commentReadService.queryComment(711L)).thenReturn(parent);
        CommentSaveReq req = new CommentSaveReq();
        req.setCommentContent("错误顶级评论");
        req.setParentCommentId(711L);
        req.setTopCommentId(712L);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(articleReadService, mock(CategoryService.class),
                commentReadService, commentWriteService, mock(UserService.class))
                .postArticleComment(626L, req);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(commentWriteService);
    }

    @Test
    public void shouldRejectDeletingMissingMiniProgramComment() {
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentWriteService commentWriteService = mock(CommentWriteService.class);
        when(commentReadService.queryComment(800L)).thenReturn(null);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, commentWriteService, mock(UserService.class))
                .deleteArticleComment(700L, 800L);

        assertEquals(StatusEnum.COMMENT_NOT_EXISTS.getCode(), res.getStatus().getCode());
        verifyNoInteractions(commentWriteService);
    }

    @Test
    public void shouldRejectDeletingCommentFromDifferentArticle() {
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentWriteService commentWriteService = mock(CommentWriteService.class);
        CommentDO comment = new CommentDO();
        comment.setArticleId(701L);
        when(commentReadService.queryComment(801L)).thenReturn(comment);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, commentWriteService, mock(UserService.class))
                .deleteArticleComment(700L, 801L);

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(commentWriteService);
    }

    @Test
    public void shouldDeleteMiniProgramCommentWithCurrentUserAndRefreshFirstPage() {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(905L);
        ReqInfoContext.addReqInfo(reqInfo);
        CommentReadService commentReadService = mock(CommentReadService.class);
        CommentWriteService commentWriteService = mock(CommentWriteService.class);
        CommentDO comment = new CommentDO();
        comment.setArticleId(702L);
        when(commentReadService.queryComment(802L)).thenReturn(comment);
        when(commentReadService.getArticleComments(eq(702L), any())).thenReturn(Collections.emptyList());
        when(commentReadService.queryTopCommentCount(702L)).thenReturn(0);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, commentWriteService, mock(UserService.class))
                .deleteArticleComment(702L, 802L);

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        assertTrue(res.getResult().getList().isEmpty());
        verify(commentWriteService).deleteComment(802L, 905L);
        verify(commentReadService).getArticleComments(eq(702L), any());
        verify(commentReadService).queryTopCommentCount(702L);
    }

    @Test
    public void shouldRejectInvalidMiniProgramCommentFavorType() throws IOException, TimeoutException {
        CommentReadService commentReadService = mock(CommentReadService.class);
        UserFootService userFootService = mock(UserFootService.class);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, mock(CommentWriteService.class), userFootService, mock(UserService.class))
                .favorArticleComment(703L, 803L, OperateTypeEnum.COLLECTION.getCode());

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(commentReadService, userFootService);
    }

    @Test
    public void shouldRejectMiniProgramCommentFavorWhenCommentBelongsToDifferentArticle() throws IOException, TimeoutException {
        CommentReadService commentReadService = mock(CommentReadService.class);
        UserFootService userFootService = mock(UserFootService.class);
        CommentDO comment = new CommentDO();
        comment.setArticleId(704L);
        when(commentReadService.queryComment(804L)).thenReturn(comment);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, mock(CommentWriteService.class), userFootService, mock(UserService.class))
                .favorArticleComment(703L, 804L, OperateTypeEnum.PRAISE.getCode());

        assertEquals(StatusEnum.ILLEGAL_ARGUMENTS_MIXED.getCode(), res.getStatus().getCode());
        verifyNoInteractions(userFootService);
    }

    @Test
    public void shouldFavorMiniProgramCommentWithCurrentUserAndRefreshFirstPage() throws IOException, TimeoutException {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(906L);
        ReqInfoContext.addReqInfo(reqInfo);
        CommentReadService commentReadService = mock(CommentReadService.class);
        UserFootService userFootService = mock(UserFootService.class);
        CommentDO comment = new CommentDO();
        comment.setArticleId(705L);
        comment.setUserId(907L);
        when(commentReadService.queryComment(805L)).thenReturn(comment);
        when(commentReadService.getArticleComments(eq(705L), any())).thenReturn(Collections.emptyList());
        when(commentReadService.queryTopCommentCount(705L)).thenReturn(0);

        ResVo<PageListVo<WxMiniCommentDTO>> res = newController(mock(ArticleReadService.class), mock(CategoryService.class),
                commentReadService, mock(CommentWriteService.class), userFootService, mock(UserService.class))
                .favorArticleComment(705L, 805L, OperateTypeEnum.PRAISE.getCode());

        assertEquals(StatusEnum.SUCCESS.getCode(), res.getStatus().getCode());
        verify(userFootService).favorArticleComment(
                eq(DocumentTypeEnum.COMMENT),
                eq(805L),
                eq(907L),
                eq(906L),
                eq(OperateTypeEnum.PRAISE));
        verify(commentReadService).getArticleComments(eq(705L), any());
        verify(commentReadService).queryTopCommentCount(705L);
    }

    private WxMiniProgramRestController newController(ArticleReadService articleReadService,
                                                      CategoryService categoryService,
                                                      UserService userService) {
        return newController(articleReadService, categoryService, mock(UserFootService.class), userService);
    }

    private WxMiniProgramRestController newController(ArticleReadService articleReadService,
                                                      CategoryService categoryService,
                                                      UserFootService userFootService,
                                                      UserService userService) {
        return newController(articleReadService, categoryService, mock(CommentReadService.class), mock(CommentWriteService.class), userFootService, userService);
    }

    private WxMiniProgramRestController newController(ArticleReadService articleReadService,
                                                      CategoryService categoryService,
                                                      CommentReadService commentReadService,
                                                      CommentWriteService commentWriteService,
                                                      UserService userService) {
        return newController(articleReadService, categoryService, commentReadService, commentWriteService, mock(UserFootService.class), userService);
    }

    private WxMiniProgramRestController newController(ArticleReadService articleReadService,
                                                      CategoryService categoryService,
                                                      CommentReadService commentReadService,
                                                      CommentWriteService commentWriteService,
                                                      UserFootService userFootService,
                                                      UserService userService) {
        return new WxMiniProgramRestController(
                mock(WxMiniProgramAuthService.class),
                articleReadService,
                categoryService,
                commentReadService,
                commentWriteService,
                userFootService,
                userService);
    }
}
