package com.github.paicoding.forum.service.shortlink.repository.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接数据库对象
 *
 * @author betasecond
 * @date 2025-02-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("short_link")
public class ShortLinkDO extends BaseDO {


    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

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


}