package com.github.paicoding.forum.api.model.vo.feed;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author YiHui
 * @date 2024/3/18
 */
@Data
@ApiModel(value = "Feed动态")
public class FeedSaveReq implements Serializable {

    private static final long serialVersionUID = -3314909111634518759L;

    @ApiModelProperty("动态id,存在时表示更新")
    private Long id;

    @ApiModelProperty("动态内容，长度小于512字符")
    private String content;

    @ApiModelProperty("动态的图片列表，最多9张")
    private List<String> imgs;

    @ApiModelProperty("转发引用的文档")
    private Long refId;

    @ApiModelProperty("转发的链接")
    private String refUrl;

    @ApiModelProperty(value = "信息流类型: 0 普通动态 1 转发/发布文章 2 转发动态 3 转发评论 4 转发专栏 5 外部链接")
    private Integer type;

    @ApiModelProperty(value = "0: 全部可见 1 登录可见 2 粉丝可见 3 自己可见")
    private Integer view;
}
