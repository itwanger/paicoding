package com.github.paicoding.forum.api.model.vo.article;

import lombok.Data;

@Data
public class SearchTagReq {
    // 标签名称
    private String tag;
    // 分页
    private Long pageNumber;
    private Long pageSize;
}
