package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 微信菜单整套配置
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 微信官方菜单 JSON
     */
    private String menuJson;

    /**
     * 备注
     */
    private String comment;

    /**
     * 关注后回复
     */
    private WxMenuReplyDTO subscribeReply;

    /**
     * 默认兜底回复
     */
    private WxMenuReplyDTO defaultReply;

    /**
     * 关键字/事件回复规则
     */
    private List<WxMenuKeywordReplyDTO> keywordReplies;

    /**
     * none / fixed_reply / ai_reply
     */
    private String messageFallbackStrategy;

    private String aiPrompt;
    private String aiProvider;
    private Boolean aiEnable;

    /**
     * 兼容旧结构：click 菜单 key -> 回复内容
     */
    private List<WxMenuClickReplyDTO> clickReplies;
}
