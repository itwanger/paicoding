package com.github.paicoding.forum.api.model.vo.wx.mini;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信小程序登录请求。
 */
@Data
public class WxMiniLoginReq implements Serializable {
    private static final long serialVersionUID = 613605874864034083L;

    private String code;
    private String nickName;
    private String avatarUrl;
}
