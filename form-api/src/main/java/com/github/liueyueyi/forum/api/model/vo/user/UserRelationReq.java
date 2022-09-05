package com.github.liueyueyi.forum.api.model.vo.user;

import lombok.Data;

/**
 * 用户关系入参
 *
 * @author louzai
 * @date 2022-07-24
 */
@Data
public class UserRelationReq {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否关注当前用户
     */
    private Boolean followed;
}
