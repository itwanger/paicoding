package com.github.paicoding.forum.web.front.search.view;

import com.github.paicoding.forum.web.front.home.helper.IndexRecommendHelper;
import com.github.paicoding.forum.web.front.home.vo.IndexVo;
import com.github.paicoding.forum.web.global.SeoInjectService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 推荐服务接口
 *
 * @author YiHui
 * @date 2022/10/28
 */
@Controller
public class SearchViewController {
    @Autowired
    private IndexRecommendHelper indexRecommendHelper;
    @Autowired
    private SeoInjectService seoInjectService;


    /**
     * 查询文章列表
     *
     * @param model
     */
    @GetMapping(path = "search")
    public String searchArticleList(@RequestParam(name = "key", required = false) String key,
                                    @RequestParam(name = "q", required = false) String q,
                                    Model model) {
        String searchKey = StringUtils.defaultIfBlank(key, q);
        if (isSearchTemplate(searchKey)) {
            searchKey = null;
        }
        if (StringUtils.isBlank(searchKey)) {
            return "redirect:/";
        }
        if (!StringUtils.isBlank(searchKey)) {
            IndexVo vo = indexRecommendHelper.buildSearchVo(searchKey);
            model.addAttribute("vo", vo);
        }
        // 站内搜索结果页 noindex，避免无限的搜索 URL 被谷歌索引为低质量页
        seoInjectService.initNoindexSeo();
        return "views/article-search-list/index";
    }

    private boolean isSearchTemplate(String key) {
        return StringUtils.equals(key, "{search_term_string}");
    }

}
