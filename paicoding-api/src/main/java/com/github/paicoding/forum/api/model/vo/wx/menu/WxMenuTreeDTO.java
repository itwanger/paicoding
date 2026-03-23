package com.github.paicoding.forum.api.model.vo.wx.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 微信自定义菜单树
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WxMenuTreeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<WxMenuButtonDTO> button;
}
