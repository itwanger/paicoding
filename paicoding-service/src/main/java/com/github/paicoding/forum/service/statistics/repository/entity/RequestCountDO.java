package com.github.paicoding.forum.service.statistics.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import com.github.paicoding.forum.api.model.enums.cache.CacheTypeEnum;
import com.github.paicoding.forum.core.cache.annotation.CacheKey;
import com.github.paicoding.forum.core.cache.annotation.CacheType;
import com.github.paicoding.forum.core.cache.annotation.CacheValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 请求计数表
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("request_count")
@CacheType(value = CacheTypeEnum.CACHE_HASH)
public class RequestCountDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 机器IP
     */
    @CacheKey
    private String host;

    /**
     * 访问计数
     */
    @CacheValue
    private Integer cnt;

    /**
     * 当前日期
     */
    private Date date;
}
