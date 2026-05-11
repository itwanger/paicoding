package com.github.paicoding.forum.service.article.service.search;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.service.article.repository.params.SearchArticleParams;

public interface ArticleSearchService {

    boolean enabled();

    ArticleSearchResult searchHintArticleIds(String keyword, int limit);

    void syncHintKeyword(String keyword, int limit);

    ArticleSearchResult searchOnlineArticleIds(String keyword, PageParam pageParam);

    void syncOnlineKeyword(String keyword, PageParam pageParam);

    ArticleSearchResult searchAdminArticleIds(SearchArticleParams params);

    void syncAdminKeyword(SearchArticleParams params);

    void rebuildArticleIndex();

    void syncArticle(Long articleId);

    void deleteArticle(Long articleId);
}
