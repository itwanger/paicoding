package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.banner.ConfigReq;
import com.github.paicoding.forum.api.model.vo.banner.SearchConfigReq;
import com.github.paicoding.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.config.service.impl.ConfigSettingServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Banner后台
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@RestController
@Permission(role = UserRole.LOGIN)
@Tag(name = "后台运营配置管理控制器", description = "配置管理")
@RequestMapping(path = {"api/admin/config/", "admin/config/"})
public class ConfigSettingrRestController {

    @Autowired
    private ConfigSettingServiceImpl configSettingService;

    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody ConfigReq configReq) {
        configSettingService.saveConfig(configReq);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "delete")
    public ResVo<String> delete(@RequestParam(name = "configId") Integer configId) {
        configSettingService.deleteConfig(configId);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "operate")
    public ResVo<String> operate(@RequestParam(name = "configId") Integer configId,
                                 @RequestParam(name = "pushStatus") Integer pushStatus) {
        if (pushStatus != PushStatusEnum.OFFLINE.getCode() && pushStatus!= PushStatusEnum.ONLINE.getCode()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
        }
        configSettingService.operateConfig(configId, pushStatus);
        return ResVo.ok("ok");
    }

    /**
     * 获取配置列表
     *
     * @return
     */
    @PostMapping(path = "list")
    public ResVo<PageVo<ConfigDTO>> list(@RequestBody SearchConfigReq req) {
        PageVo<ConfigDTO> bannerDTOPageVo = configSettingService.getConfigList(req);
        return ResVo.ok(bannerDTOPageVo);
    }
}
