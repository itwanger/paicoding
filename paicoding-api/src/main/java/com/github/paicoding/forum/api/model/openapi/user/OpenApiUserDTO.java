package com.github.paicoding.forum.api.model.openapi.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author YiHui
 * @date 2025/9/15
 */
@Data
public class OpenApiUserDTO implements Serializable {
    private static final long serialVersionUID = 4663622879892017339L;
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id", required = true)
    private Long userId;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称", required = true)
    private String userName;

    /**
     * 登录用户名
     */
    @ApiModelProperty(value = "登录用户名", required = true)
    private String loginName;

    /**
     * 用户角色 admin, normal
     */
    @ApiModelProperty(value = "角色", example = "ADMIN|NORMAL")
    private String role;

    /**
     * 用户图像
     */
    @ApiModelProperty(value = "用户头像")
    private String photo;

    /**
     * 用户的邮箱
     */
    @ApiModelProperty(value = "用户邮箱", example = "paicoding@126.com")
    private String email;

    /**
     * 个人简介
     */
    @ApiModelProperty(value = "用户简介")
    private String profile;
    /**
     * 职位
     */
    @ApiModelProperty(value = "个人职位")
    private String position;

    /**
     * 公司
     */
    @ApiModelProperty(value = "公司")
    private String company;


    @ApiModelProperty(value = "微信id")
    private String wxId;

    /**
     * 星球id
     */
    @ApiModelProperty(value = "星球id")
    private String zsxqId;

    /**
     * 星球到期时间(秒)
     */
    @ApiModelProperty(value = "星球到期时间(秒)")
    private Long zsxqExpireTime;
}
