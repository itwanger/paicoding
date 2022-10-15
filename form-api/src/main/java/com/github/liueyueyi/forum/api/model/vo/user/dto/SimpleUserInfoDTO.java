package com.github.liueyueyi.forum.api.model.vo.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 基本用户信息
 *
 * @author YiHui
 * @date 2022/9/26
 */
@Data
public class SimpleUserInfoDTO implements Serializable {
    private static final long serialVersionUID = 4802653694786272120L;

    /**
     * 作者
     */
    private Long userId;

    /**
     * 作者名
     */
    private String name;

    /**
     * 作者头像
     */
    private String avatar;

    /**
     * 作者简介
     */
    private String profile;
}
