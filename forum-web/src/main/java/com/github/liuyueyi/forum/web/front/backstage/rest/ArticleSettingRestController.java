package com.github.liuyueyi.forum.web.front.backstage.rest;

import com.github.liuyueyi.forum.service.article.service.ArticleSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
