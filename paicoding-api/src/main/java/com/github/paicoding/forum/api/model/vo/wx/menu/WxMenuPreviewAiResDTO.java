package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信菜单 AI 预览结果
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuPreviewAiResDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean success;
    private String replyText;
    private String provider;
    private String errorMsg;
}
