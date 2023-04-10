package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.service.ArticleWhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 作者白名单服务
 *
 * @author YiHui
 * @date 2023/4/9
 */
@RestController
@Permission(role = UserRole.ADMIN)
@RequestMapping(path = {"api/admin/whitelist"})
public class ArticleWhiteListContoller {
    @Autowired
    private ArticleWhiteListService articleWhiteListService;

    @GetMapping(path = "get")
    public ResVo<List<BaseUserInfoDTO>> whiteList() {
        return ResVo.ok(articleWhiteListService.queryAllArticleWhiteListAuthors());
    }

    @RequestMapping(path = "add")
    public ResVo<Boolean> addAuthor(@RequestParam("authorId") Long authorId) {
        articleWhiteListService.addAuthor2ArticleWhitList(authorId);
        return ResVo.ok(true);
    }

    @RequestMapping(path = "remove")
    public ResVo<Boolean> rmAtuhor(@RequestParam("authorId") Long authorId) {
        articleWhiteListService.removeAuthorFromArticelWhiteList(authorId);
        return ResVo.ok(true);
    }
}
