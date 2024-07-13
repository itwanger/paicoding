package com.github.paicoding.forum.service.config.repository.params;

import com.github.paicoding.forum.api.model.vo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchConfigParams extends PageParam {
    // 类型
    private Integer type;
    // 名称
    private String name;
}
