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
     * 用户关系ID
     */
    private Long userRelationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关注用户ID
     */
    private Long followUserId;

    /**
     * 关注状态: 0-未关注，1-已关注，2-取消关注
     */
    private Integer followState;
}
