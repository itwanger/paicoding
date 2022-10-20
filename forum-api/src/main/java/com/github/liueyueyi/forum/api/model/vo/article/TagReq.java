package com.github.liueyueyi.forum.api.model.vo.article;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存Tag请求参数
 *
 * @author LouZai
 * @date 2022/9/17
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
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    private Integer tagType;

    /**
     * 类目ID
     */
    private Long categoryId;
}
