package com.github.paicoding.forum.api.model.vo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 基本用户信息
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Accessors(chain = true)
public class ZsxqUserInfoDTO implements Serializable {
    private static final long serialVersionUID = 4802653694786272120L;

    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    // 这个是 userinfo 表中的 username
    @Schema(description = "用户名")
    private String name;

    @Schema(description = "用户头像")
    private String avatar;

    // 这个是 user 表中的 username
    @Schema(description = "用户编号")
    private String userCode;

    // 星球编号
    @Schema(description = "星球编号")
    private String starNumber;

    // 邀请码
    @Schema(description = "邀请码")
    private String inviteCode;

    // 邀请人数
    @Schema(description = "邀请人数")
    private Integer inviteNum;

    // 状态
    @Schema(description = "状态")
    private Integer state;

    // login_type
    @Schema(description = "登录类型")
    private Integer loginType;

    // strategy
    @Schema(description = "AI策略")
    private Integer strategy;
}
