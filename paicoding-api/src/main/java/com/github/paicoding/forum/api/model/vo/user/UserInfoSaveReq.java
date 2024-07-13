package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

/**
 * 用户信息入参
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class UserInfoSaveReq {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户图像
     */
    private String photo;

    /**
     * 职位
     */
    private String position;

    /**
     * 公司
     */
    private String company;

    /**
     * 个人简介
     */
    private String profile;
}
