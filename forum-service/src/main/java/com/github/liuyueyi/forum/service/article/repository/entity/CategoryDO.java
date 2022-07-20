package com.github.liuyueyi.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liuyueyi.forum.service.common.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 类目管理表
 *
 * @author louzai
 * @date 2022-07-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class CategoryDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 类目名称
     */
    private String categoryName;

    /**
     * 状态：0-未发布，1-已发布
     */
    private Integer status;

    private Integer deleted;
}
