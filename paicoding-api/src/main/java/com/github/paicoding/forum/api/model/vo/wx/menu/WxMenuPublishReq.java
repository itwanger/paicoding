package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

/**
 * 微信菜单发布请求
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuPublishReq {
    /**
     * 菜单 JSON；为空时发布当前草稿
     */
    private String menuJson;
}
