package com.github.liuyueyi.forum.web.front.backstage.view;

import com.github.liuyueyi.forum.service.user.service.UserSettingService;
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
@RequestMapping(path = "backstage/user/")
public class UserSettingViewController {

    @Autowired
    private UserSettingService userSettingService;
}
