package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 教程说明页内容保存请求。
 *
 * @author Codex
 */
@Data
public class ColumnReadmeContentReq implements Serializable {
    private static final long serialVersionUID = 7533904617738972881L;

    @ApiModelProperty("教程ID")
    private Long columnId;

    @ApiModelProperty("说明页 Markdown 内容")
    private String content;
}
