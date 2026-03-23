package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.ai.config.AiConfigAdminReq;
import com.github.paicoding.forum.api.model.vo.ai.config.AiConfigTestReq;
import com.github.paicoding.forum.api.model.vo.ai.config.dto.AiConfigAdminDTO;
import com.github.paicoding.forum.api.model.vo.ai.config.dto.AiConfigTestDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.chatai.config.AiConfigAdminService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * AI 配置管理控制器
 *
 * @author Codex
 * @date 2026/3/23
 */
@RestController
@Permission(role = UserRole.LOGIN)
@Api(value = "AI 配置管理控制器", tags = "AI 配置")
@RequestMapping(path = {"api/admin/ai/config/", "admin/ai/config/"})
public class AiConfigAdminRestController {
    @Resource
    private AiConfigAdminService aiConfigAdminService;

    @GetMapping(path = "detail")
    @Permission(role = UserRole.ADMIN)
    public ResVo<AiConfigAdminDTO> detail() {
        return ResVo.ok(aiConfigAdminService.getConfig());
    }

    @PostMapping(path = "save")
    @Permission(role = UserRole.ADMIN)
    public ResVo<String> save(@RequestBody AiConfigAdminReq req) {
        aiConfigAdminService.save(req);
        return ResVo.ok();
    }

    @PostMapping(path = "test")
    @Permission(role = UserRole.ADMIN)
    public ResVo<AiConfigTestDTO> test(@RequestBody AiConfigTestReq req) {
        return ResVo.ok(aiConfigAdminService.test(req));
    }
}
