package com.github.paicoding.forum.api.model.vo.wx.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 微信自定义菜单按钮
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WxMenuButtonDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private String name;
    private String key;
    private String url;
    private String appid;
    private String pagepath;

    @JsonProperty("media_id")
    private String mediaId;

    @JsonProperty("article_id")
    private String articleId;

    @JsonProperty("sub_button")
    private List<WxMenuButtonDTO> subButton;
}
