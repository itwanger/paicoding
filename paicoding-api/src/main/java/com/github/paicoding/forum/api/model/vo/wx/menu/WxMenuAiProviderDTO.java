package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信菜单可选 AI provider
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuAiProviderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String value;
    private String name;
    private Boolean syncSupport;
}
