package com.github.paicoding.forum.api.model.enums.article;

import lombok.Getter;

/**
 * @program: pai_coding
 * @description: 文章分类的特殊常量
 * @author: XuYifei
 * @create: 2024-10-21
 */

@Getter
public class ArticleCategoryEnum {

    /**
     * 全部
     */
    public static final String CATEGORY_ALL = "全部";

    private final String categoryName;

    ArticleCategoryEnum(String categoryName) {
        this.categoryName = categoryName;
    }
}
