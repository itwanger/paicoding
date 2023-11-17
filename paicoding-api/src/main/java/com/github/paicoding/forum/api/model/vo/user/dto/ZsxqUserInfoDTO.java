package com.github.paicoding.forum.api.model.vo.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 基本用户信息
 *
 * @author YiHui
 * @date 2022/9/26
 */
@Data
@Accessors(chain = true)
public class ZsxqUserInfoDTO implements Serializable {
    private static final long serialVersionUID = 4802653694786272120L;

    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    // 这个是 userinfo 表中的 username
    @ApiModelProperty("用户名")
    private String name;

    @ApiModelProperty("用户头像")
    private String avatar;

    // 这个是 user 表中的 username
    @ApiModelProperty("用户编号")
    private String userCode;

    // 星球编号
    @ApiModelProperty("星球编号")
    private String starNumber;

    // 邀请码
    @ApiModelProperty("邀请码")
    private String inviteCode;

    // 邀请人数
    @ApiModelProperty("邀请人数")
    private Integer inviteNum;

    // 状态
    @ApiModelProperty("状态")
    private Integer state;

    // login_type
    @ApiModelProperty("登录类型")
    private Integer loginType;

    // strategy
    @ApiModelProperty("AI策略")
    private Integer strategy;
}
