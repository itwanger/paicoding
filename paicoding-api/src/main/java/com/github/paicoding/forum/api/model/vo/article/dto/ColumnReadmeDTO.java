package com.github.paicoding.forum.api.model.vo.article.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 教程说明页内容。
 *
 * @author Codex
 */
@Data
public class ColumnReadmeDTO implements Serializable {
    private static final long serialVersionUID = 6437806500645405947L;

    private Long columnId;

    private String title;

    private String content;

    private Boolean created;
}
