package com.github.paicoding.forum.web.admin.rest.knowledge;

import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeCategoryReq;
import com.github.paicoding.forum.api.model.vo.knowledge.SearchKnowledgeCategoryReq;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeCategoryDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@Permission(role = UserRole.LOGIN)
@RequestMapping(path = {"api/admin/knowledge/category/", "admin/knowledge/category/"})
@RequiredArgsConstructor
public class KnowledgeCategorySettingRestController {

    private final KnowledgeCategoryService knowledgeCategoryService;

    @PostMapping("list")
    public ResVo<PageVo<KnowledgeCategoryDTO>> list(@RequestBody SearchKnowledgeCategoryReq req) {
        return ResVo.ok(knowledgeCategoryService.queryAdminPage(req));
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping("save")
    public ResVo<String> save(@RequestBody KnowledgeCategoryReq req) {
        knowledgeCategoryService.saveCategory(req);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping("delete")
    public ResVo<String> delete(@RequestParam Long categoryId) {
        knowledgeCategoryService.deleteCategory(categoryId);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping("operate")
    public ResVo<String> operate(@RequestParam Long categoryId, @RequestParam Integer status) {
        knowledgeCategoryService.operateCategory(categoryId, status);
        return ResVo.ok("ok");
    }
}
