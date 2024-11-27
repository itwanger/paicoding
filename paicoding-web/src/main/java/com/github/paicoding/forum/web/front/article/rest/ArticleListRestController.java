
package com.github.paicoding.forum.web.front.article.rest;

import com.github.paicoding.forum.api.model.vo.NextPageHtmlVo;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.web.component.TemplateEngineHelper;
import com.github.paicoding.forum.web.global.BaseViewController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章列表
 *
 * @author yihui
 */
@RequestMapping(path = "article/api/list")
@RestController
public class ArticleListRestController extends BaseViewController {
    @Autowired
    private ArticleReadService articleService;
    @Autowired
    private TemplateEngineHelper templateEngineHelper;

    /**
     * 分类下的文章列表
     *
     * @param categoryId 类目id
     * @param page 请求页
     * @param size 分页数
     * @return 文章列表
     */
    @GetMapping(path = "data/category/{category}")
    public ResVo<PageListVo<ArticleDTO>> categoryDataList(@PathVariable("category") Long categoryId,
                                                          @RequestParam(name = "page") Long page,
                                                          @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = buildPageParam(page, size);
        PageListVo<ArticleDTO> list = articleService.queryArticlesByCategory(categoryId, pageParam);
        return ResVo.ok(list);
    }


    /**
     * 分类下的文章列表
     *
     * @param categoryId
     * @return
     */
    @GetMapping(path = "category/{category}")
    public ResVo<NextPageHtmlVo> categoryList(@PathVariable("category") Long categoryId,
                                              @RequestParam(name = "page") Long page,
                                              @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = buildPageParam(page, size);
        PageListVo<ArticleDTO> list = articleService.queryArticlesByCategory(categoryId, pageParam);
        String html = templateEngineHelper.renderToVo("views/article-category-list/article/list", "articles", list);
        return ResVo.ok(new NextPageHtmlVo(html, list.getHasMore()));
    }

    /**
     * 标签下的文章列表
     *
     * @param tagId
     * @param page
     * @param size
     * @return
     */
    @GetMapping(path = "tag/{tag}")
    public ResVo<NextPageHtmlVo> tagList(@PathVariable("tag") Long tagId,
                                         @RequestParam(name = "page") Long page,
                                         @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = buildPageParam(page, size);
        PageListVo<ArticleDTO> list = articleService.queryArticlesByTag(tagId, pageParam);
        String html = templateEngineHelper.renderToVo("views/article-tag-list/article/list", "articles", list);
        return ResVo.ok(new NextPageHtmlVo(html, list.getHasMore()));
    }
}
