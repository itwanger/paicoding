package com.github.paicoding.forum.api.model.vo.banner.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.paicoding.forum.api.model.entity.BaseDTO;
import com.github.paicoding.forum.api.model.enums.ConfigTagEnum;
import com.github.paicoding.forum.api.model.util.cdn.CdnImgSerializer;
import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
import lombok.Data;

/**
 * Banner
 *
 * @author louzai
 * @date 2022-09-17
 */
@Data
public class ConfigDTO extends BaseDTO {

    /**
     * 类型
     */
    private Integer type;

    /**
     * 名称
     */
    private String name;

    /**
     * 图片链接
     */
    @JsonSerialize(using = CdnImgSerializer.class)
    private String bannerUrl;

    /**
     * 跳转链接
     */
    private String jumpUrl;

    /**
     * 内容
     */
    private String content;

    /**
     * 排序
     */
    private Integer rank;

    /**
     * 状态：0-未发布，1-已发布
     */
    private Integer status;

    /**
     * json格式扩展信息
     */
    private String extra;

    /**
     * 配置相关的标签：如 火，推荐，精选 等等，英文逗号分隔
     *
     * @see ConfigTagEnum#getCode()
     */
    private String tags;

    public ConfigDTO setBannerUrl(String bannerUrl) {
        this.bannerUrl = CdnUtil.autoTransCdn(bannerUrl);
        return this;
    }
}
