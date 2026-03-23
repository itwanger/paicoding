package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

/**
 * 微信菜单 AI 预览请求
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuPreviewAiReq {
    private String content;
    private String aiPrompt;
    private String aiProvider;
    private Boolean aiEnable;
}
