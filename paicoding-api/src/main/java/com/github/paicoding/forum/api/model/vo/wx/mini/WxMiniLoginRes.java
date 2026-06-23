package com.github.paicoding.forum.api.model.vo.wx.mini;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 微信小程序登录返回。
 */
@Data
@Accessors(chain = true)
public class WxMiniLoginRes implements Serializable {
    private static final long serialVersionUID = -1015857548785083078L;

    private String token;
    private String tokenHeader;
    private WxMiniUserDTO user;
    private Boolean needProfile;
}
