package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 设置教程说明页请求参数。
 *
 * @author Codex
 */
@Data
public class ColumnReadmeReq implements Serializable {
    private static final long serialVersionUID = -3046396589082608274L;

    @ApiModelProperty("教程ID")
    private Long columnId;

    @ApiModelProperty("说明页文章ID，0表示清空")
    private Long articleId;
}
