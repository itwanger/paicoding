package com.github.paicoding.forum.api.model.vo.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用标签选择
 *
 * @author LouZai
 * @date 2022/8/28
 */
@Data
public class TagSelectDTO implements Serializable {

    /**
     * 类型
     */
    private String selectType;

    /**
     * 描述
     */
    private String selectDesc;

    /**
     * 是否选中
     */
    private Boolean selected;
}
