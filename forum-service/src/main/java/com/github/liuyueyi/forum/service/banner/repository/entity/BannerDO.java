package com.github.liuyueyi.forum.service.banner.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liueyueyi.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论表
 *
 * @author louzai
 * @date 2022-07-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("banner")
public class BannerDO extends BaseDO {

    private static final long serialVersionUID = 1L;

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

    /**
     * 0未删除 1 已删除
     */
    private Integer deleted;
}
