
package com.github.paicoding.forum.api.model.vo.article;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存Column分组请求参数
 *
 * @author yihui
 * @date 2024/12/17
 */
@Data
public class ColumnArticleGroupReq implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 专栏ID
     */
    private Long columnId;

    /**
     * 父分组id
     */
    private Long parentGroupId;

    /**
     * 文章ID
     */
    private String title;

    /**
     * 专栏排序
     */
    private Long section;
}
