package com.github.paicoding.forum.api.model.vo.wx.mini;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 微信小程序文章详情。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class WxMiniArticleDetailDTO extends WxMiniArticleDTO {
    private static final long serialVersionUID = 5716595495220548972L;

    /**
     * Markdown 转换后的 HTML，前端使用 rich-text 渲染。
     */
    private String contentHtml;
    private Boolean praised;
    private Boolean collected;
    private Boolean commented;
    private Boolean canRead;
    private Integer readType;
    private String sourceType;
    private String sourceUrl;
}
