package com.github.paicoding.forum.web.front.search.view;

import com.github.paicoding.forum.web.front.home.helper.IndexRecommendHelper;
import com.github.paicoding.forum.web.front.home.vo.IndexVo;
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


    /**
     * 查询文章列表
     *
     * @param model
     */
    @GetMapping(path = "search")
    public String searchArticleList(@RequestParam(name = "key") String key, Model model) {
        if (!StringUtils.isBlank(key)) {
            IndexVo vo = indexRecommendHelper.buildSearchVo(key);
            model.addAttribute("vo", vo);
        }
        return "views/article-search-list/index";
    }

}
