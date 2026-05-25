package com.github.paicoding.forum.web.front.article.view;

import com.github.paicoding.forum.api.model.exception.ForumException;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * 普通文章短地址入口。
 *
 * @author Codex
 */
@Controller
public class ArticleSlugViewController {
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private ColumnService columnService;
    @Autowired
    private ArticleViewController articleViewController;
    @Autowired
    private ColumnViewController columnViewController;

    @GetMapping(path = "/{urlSlug:[a-z0-9][a-z0-9-]*}")
    public ModelAndView article(@PathVariable("urlSlug") String urlSlug, Model model) throws IOException {
        try {
            return columnViewController.columnByRootSlug(urlSlug, model);
        } catch (ForumException e) {
            // 不是教程 slug 时，继续按文章短地址解析。
        }

        ArticleDO article = articleDao.getByUrlSlug(urlSlug);
        if (article == null) {
            return new ModelAndView("error/404");
        }
        if (columnService.getColumnArticleRelation(article.getId()) != null) {
            return columnViewController.articleByRootArticleSlug(article.getId(), model);
        }
        return articleViewController.detailByArticleId(article.getId(), model);
    }
}
