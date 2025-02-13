package com.github.paicoding.forum.api.model.vo.shortlink;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 短链接数据库对象
 *
 * @author betasecond
 * @date 2025-02-13
 */
@Data
@TableName("short_link")
public class ShortLinkDO {


    private static final long serialVersionUID = 1L;

    /**
     * 原始URL
     */
    private String originalUrl;

    /**
     * 短链接代码
     */
    private String shortCode;

    /**
     * 删除标记
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}