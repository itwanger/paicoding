package com.github.paicoding.forum.api.model.vo.user.dto;

import com.github.paicoding.forum.api.model.entity.BaseDTO;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Tag(name = "用户基础实体对象")
@Accessors(chain = true)
public class BaseUserInfoDTO extends BaseDTO {
    /**
     * 用户id
     */
    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名",requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    /**
     * 用户角色 admin, normal
     */
    @Schema(description = "角色", example = "ADMIN|NORMAL")
    private String role;

    /**
     * 用户图像
     */
    @Schema(description = "用户头像")
    private String photo;
    /**
     * 个人简介
     */
    @Schema(description = "用户简介")
    private String profile;
    /**
     * 职位
     */
    @Schema(description = "个人职位")
    private String position;

    /**
     * 公司
     */
    @Schema(description = "公司")
    private String company;

    /**
     * 扩展字段
     */
    @Schema(hidden = true)
    private String extend;

    /**
     * 是否删除
     */
    @Schema(hidden = true, description = "用户是否被删除")
    private Integer deleted;

    /**
     * 用户最后登录区域
     */
    @Schema(description = "用户最后登录的地理位置", example = "湖北·武汉")
    private String region;

    /**
     * 星球状态
     */
    private UserAIStatEnum starStatus;
}
