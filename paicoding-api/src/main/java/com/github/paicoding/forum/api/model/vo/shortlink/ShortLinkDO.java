package com.github.paicoding.forum.api.model.vo.shortlink;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
/**
 * 短链接表
 *
 * @author betasecond
 * @date 2025-02-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("short_link")
public class ShortLinkDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String originalUrl;
    private String shortUrl;
    private String username;
    private String thirdPartyUserId;
    private String userAgent;
    private String loginMethod;
    private int deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
