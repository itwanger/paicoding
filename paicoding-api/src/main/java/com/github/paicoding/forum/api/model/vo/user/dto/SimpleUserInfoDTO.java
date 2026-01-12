package com.github.paicoding.forum.api.model.vo.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.paicoding.forum.api.model.util.cdn.CdnImgSerializer;
import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
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
public class SimpleUserInfoDTO implements Serializable {
    private static final long serialVersionUID = 4802653694786272120L;

    @ApiModelProperty("作者ID")
    private Long userId;

    @ApiModelProperty("作者名")
    private String name;

    @ApiModelProperty("作者头像")
    @JsonSerialize(using = CdnImgSerializer.class)
    private String avatar;

    @ApiModelProperty("作者简介")
    private String profile;

    public SimpleUserInfoDTO setAvatar(String avatar) {
        this.avatar = CdnUtil.autoTransCdn(avatar);
        return this;
    }
}
