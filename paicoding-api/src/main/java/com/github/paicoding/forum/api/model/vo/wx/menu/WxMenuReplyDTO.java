package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 微信菜单回复配置
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuReplyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 支持 text/news
     */
    private String replyType;

    /**
     * 文本消息内容
     */
    private String content;

    /**
     * 图文消息列表，最多 8 条
     */
    private List<WxMenuReplyArticleDTO> articles;
}
