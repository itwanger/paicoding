package com.github.liueyueyi.forum.api.model.vo.article;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 保存Column请求参数
 *
 * @author LouZai
 * @date 2022/9/26
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
    private String columnName;

    /**
     * 作者
     */
    private Long userId;

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
     * 上线时间
     */
    private Date publishTime;
}
