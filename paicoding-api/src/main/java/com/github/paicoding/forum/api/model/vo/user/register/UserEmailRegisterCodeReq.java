package com.github.paicoding.forum.api.model.vo.user.register;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: pai_coding
 * @description: 邮箱注册申请获得验证码
 * @author: XuYifei
 * @create: 2024-11-12
 */

@Data
public class UserEmailRegisterCodeReq implements Serializable {

        private static final long serialVersionUID = 2139742660720910123L;

        private String email;

}
