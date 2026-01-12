package com.github.paicoding.forum.api.model.vo.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.paicoding.forum.api.model.util.cdn.CdnImgSerializer;
import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * 关注者用户信息
 *
 * @author YiHui
 * @date 2022/11/2
 */
@Data
public class FollowUserInfoDTO implements Serializable {
    private static final long serialVersionUID = 7169636386013658631L;
    /**
     * 当前登录的用户与这个用户之间的关联关系id
     */
    private Long relationId;

    /**
     * true 表示当前登录用户关注了这个用户
     * false 标识当前登录用户没有关注这个用户
     */
    private Boolean followed;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    @JsonSerialize(using = CdnImgSerializer.class)
    private String avatar;

    public FollowUserInfoDTO setAvatar(String avatar) {
        this.avatar = CdnUtil.autoTransCdn(avatar);
        return this;
    }
}
