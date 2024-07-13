package com.github.paicoding.forum.api.model.vo.article;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存Category请求参数
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class CategoryReq implements Serializable {

    /**
     * ID
     */
    private Long categoryId;

    /**
     * 类目名称
     */
    private String category;

    /**
     * 排序
     */
    private Integer rank;
}
