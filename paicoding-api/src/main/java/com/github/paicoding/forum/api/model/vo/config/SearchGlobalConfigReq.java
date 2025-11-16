package com.github.paicoding.forum.api.model.vo.config;

import lombok.Data;

@Data
public class SearchGlobalConfigReq {
    // 配置项名称
    private String keywords;
    // 配置项值
    private String value;
    // 备注
    private String comment;
    // 分页
    private Long pageNumber;
    private Long pageSize;
}
