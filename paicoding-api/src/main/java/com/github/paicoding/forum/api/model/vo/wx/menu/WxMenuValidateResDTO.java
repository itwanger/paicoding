package com.github.paicoding.forum.api.model.vo.wx.menu;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 微信菜单校验结果
 *
 * @author Codex
 * @date 2026/3/23
 */
@Data
public class WxMenuValidateResDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean valid;
    private String normalizedMenuJson;
    private List<String> errors;
}
