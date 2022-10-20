package com.github.liueyueyi.forum.api.model.vo.banner.dto;

import com.github.liueyueyi.forum.api.model.entity.BaseDTO;
import lombok.Data;

import java.util.Date;

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
}
