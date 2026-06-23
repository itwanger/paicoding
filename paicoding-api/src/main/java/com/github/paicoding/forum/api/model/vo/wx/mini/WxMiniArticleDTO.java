package com.github.paicoding.forum.api.model.vo.wx.mini;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 微信小程序文章列表项。
 */
@Data
@Accessors(chain = true)
public class WxMiniArticleDTO implements Serializable {
    private static final long serialVersionUID = -6940613045867289153L;

    private Long articleId;
    private String title;
    private String shortTitle;
    private String summary;
    private String cover;
    private String urlSlug;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Long categoryId;
    private String category;
    private List<String> tags;
    private Integer readCount;
    private Integer praiseCount;
    private Integer collectionCount;
    private Integer commentCount;
    private Long createTime;
    private Long lastUpdateTime;
    private String searchHit;
}
