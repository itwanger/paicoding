package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 11/25/23
 */
@Data
@ApiModel("教程排序，根据 ID 和新填的排序")
public class SortColumnArticleByIDReq implements Serializable {
    // 要排序的 id
    @ApiModelProperty("要排序的 id")
    private Long id;
    // 新的排序
    @ApiModelProperty("新的排序")
    private Integer sort;
}
