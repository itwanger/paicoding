package com.github.liueyueyi.forum.api.model.vo.comment.dto;

import lombok.Data;

import java.util.List;

/**
 * 关注用户
 *
 * @author louzai
 * @since 2022/7/19
 */
@Data
public class UserFollowListDTO {

    /**
     * 用户列表
     */
    List<UserFollowDTO> userFollowList;

    /**
     * 是否有更多
     */
    private Boolean isMore;
}
