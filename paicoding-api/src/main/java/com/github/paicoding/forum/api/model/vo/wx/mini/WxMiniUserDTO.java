package com.github.paicoding.forum.api.model.vo.wx.mini;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 微信小程序端用户信息。
 */
@Data
@Accessors(chain = true)
public class WxMiniUserDTO implements Serializable {
    private static final long serialVersionUID = 1451966140133608428L;

    private Long userId;
    private String nickName;
    private String avatarUrl;
    private String role;
    private String profile;
}
