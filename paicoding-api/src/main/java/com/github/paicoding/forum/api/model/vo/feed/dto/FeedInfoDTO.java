package com.github.paicoding.forum.api.model.vo.feed.dto;

import com.github.paicoding.forum.api.model.vo.user.dto.FeedUserInfoDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * feed信息
 *
 * @author YiHui
 * @date 2024/3/18
 */
@Data
public class FeedInfoDTO implements Serializable {
    private static final long serialVersionUID = 4105604768468564421L;

    @ApiModelProperty("feed主键")
    private Long id;

    @ApiModelProperty("动态内容，长度小于512字符")
    private String content;

    @ApiModelProperty("动态内容话题/@扩展解析")
    private Map<String, List<FeedContentExtra>> extra;

    @ApiModelProperty("动态的图片列表，最多9张")
    private List<String> imgs;

    @ApiModelProperty(value = "信息流类型: 0 普通动态 1 转发/发布文章 2 转发动态 3 转发评论 4 转发专栏 5 外部链接")
    private Integer type;

    @ApiModelProperty(value = "0: 全部可见 1 登录可见 2 粉丝可见 3 自己可见")
    private Integer view;

    @ApiModelProperty(value = "true 已点赞 false 未点赞")
    private Boolean praised;

    @ApiModelProperty(value = "发布者相关信息")
    private FeedUserInfoDTO user;

    @ApiModelProperty(value = "统计信息")
    private FeedFootCountDTO count;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "更新时间")
    private Long updateTime;

    @ApiModelProperty(value = "转发的内容")
    private FeedRefDTO refInfo;
}
