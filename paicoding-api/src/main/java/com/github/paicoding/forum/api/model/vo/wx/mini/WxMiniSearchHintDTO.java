package com.github.paicoding.forum.api.model.vo.wx.mini;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 微信小程序搜索建议。
 */
@Data
@Accessors(chain = true)
public class WxMiniSearchHintDTO implements Serializable {
    private static final long serialVersionUID = -4370693347099546323L;

    private Long articleId;
    private String title;
    private String urlSlug;
}
