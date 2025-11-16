package com.github.paicoding.forum.web.controller.login.pwd;

import com.github.paicoding.forum.api.model.vo.user.register.UserEmailRegisterCodeReq;
import com.github.paicoding.forum.api.model.vo.user.register.UserEmailRegisterReq;
import com.github.paicoding.forum.web.global.vo.ResultVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: pai_coding
 * @description: 使用账号密码的方式注册
 * @author: XuYifei
 * @create: 2024-11-04
 */

@RestController
public class RegisterRestController {

    @PostMapping("/register/code")
    public ResultVo<Boolean> getVerifyCode(UserEmailRegisterCodeReq req) {
        return ResultVo.ok(true);
    }

    @PostMapping("/register")
    public ResultVo<Boolean> registerByEmail(@RequestBody UserEmailRegisterReq req) {
        return ResultVo.ok(true);

    }
}
