package com.github.paicoding.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专栏文章
 *
 * @author YiHui
 * @date 2022/9/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("column_article_group")
public class ColumnArticleGroupDO extends BaseDO {
    private static final long serialVersionUID = -2372103913090667453L;

    /**
     * 专栏id
     */
    private Long columnId;

    /**
     * 父分组id，如果为0或者null，表示当前分组为顶层
     */
    private Long parentGroupId;

    /**
     * 分组名
     */
    private String title;

    /**
     * 顺序，越小越靠前
     */
    private Long section;

    /**
     * 是否删除
     */
    private Integer deleted;
}
