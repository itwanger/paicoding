package com.github.paicoding.forum.api.model.vo.article.cache;

import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import lombok.Data;

/**
 * @program: pai_coding
 * @description: 文章缓存时的对象结构
 * @author: XuYifei
 * @create: 2024-10-24
 */

@Data
public class ArticleCacheDTO {

    /**
     * 文章信息
     */
    private ArticleDTO article;

    /**
     * 所属专栏id
     */
    private Long columnId;

    /**
     * 所属专栏中的文章的id
     */
    private Integer sectionId;

}
