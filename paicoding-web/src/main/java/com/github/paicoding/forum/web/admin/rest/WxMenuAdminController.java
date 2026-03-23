package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuDetailDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPublishReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPublishResDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuSaveReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuValidateReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuValidateResDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.config.service.WxMenuService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 微信菜单后台控制器
 *
 * @author Codex
 * @date 2026/3/23
 */
@RestController
@Permission(role = UserRole.LOGIN)
@Api(value = "微信菜单后台控制器", tags = "微信菜单")
@RequestMapping(path = {"api/admin/wx/menu/", "admin/wx/menu/"})
public class WxMenuAdminController {
    @Resource
    private WxMenuService wxMenuService;

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "detail")
    public ResVo<WxMenuDetailDTO> detail() {
        return ResVo.ok(wxMenuService.getDetail());
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody WxMenuSaveReq req) {
        wxMenuService.saveDraft(req);
        return ResVo.ok();
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "validate")
    public ResVo<WxMenuValidateResDTO> validate(@RequestBody(required = false) WxMenuValidateReq req) {
        return ResVo.ok(wxMenuService.validate(req));
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "publish")
    public ResVo<WxMenuPublishResDTO> publish(@RequestBody(required = false) WxMenuPublishReq req) {
        return ResVo.ok(wxMenuService.publish(req));
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "sync")
    public ResVo<WxMenuDetailDTO> sync() {
        return ResVo.ok(wxMenuService.syncRemoteToDraft());
    }
}
