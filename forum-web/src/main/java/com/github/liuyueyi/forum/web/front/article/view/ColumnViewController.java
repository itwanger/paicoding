package com.github.liuyueyi.forum.web.front.article.view;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.exception.ExceptionUtil;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnArticlesDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.service.article.service.ColumnService;
import com.github.liuyueyi.forum.service.comment.service.CommentReadService;
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

    @GetMapping(path = {"list", "/", ""})
    public String list(Model model) {
        PageListVo<ColumnDTO> vo = columnService.listColumn(PageParam.newPageInstance());
        model.addAttribute("vo", vo);
        return "biz/column/index";
    }


    /**
     * 专栏详情
     *
     * @param columnId
     * @return
     */
    @GetMapping(path = "{columnId}")
    public String column(@PathVariable("columnId") Long columnId, Model model) {
        ColumnDTO dto = columnService.queryColumnInfo(columnId);
        if (dto == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "专栏不存在");
        }
        model.addAttribute("vo", dto);
        return "/biz/column/detail";
    }


    /**
     * 专栏的文章阅读界面
     *
     * @param columnId 专栏id
     * @param section  节数，从1开始
     * @param model
     * @return
     */
    @GetMapping(path = "{columnId}/{section}")
    public String articles(@PathVariable("columnId") Long columnId, @PathVariable("section") Integer section, Model model) {
        if (section <= 0) section = 1;
        Long articleId = columnService.queryColumnArticle(columnId, section);
        // 文章信息
        ArticleDTO articleDTO = articleReadService.queryTotalArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());

        // 评论信息
        List<TopCommentDTO> comments = commentReadService.getArticleComments(articleId, PageParam.newPageInstance());

        // 文章列表
        List<SimpleArticleDTO> articles = columnService.queryColumnArticles(columnId);

        ColumnArticlesDTO vo = new ColumnArticlesDTO();
        vo.setArticle(articleDTO);
        vo.setComments(comments);
        vo.setColumn(columnId);
        vo.setArticleList(articles);
        model.addAttribute("vo", vo);
        return "biz/column/article";
    }
}
