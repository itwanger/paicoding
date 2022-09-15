package com.github.liuyueyi.forum.web.front.article.view;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.exception.ExceptionUtil;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.service.article.service.ColumnService;
import com.github.liuyueyi.forum.service.comment.service.CommentReadService;
import com.github.liuyueyi.forum.web.front.article.vo.ArticleDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
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

    @GetMapping(path = "list")
    public String list(Model model) {
        PageListVo<ColumnDTO> vo = columnService.listColumn(PageParam.newPageInstance());
        model.addAttribute("vo", vo);
        return "biz/column/index";
    }


    @GetMapping(path = "{columnId}/{articleId}")
    public String detail(@PathVariable("columnId") Long columnId, @PathVariable("articleId") Long articleId, Model model) {
        if (!columnService.checkColumnArticle(columnId, articleId)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "专栏内没有这篇文章哦~");
        }

        ArticleDetailVo vo = new ArticleDetailVo();
        // 文章信息
        ArticleDTO articleDTO = articleReadService.queryTotalArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        vo.setArticle(articleDTO);

        // 评论信息
        List<TopCommentDTO> comments = commentReadService.getArticleComments(articleId, PageParam.newPageInstance(1L, 10L));
        vo.setComments(comments);

        model.addAttribute("vo", vo);
        return "biz/column/detail";
    }
}
