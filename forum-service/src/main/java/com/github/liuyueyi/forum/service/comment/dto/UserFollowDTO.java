package com.github.liuyueyi.forum.service.comment.dto;

import lombok.Data;

/**
 * 关注用户
 *
 * @author louzai
 * @since 2022/7/19
 */
@Data
public class UserFollowDTO {

    private Long userRelationId;

    /**
     * 关注用户ID
     */
    private Long followUserId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户图像
     */
    private String photo;

    /**
     * 个人简介
     */
    private String profile;
}
