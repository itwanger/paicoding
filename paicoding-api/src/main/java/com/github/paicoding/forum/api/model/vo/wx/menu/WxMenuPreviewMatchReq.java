package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

/**
 * 微信菜单命中预览请求
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuPreviewMatchReq extends WxMenuSaveReq {
    private String eventType;
    private String eventKey;
    private String content;
}
