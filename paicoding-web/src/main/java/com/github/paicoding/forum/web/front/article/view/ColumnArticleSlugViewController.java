package com.github.paicoding.forum.web.front.article.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

/**
 * 教程文章短地址入口。
 *
 * @author Codex
 */
@Controller
public class ColumnArticleSlugViewController {
    @Autowired
    private ColumnViewController columnViewController;

    @GetMapping(path = "/{columnSlug:[a-z0-9][a-z0-9-]*}/{articleSlug:[a-z0-9][a-z0-9-]*}")
    public ModelAndView article(@PathVariable("columnSlug") String columnSlug,
                                @PathVariable("articleSlug") String articleSlug,
                                Model model) {
        if ("readme".equals(articleSlug)) {
            return columnViewController.columnReadmeByRootSlug(columnSlug, model);
        }
        return columnViewController.articleByArticleSlug(columnSlug, articleSlug, model);
    }
}
