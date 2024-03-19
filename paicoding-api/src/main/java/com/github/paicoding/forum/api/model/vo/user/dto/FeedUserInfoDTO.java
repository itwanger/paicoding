package com.github.paicoding.forum.api.model.vo.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 广场信息流的用户信息
 *
 * @author YiHui
 * @date 2024/3/18
 */
@Data
@ToString(callSuper = true)
public class FeedUserInfoDTO extends FollowUserInfoDTO {
    private static final long serialVersionUID = -7988781273433005647L;

    @ApiModelProperty("用户简介")
    private String profile;

    /**
     * 用户最后登录区域
     */
    @ApiModelProperty(value = "用户最后登录的地理位置", example = "湖北·武汉")
    private String region;
}
