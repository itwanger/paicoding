package com.github.paicoding.forum.web.controller.login.wx.vo;

import lombok.Data;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class WxLoginVo {
    /**
     * 验证码
     */
    private String code;

    /**
     * 二维码
     */
    private String qr;

    /**
     * true 表示需要重新建立连接
     */
    private boolean reconnect;

}
