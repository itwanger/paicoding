package com.github.paicoding.forum.api.model.vo.banner;

import lombok.Data;

@Data
public class SearchConfigReq {
    /**
    * 类型
    */
    private Integer type;

    /**
    * 名称
    */
    private String name;

    /**
     * 分页
     */
    private Long pageNumber;
    private Long pageSize;

}
