package com.github.paicoding.forum.api.model.vo.article;

import com.github.paicoding.forum.api.model.enums.column.ColumnArticleReadEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 保存Column文章请求参数
 *
 * @author LouZai
 * @date 2022/9/26
 */
@Data
public class ColumnArticleReq implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 专栏ID
     */
    private Long columnId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 文章排序
     */
    private Integer sort;

    /**
     * 教程标题
     */
    private String shortTitle;

    /**
     * 阅读方式
     */
    private Integer read;
}
