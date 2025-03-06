package com.github.paicoding.forum.service.shortlink.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接记录数据库对象
 *
 * @author betasecond
 * @date 2025-02-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("short_link_record")
public class ShortLinkRecordDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 短链接代码
     */
    private String shortCode;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 访问时间
     */
    private Long accessTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 登录方式 （如：微信、QQ、微博等）。
     */
    private String loginMethod;

    /**
     * 访问来源（如：网页、移动端等）。
     */
    private String accessSource;
}