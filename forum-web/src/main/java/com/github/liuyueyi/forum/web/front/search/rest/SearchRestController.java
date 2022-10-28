package com.github.liuyueyi.forum.web.front.search.rest;

import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.web.front.search.vo.SearchHintsVo;
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
public class SearchRestController {

    @Autowired
    private ArticleReadService articleReadService;

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
}
