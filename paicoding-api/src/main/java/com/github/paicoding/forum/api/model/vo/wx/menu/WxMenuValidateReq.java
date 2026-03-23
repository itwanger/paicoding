package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

/**
 * 微信菜单校验请求
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuValidateReq {
    /**
     * 菜单 JSON；为空时校验当前草稿
     */
    private String menuJson;
}
