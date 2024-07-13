package com.github.paicoding.forum.api.model.vo.article;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存Column请求参数
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class ColumnReq implements Serializable {

    /**
     * ID
     */
    private Long columnId;

    /**
     * 专栏名
     */
    private String column;

    /**
     * 作者
     */
    private Long author;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 封面
     */
    private String cover;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 排序
     */
    private Integer section;

    /**
     * 专栏预计的文章数
     */
    private Integer nums;

    /**
     * 专栏类型
     */
    private Integer type;

    /**
     * 限时免费开始时间
     */
    private Long freeStartTime;

    /**
     * 限时免费结束时间
     */
    private Long freeEndTime;
}
