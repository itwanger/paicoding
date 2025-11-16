package com.github.paicoding.forum.api.model.vo.config;

import lombok.Data;

@Data
public class GlobalConfigReq {
    // 配置项名称
    private String keywords;
    // 配置项值
    private String value;
    // 备注
    private String comment;
    // id
    private Long id;
}
