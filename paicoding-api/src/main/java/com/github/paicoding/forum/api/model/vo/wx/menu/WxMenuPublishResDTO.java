package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信菜单发布结果
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuPublishResDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean success;
    private Integer errCode;
    private String errMsg;
    private String publishedMenuJson;
}
