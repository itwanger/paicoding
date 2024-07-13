package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.service.AuthorWhiteListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 作者白名单服务
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@RestController
@Tag(name = "发布文章作者白名单管理控制器", description = "作者白名单")
@Permission(role = UserRole.ADMIN)
@RequestMapping(path = {"api/admin/author/whitelist"})
public class AuthorWhiteListController {
    @Autowired
    private AuthorWhiteListService articleWhiteListService;

    @GetMapping(path = "get")
    @Operation(summary = "白名单列表", description = "返回作者白名单列表")
    public ResVo<List<BaseUserInfoDTO>> whiteList() {
        return ResVo.ok(articleWhiteListService.queryAllArticleWhiteListAuthors());
    }

    @GetMapping(path = "add")
    @Operation(summary = "添加白名单", description = "将指定作者加入作者白名单列表")
    @Parameter(name = "authorId", description = "传入需要添加白名单的作者UserId", required = true, allowEmptyValue = false, example = "1")
    public ResVo<Boolean> addAuthor(@RequestParam("authorId") Long authorId) {
        articleWhiteListService.addAuthor2ArticleWhitList(authorId);
        return ResVo.ok(true);
    }

    @GetMapping(path = "remove")
    @Operation(summary = "删除白名单", description = "将作者从白名单列表")
    @Parameter(name = "authorId", description = "传入需要删除白名单的作者UserId", required = true, allowEmptyValue = false, example = "1")
    public ResVo<Boolean> rmAuthor(@RequestParam("authorId") Long authorId) {
        articleWhiteListService.removeAuthorFromArticleWhiteList(authorId);
        return ResVo.ok(true);
    }
}
