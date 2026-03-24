package com.github.paicoding.forum.api.model.vo.config.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 敏感词配置详情
 *
 * @author Codex
 * @date 2026/3/24
 */
@Data
public class SensitiveWordConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否开启敏感词能力
     */
    private Boolean enable;

    /**
     * 敏感词列表
     */
    private List<String> denyWords;

    /**
     * 白名单列表
     */
    private List<String> allowWords;

    /**
     * 命中词条总数
     */
    private Long hitTotal;

    /**
     * 命中统计（兼容保留，详情接口不再返回全量列表）
     */
    private List<SensitiveWordHitDTO> hitWords;
}
