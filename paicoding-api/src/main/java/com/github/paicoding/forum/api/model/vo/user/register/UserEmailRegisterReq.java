package com.github.paicoding.forum.api.model.vo.user.register;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @program: pai_coding
 * @description: 使用邮箱进行注册
 * @author: XuYifei
 * @create: 2024-11-04
 */

@Data
public class UserEmailRegisterReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 2139742660700910123L;

    private String userAccount;

    private String email;

    private String password;

}
