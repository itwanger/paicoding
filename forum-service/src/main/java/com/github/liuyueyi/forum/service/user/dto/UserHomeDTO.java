package com.github.liuyueyi.forum.service.user.dto;

import lombok.Data;

/**
 * 用户主页信息
 *
 * @author louzai
 * @since 2022/7/19
 */
@Data
public class UserHomeDTO {
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
     * 关注数
     */
    private Integer followCount;

    /**
     * 粉丝数
     */
    private Integer fansCount;

    /**
     * 已发布文章数
     */
    private Integer  articleCount;

    /**
     * 文章点赞数
     */
    private Integer  praiseCount;

    /**
     * 文章被阅读数
     */
    private Integer  readCount;

    /**
     * 文章被收藏数
     */
    private Integer  collectionCount;
}
