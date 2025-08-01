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
@ApiModel("拖拽移动教程顺序")
public class MoveColumnArticleOrGroupReq implements Serializable {
    // 要排序的 id
    @ApiModelProperty("专栏ID")
    private Long columnId;

    @ApiModelProperty("移动的分组")
    private Long moveGroupId;

    @ApiModelProperty("移动的教程")
    private Long moveArticleId;

    /**
     * 当这个不存在时，groupId必须存在，表示移动当目标分组的首个位置
     */
    @ApiModelProperty("目标教程")
    private Long targetArticleId;

    /**
     * 目标分组
     */
    @ApiModelProperty("教程分组")
    private Long targetGroupId;

    /**
     * 1 表示在目标的后面一个
     * 0 表示移动到目标里面
     * -1 表示移动到目标前面一个
     */
    @ApiModelProperty("移动位置")
    private Integer movePosition;
}
