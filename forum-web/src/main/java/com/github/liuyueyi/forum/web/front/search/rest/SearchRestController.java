package com.github.liuyueyi.forum.web.front.search.rest;

import com.github.liueyueyi.forum.api.model.vo.NextPageHtmlVo;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.web.component.TemplateEngineHelper;
import com.github.liuyueyi.forum.web.front.search.vo.SearchHintsVo;
import com.github.liuyueyi.forum.web.global.BaseViewController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 推荐服务接口
 *
 * @author YiHui
 * @date 2022/10/28
 */
@RequestMapping(path = "search/api")
@RestController
public class SearchRestController extends BaseViewController {

    @Autowired
    private ArticleReadService articleReadService;

    @Autowired
    private TemplateEngineHelper templateEngineHelper;

    /**
     * 根据关键词给出搜索下拉框
     *
     * @param key
     */
    @GetMapping(path = "hint")
    public ResVo<SearchHintsVo> recommend(@RequestParam(name = "key", required = false) String key) {
        List<SimpleArticleDTO> list = articleReadService.querySimpleArticleBySearchKey(key);
        SearchHintsVo vo = new SearchHintsVo();
        vo.setKey(key);
        vo.setItems(list);
        return ResVo.ok(vo);
    }


    /**
     * 分类下的文章列表
     *
     * @param key
     * @return
     */
    @GetMapping(path = "list")
    public ResVo<NextPageHtmlVo> searchList(@RequestParam(name = "key", required = false) String key,
                                            @RequestParam(name = "page") Long page,
                                            @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = buildPageParam(page, size);
        PageListVo<ArticleDTO> list = articleReadService.queryArticlesBySearchKey(key, pageParam);
        String html = templateEngineHelper.renderToVo("views/article-search-list/article/list", "articles", list);
        return ResVo.ok(new NextPageHtmlVo(html, list.getHasMore()));
    }
}
