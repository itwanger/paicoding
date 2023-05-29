package com.github.paicoding.forum.api.model.vo.article;

import lombok.Data;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/29/23
 */
@Data
public class SearchTagReq {
    // 标签名称
    private String tag;
    // 分页
    private Long pageNumber;
    private Long pageSize;
}
