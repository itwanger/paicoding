package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.user.SearchZsxqUserReq;
import com.github.paicoding.forum.api.model.vo.user.ZsxqUserBatchOperateReq;
import com.github.paicoding.forum.api.model.vo.user.ZsxqUserPostReq;
import com.github.paicoding.forum.api.model.vo.user.dto.ZsxqUserInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.service.ZsxqWhiteListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 作者白名单服务
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@RestController
@Tag(name = "星球用户白名单管理控制器", description = "星球白名单")
@Permission(role = UserRole.ADMIN)
@RequestMapping(path = {"api/admin/zsxq/whitelist"})
public class ZsxqWhiteListController {
    @Autowired
    private ZsxqWhiteListService zsxqWhiteListService;

    @Operation(summary = "获取知识星球白名单用户列表")
    @PostMapping(path = "")
    public ResVo<PageVo<ZsxqUserInfoDTO>> list(@RequestBody SearchZsxqUserReq req) {
        PageVo<ZsxqUserInfoDTO> articleDTOPageVo = zsxqWhiteListService.getList(req);
        return ResVo.ok(articleDTOPageVo);
    }

    // 改变用户状态，审核通过
    @Operation(summary = "改变用户状态")
    @GetMapping(path = "operate")
    public ResVo<String> operate(@RequestParam(name = "id") Long id,
                                  @RequestParam(name = "status") Integer status) {
        UserAIStatEnum operate = UserAIStatEnum.fromCode(status);
        zsxqWhiteListService.operate(id, operate);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "reset")
    public ResVo<String> reset(@RequestParam(name = "authorId") Integer authorId) {
        zsxqWhiteListService.reset(authorId);
        return ResVo.ok("ok");
    }

    // 批量审核通过
    @Operation(summary = "批量审核通过")
    @PostMapping(path = "batchOperate")
    public ResVo<String> batchOperate(@RequestBody ZsxqUserBatchOperateReq req) {
        UserAIStatEnum operate = UserAIStatEnum.fromCode(req.getStatus());
        zsxqWhiteListService.batchOperate(req.getIds(), operate);
        return ResVo.ok("ok");
    }

    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody ZsxqUserPostReq req) {
        zsxqWhiteListService.update(req);
        return ResVo.ok("ok");
    }
}
