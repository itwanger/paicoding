package com.github.paicoding.forum.api.model.vo.article.dto;

import com.github.paicoding.forum.api.model.enums.column.ColumnArticleReadEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 文章推荐
 *
 * @author YiHui
 * @date 2022/9/6
 */
@Data
@Accessors(chain = true)
public class SimpleArticleDTO implements Serializable {
    private static final long serialVersionUID = 3646376715620165839L;

    @ApiModelProperty("文章ID")
    private Long id;

    @ApiModelProperty("文章标题")
    private String title;

    @ApiModelProperty("专栏ID")
    private Long columnId;

    @ApiModelProperty("专栏标题")
    private String column;

    @ApiModelProperty("文章排序")
    private Integer sort;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    /**
     * @see ColumnArticleReadEnum#getRead()
     */
    @ApiModelProperty("阅读模式")
    private Integer readType;
}
