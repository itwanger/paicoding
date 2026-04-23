package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.user.SearchUserLoginAuditReq;
import com.github.paicoding.forum.api.model.vo.user.SearchUserSessionReq;
import com.github.paicoding.forum.api.model.vo.user.dto.UserActiveSessionDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserLoginAuditDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.service.LoginAuditService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.front.search.vo.SearchUserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户权限管理后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@Permission(role = UserRole.ADMIN)
@Api(value = "用户管理控制器", tags = "用户管理")
@RequestMapping(path = {"api/admin/user/", "admin/user/"})
public class UserSettingRestController {

    @Autowired
    private UserService userService;
    @Autowired
    private LoginAuditService loginAuditService;

    @ApiOperation("用户搜索")
    @GetMapping(path = "query")
    public ResVo<SearchUserVo> queryUserList(@RequestParam(name = "key", required = false) String key) {
        List<SimpleUserInfoDTO> list = userService.searchUser(key);
        SearchUserVo vo = new SearchUserVo();
        vo.setKey(key);
        vo.setItems(list);
        return ResVo.ok(vo);
    }

    @Permission(role = UserRole.LOGIN)
    @ApiOperation("获取当前登录用户信息")
    @GetMapping("info")
    public ResVo<BaseUserInfoDTO> info() {
        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        return ResVo.ok(user);
    }

    @ApiOperation("分页获取用户登录审计")
    @PostMapping(path = "login-audit")
    public ResVo<PageVo<UserLoginAuditDTO>> loginAudit(@RequestBody(required = false) SearchUserLoginAuditReq req) {
        return ResVo.ok(loginAuditService.getLoginAuditPage(req));
    }

    @ApiOperation("分页获取用户登录会话")
    @PostMapping(path = "session")
    public ResVo<PageVo<UserActiveSessionDTO>> session(@RequestBody(required = false) SearchUserSessionReq req) {
        return ResVo.ok(loginAuditService.getSessionPage(req));
    }
}
