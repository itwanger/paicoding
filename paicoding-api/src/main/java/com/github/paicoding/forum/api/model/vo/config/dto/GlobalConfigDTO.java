package com.github.paicoding.forum.api.model.vo.config.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GlobalConfigDTO implements Serializable {
    // uid
    private static final long serialVersionUID = 1L;

    // id
    private Long id;
    // 配置项名称
    private String keywords;
    // 配置项值
    private String value;
    // 备注
    private String comment;
}
