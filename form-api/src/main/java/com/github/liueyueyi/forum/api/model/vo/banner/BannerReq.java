package com.github.liueyueyi.forum.api.model.vo.banner;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存Banner请求参数
 *
 * @author LouZai
 * @date 2022/9/17
 */
@Data
public class BannerReq implements Serializable {

    /**
     * ID
     */
    private Long bannerId;

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
}
