package com.github.liuyueyi.forum.service.article.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章信息
 *
 * @author YiHui
 * @date 2022/7/24
 */
@Data
public class ArticleDTO implements Serializable {
    private static final long serialVersionUID = -793906904770296838L;

    private Long articleId;

    /**
     * 作者uid
     */
    private Long author;

    private String title;

    private String shortTitle;
    /**
     * 简介
     */
    private String summary;

    /**
     * 封面
     */
    private String cover;

    /**
     * 正文
     */
    private String content;

    /**
     * 文章来源
     *
     * @see com.github.liuyueyi.forum.service.common.enums.SourceTypeEnum
     */
    private String sourceType;

    /**
     * 原文地址
     */
    private String sourceUrl;

    /**
     * 0 未发布 1 已发布
     */
    private Integer status;

    /**
     * 最后更新时间
     */
    private Long lastUpdateTime;

    /**
     * 分类
     */
    private CategoryDTO category;

    /**
     * 标签
     */
    private List<TagDTO> tags;

}
