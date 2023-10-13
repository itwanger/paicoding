package com.github.paicoding.forum.web.front.login.wx.vo;

import lombok.Data;

/**
 * @author YiHui
 * @date 2022/9/5
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
