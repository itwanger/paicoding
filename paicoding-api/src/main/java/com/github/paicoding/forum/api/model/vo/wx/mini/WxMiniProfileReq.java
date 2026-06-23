package com.github.paicoding.forum.api.model.vo.wx.mini;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信小程序用户资料保存请求。
 */
@Data
public class WxMiniProfileReq implements Serializable {
    private static final long serialVersionUID = 1023688770953835428L;

    private String nickName;
    private String avatarUrl;
    private String profile;
}
