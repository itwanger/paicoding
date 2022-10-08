package com.github.liuyueyi.forum.web.front.backstage.rest;

import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liuyueyi.forum.service.article.service.ArticleSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文章后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "backstage/article/")
public class ArticleSettingRestController {

    @Autowired
    private ArticleSettingService articleSettingService;

    @ResponseBody
    @GetMapping(path = "operate")
    public ResVo<String> operate(@RequestParam(name = "articleId") Long articleId,
                                 @RequestParam(name = "operateType") Integer operateType) {
        // TODO：参数校验
        articleSettingService.operateArticle(articleId, operateType);
        return ResVo.ok("ok");
    }
}
