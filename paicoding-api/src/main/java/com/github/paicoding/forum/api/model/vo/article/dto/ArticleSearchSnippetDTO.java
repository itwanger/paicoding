package com.github.paicoding.forum.api.model.vo.article.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 文章全文搜索命中片段
 */
@Data
public class ArticleSearchSnippetDTO implements Serializable {

    private static final long serialVersionUID = -4384904141098765126L;

    /**
     * 命中的字段，如 title、summary、content
     */
    private String field;

    /**
     * 字段展示名，如标题、摘要、正文
     */
    private String fieldName;

    /**
     * 命中关键词附近的片段
     */
    private String fragment;
}
