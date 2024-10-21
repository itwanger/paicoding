package com.github.paicoding.forum.api.model.vo.article.dto;

import com.github.paicoding.forum.api.model.enums.column.ColumnArticleReadEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 文章推荐
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Accessors(chain = true)
public class SimpleArticleDTO implements Serializable {
    private static final long serialVersionUID = 3646376715620165839L;

    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "专栏ID")
    private Long columnId;

    @Schema(description = "专栏标题")
    private String column;

    @Schema(description = "文章排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private Timestamp createTime;

    /**
     * @see ColumnArticleReadEnum#getRead()
     */
    @Schema(description = "阅读模式")
    private Integer readType;
}
