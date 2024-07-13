package com.github.paicoding.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章详情
 *
 * DO 对应数据库实体类
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_detail")
public class ArticleDetailDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 文章内容
     */
    private String content;

    private Integer deleted;
}
