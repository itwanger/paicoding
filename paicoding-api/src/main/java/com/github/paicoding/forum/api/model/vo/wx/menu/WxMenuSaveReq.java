package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

/**
 * 微信菜单草稿保存请求
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuSaveReq {
    /**
     * 菜单 JSON
     */
    private String menuJson;

    /**
     * 备注，可选
     */
    private String comment;
}
