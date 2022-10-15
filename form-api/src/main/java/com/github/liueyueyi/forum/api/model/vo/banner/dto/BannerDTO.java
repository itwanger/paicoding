package com.github.liueyueyi.forum.api.model.vo.banner.dto;

import com.github.liueyueyi.forum.api.model.entity.BaseDTO;
import lombok.Data;

/**
 * Banner
 *
 * @author louzai
 * @date 2022-09-17
 */
@Data
public class BannerDTO extends BaseDTO {

    /**
     * 图片名称
     */
    private String bannerName;

    /**
     * 图片url
     */
    private String bannerUrl;

    /**
     * 图片类型
     */
    private Integer bannerType;

    /**
     * 排序
     */
    private Integer rank;

    /**
     * 状态：0-未发布，1-已发布
     */
    private Integer status;
}
