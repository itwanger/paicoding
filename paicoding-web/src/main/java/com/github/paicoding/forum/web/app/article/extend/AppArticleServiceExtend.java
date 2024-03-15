package com.github.paicoding.forum.web.app.article.extend;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.CurrentCommentDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.core.util.MarkdownConverter;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.front.article.vo.ArticleDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author YiHui
 * @date 2024/3/14
 */
@Service
public class AppArticleServiceExtend {

    @Autowired
    private ArticleReadService articleService;

    @Autowired
    private CommentReadService commentService;

    @Autowired
    private UserService userService;

    /**
     * 文章详情
     *
     * @param articleId
     * @return
     */
    public ArticleDetailVo queryDetail(Long articleId) {
        ArticleDetailVo vo = new ArticleDetailVo();
        // 文章相关信息
        ArticleDTO articleDTO = articleService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        // 返回给前端页面时，转换为html格式
//        articleDTO.setContent(MarkdownConverter.markdownToHtml(articleDTO.getContent()));
        articleDTO.setContent((articleDTO.getContent()));
        vo.setArticle(articleDTO);

        // 作者信息
        UserStatisticInfoDTO user = userService.queryUserInfoWithStatistic(articleDTO.getAuthor());
        articleDTO.setAuthorName(user.getUserName());
        articleDTO.setAuthorAvatar(user.getPhoto());
        vo.setAuthor(user);
        return vo;
    }

    /**
     * 查询文章评论
     *
     * @param articleId
     * @param pageParam
     * @return
     */
    public PageListVo<CurrentCommentDTO> queryComments(Long articleId, PageParam pageParam) {
        // 评论信息
        List<CurrentCommentDTO> comments = commentService.queryLatestComments(articleId, pageParam);
        return PageListVo.newVo(comments, pageParam.getPageSize());
    }

}
