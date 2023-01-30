package com.github.paicoding.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author YiHui
 * @date 2022/9/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("column_article")
public class ColumnArticleDO extends BaseDO {
    private static final long serialVersionUID = -2372103913090667453L;

    private Long columnId;

    private Long articleId;

    /**
     * 顺序，越小越靠前
     */
    private Integer section;
}
