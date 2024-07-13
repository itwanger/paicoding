package com.github.paicoding.forum.api.model.vo.article;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存Tag请求参数
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class TagReq implements Serializable {

    /**
     * ID
     */
    private Long tagId;

    /**
     * 标签名称
     */
    private String tag;

    /**
     * 类目ID
     */
    private Long categoryId;
}
