package com.github.liueyueyi.forum.api.model.vo.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author YiHui
 * @date 2022/8/15
 */
@Data
@Accessors(chain = true)
public class BaseUserInfoDTO {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户角色 admin, normal
     */
    private String role;

    /**
     * 用户图像
     */
    private String photo;
    /**
     * 个人简介
     */
    private String profile;
    /**
     * 职位
     */
    private String position;

    /**
     * 公司
     */
    private String company;

    /**
     * 扩展字段
     */
    private String extend;
}
