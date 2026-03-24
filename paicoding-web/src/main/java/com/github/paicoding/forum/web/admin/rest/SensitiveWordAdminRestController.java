package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.config.SensitiveWordConfigReq;
import com.github.paicoding.forum.api.model.vo.config.SearchSensitiveWordHitReq;
import com.github.paicoding.forum.api.model.vo.config.SensitiveWordOperateReq;
import com.github.paicoding.forum.api.model.vo.config.dto.SensitiveWordConfigDTO;
import com.github.paicoding.forum.api.model.vo.config.dto.SensitiveWordHitDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.sensitive.service.SensitiveAdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 敏感词后台
 *
 * @author Codex
 * @date 2026/3/24
 */
@RestController
@Permission(role = UserRole.LOGIN)
@Api(value = "敏感词管理控制器", tags = "敏感词管理")
@RequestMapping(path = {"/api/admin/sensitive/", "/admin/sensitive/"})
public class SensitiveWordAdminRestController {

    @Autowired
    private SensitiveAdminService sensitiveAdminService;

    @Permission(role = UserRole.ADMIN)
    @ApiOperation("获取敏感词配置详情")
    @GetMapping(path = "detail")
    public ResVo<SensitiveWordConfigDTO> detail() {
        return ResVo.ok(sensitiveAdminService.getConfig());
    }

    @Permission(role = UserRole.ADMIN)
    @ApiOperation("分页获取敏感词命中统计")
    @PostMapping(path = "hit/list")
    public ResVo<PageVo<SensitiveWordHitDTO>> hitList(@RequestBody SearchSensitiveWordHitReq req) {
        return ResVo.ok(sensitiveAdminService.getHitWordPage(req));
    }

    @Permission(role = UserRole.ADMIN)
    @ApiOperation("保存敏感词配置")
    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody SensitiveWordConfigReq req) {
        sensitiveAdminService.saveConfig(req);
        return ResVo.ok();
    }

    @Permission(role = UserRole.ADMIN)
    @ApiOperation("清除敏感词命中统计")
    @PostMapping(path = "hit/clear")
    public ResVo<String> clearHitWord(@RequestBody SensitiveWordOperateReq req) {
        sensitiveAdminService.clearHitWord(req.getWord());
        return ResVo.ok();
    }
}
