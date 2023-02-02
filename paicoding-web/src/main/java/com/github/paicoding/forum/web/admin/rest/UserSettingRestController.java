package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.service.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户权限管理后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@Permission(role = UserRole.ADMIN)
@RequestMapping(path = {"api/admin/user/", "admin/user/"})
public class UserSettingRestController {

    @Autowired
    private UserSettingService userSettingService;
}
