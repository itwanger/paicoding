package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.service.user.service.UserSettingService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private UserSettingService userSettingService;

    @Autowired
    private UserService userService;

    /**
     * 根据用户名，查询用户列表
     *
     * @param name
     * @return
     */
    @GetMapping(path = "query")
    public ResVo<List<SimpleUserInfoDTO>> queryUserList(String name) {
        return ResVo.ok(userService.searchUser(name));
    }
}
