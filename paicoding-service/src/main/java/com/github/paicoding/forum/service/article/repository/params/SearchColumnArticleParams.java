package com.github.paicoding.forum.service.article.repository.params;

import com.github.paicoding.forum.api.model.vo.PageParam;
import lombok.Data;

/**
 * 专栏查询
 */
@Data
public class SearchColumnArticleParams extends PageParam {

    /**
     * 专栏名称
     */
    private String column;

    /**
     * 专栏id
     */
    private Long columnId;

    /**
     * 文章标题
     */
    private String articleTitle;
}
