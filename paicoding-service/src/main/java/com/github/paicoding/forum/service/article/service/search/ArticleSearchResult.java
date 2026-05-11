package com.github.paicoding.forum.service.article.service.search;

import com.github.paicoding.forum.api.model.vo.article.dto.ArticleSearchSnippetDTO;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ES 搜索结果，只承载命中的文章 ID 和高亮片段，业务详情仍回源 MySQL。
 */
@Data
public class ArticleSearchResult {

    private List<Long> articleIds = Collections.emptyList();

    private Map<Long, String> highlights = Collections.emptyMap();

    private Map<Long, List<ArticleSearchSnippetDTO>> snippets = Collections.emptyMap();

    private long total;
}
