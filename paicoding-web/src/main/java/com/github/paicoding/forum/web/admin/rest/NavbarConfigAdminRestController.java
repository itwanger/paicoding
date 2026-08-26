package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.config.NavbarConfigDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.config.service.NavbarConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Permission(role = UserRole.ADMIN)
@Api(value = "顶部导航配置控制器", tags = "顶部导航配置")
@RequestMapping(path = {"api/admin/navbar/config", "admin/navbar/config"})
public class NavbarConfigAdminRestController {
    @Autowired
    private NavbarConfigService navbarConfigService;

    @GetMapping
    @ApiOperation("获取顶部导航配置")
    public ResVo<NavbarConfigDTO> get() {
        return ResVo.ok(navbarConfigService.getConfig());
    }

    @PostMapping
    @ApiOperation("保存顶部导航配置")
    public ResVo<String> save(@RequestBody NavbarConfigDTO config) {
        navbarConfigService.save(config);
        return ResVo.ok("导航配置已生效");
    }
}
