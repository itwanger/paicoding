package com.github.paicoding.forum.api.model.vo.login;


import lombok.Data;

/**
 * 保存用户名+密码登录的请求
 *
 * @author XuYifei
 * @create 2024-06-21
 */

@Data
public class UserNamePasswordReq {

    private String username;
    private String password;
}
