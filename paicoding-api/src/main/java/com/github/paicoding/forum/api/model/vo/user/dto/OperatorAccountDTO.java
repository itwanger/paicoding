package com.github.paicoding.forum.api.model.vo.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 运营账号创建结果
 *
 * @author Codex
 * @date 2026/5/30
 */
@Data
@Accessors(chain = true)
@ApiModel("运营账号创建结果")
public class OperatorAccountDTO implements Serializable {
    private static final long serialVersionUID = 4554188530715375990L;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "登录用户名")
    private String username;

    @ApiModelProperty(value = "明文密码，仅创建接口返回一次")
    private String password;

    @ApiModelProperty(value = "展示昵称")
    private String displayName;

    @ApiModelProperty(value = "角色")
    private String role;
}
