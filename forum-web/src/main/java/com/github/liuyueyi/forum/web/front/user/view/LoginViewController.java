package com.github.liuyueyi.forum.web.front.user.view;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liuyueyi.forum.web.global.BaseViewController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * 用户注册、取消，登录、登出
 *
 * @author louzai
 * @date : 2022/8/3 10:56
 **/
@Controller
@Slf4j
public class LoginViewController extends BaseViewController {
    @GetMapping(path = "login")
    public String login() {
        if (ReqInfoContext.getReqInfo().getUserId() != null) {
            // 已经登录时，直接跳转到主页
            return "/";
        }
        return "login";
    }
}
