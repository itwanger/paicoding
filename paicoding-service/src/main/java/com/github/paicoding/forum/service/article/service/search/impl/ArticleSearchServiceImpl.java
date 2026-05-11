package com.github.paicoding.forum.service.article.service.search.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleSearchSnippetDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleSearchDocumentDTO;
import com.github.paicoding.forum.service.article.repository.params.SearchArticleParams;
import com.github.paicoding.forum.service.article.service.search.ArticleSearchResult;
import com.github.paicoding.forum.service.article.service.search.ArticleSearchService;
import com.github.paicoding.forum.service.constant.EsFieldConstant;
import com.github.paicoding.forum.service.constant.EsIndexConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 文章全文搜索服务。ES 只负责召回和排序，文章展示数据仍回源 MySQL。
 */
@Slf4j
@Service
public class ArticleSearchServiceImpl implements ArticleSearchService {

    private static final int KEYWORD_BOOTSTRAP_MAX_SIZE = 50;
    private static final long KEYWORD_BOOTSTRAP_DONE_TTL_SECONDS = 30L * 24 * 60 * 60;
    private static final long KEYWORD_BOOTSTRAP_LOCK_TTL_SECONDS = 5L * 60;

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    @Value("${elasticsearch.open:false}")
    private Boolean openES;

    @Value("${elasticsearch.article-index:paicoding_article_v1}")
    private String articleIndex;

    @Autowired
    private ObjectProvider<RestHighLevelClient> restHighLevelClientProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleDao articleDao;

    private final AtomicBoolean indexChecked = new AtomicBoolean(false);
    private final AtomicBoolean indexCoverageChecked = new AtomicBoolean(false);
    private volatile boolean fullIndexAvailable = false;
    private final Set<String> keywordBootstrapInFlight = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Override
    public boolean enabled() {
        return Boolean.TRUE.equals(openES) && restClient() != null;
    }

    @Override
    public ArticleSearchResult searchHintArticleIds(String keyword, int limit) {
        if (StringUtils.isBlank(keyword)) {
            return emptyResult();
        }
        List<Map<String, Object>> must = new ArrayList<>();
        must.add(articleKeywordQuery(keyword.trim(), EsFieldConstant.ES_FIELD_TITLE, EsFieldConstant.ES_FIELD_SHORT_TITLE));
        List<Map<String, Object>> filters = onlineFilters();
        return search(keyword.trim(), normalizePageSize(limit), false, true,
                buildSearchBody(0, limit, must, filters, EsFieldConstant.ES_FIELD_TITLE, EsFieldConstant.ES_FIELD_SHORT_TITLE));
    }

    @Async
    @Override
    public void syncHintKeyword(String keyword, int limit) {
        syncKeyword(keyword, normalizePageSize(limit), false, true);
    }

    @Override
    public ArticleSearchResult searchOnlineArticleIds(String keyword, PageParam pageParam) {
        if (StringUtils.isBlank(keyword)) {
            return emptyResult();
        }
        long offset = pageParam == null ? 0 : pageParam.getOffset();
        int size = normalizePageSize(pageParam == null ? PageParam.DEFAULT_PAGE_SIZE : pageParam.getPageSize());
        List<Map<String, Object>> must = new ArrayList<>();
        must.add(articleKeywordQuery(keyword.trim(),
                EsFieldConstant.ES_FIELD_TITLE,
                EsFieldConstant.ES_FIELD_SHORT_TITLE,
                EsFieldConstant.ES_FIELD_SUMMARY,
                EsFieldConstant.ES_FIELD_CONTENT));
        return search(keyword.trim(), keywordBootstrapSize(offset, size), true, true,
                buildSearchBody(offset, size, must, onlineFilters(),
                        EsFieldConstant.ES_FIELD_TITLE, EsFieldConstant.ES_FIELD_SHORT_TITLE,
                        EsFieldConstant.ES_FIELD_SUMMARY, EsFieldConstant.ES_FIELD_CONTENT));
    }

    @Async
    @Override
    public void syncOnlineKeyword(String keyword, PageParam pageParam) {
        if (StringUtils.isBlank(keyword)) {
            return;
        }
        long offset = pageParam == null ? 0 : pageParam.getOffset();
        int size = normalizePageSize(pageParam == null ? PageParam.DEFAULT_PAGE_SIZE : pageParam.getPageSize());
        syncKeyword(keyword.trim(), keywordBootstrapSize(offset, size), true, true);
    }

    @Override
    public ArticleSearchResult searchAdminArticleIds(SearchArticleParams params) {
        if (params == null || StringUtils.isBlank(params.getKeyword())) {
            return emptyResult();
        }
        if (hasAdminStructuredFilters(params)) {
            return null;
        }

        long pageNum = params.getPageNum() <= 0 ? PageParam.DEFAULT_PAGE_NUM : params.getPageNum();
        long pageSize = params.getPageSize() <= 0 ? PageParam.DEFAULT_PAGE_SIZE : params.getPageSize();
        long offset = (pageNum - 1) * pageSize;

        List<Map<String, Object>> must = new ArrayList<>();
        must.add(articleKeywordQuery(params.getKeyword().trim(),
                EsFieldConstant.ES_FIELD_TITLE,
                EsFieldConstant.ES_FIELD_SHORT_TITLE,
                EsFieldConstant.ES_FIELD_SUMMARY,
                EsFieldConstant.ES_FIELD_CONTENT));
        if (StringUtils.isNotBlank(params.getTitle())) {
            must.add(multiMatch(params.getTitle().trim(), EsFieldConstant.ES_FIELD_TITLE + "^4", EsFieldConstant.ES_FIELD_SHORT_TITLE + "^3"));
        }
        if (StringUtils.isNotBlank(params.getUserName())) {
            must.add(matchPhrase("authorName", params.getUserName().trim()));
        }

        List<Map<String, Object>> filters = new ArrayList<>();
        filters.add(term("deleted", YesOrNoEnum.NO.getCode()));
        filters.add(term("status", PushStatusEnum.ONLINE.getCode()));
        addTermFilter(filters, "articleId", params.getArticleId());
        addTermFilter(filters, "authorId", params.getUserId());
        addEffectiveTermFilter(filters, "officalStat", params.getOfficalStat());
        addEffectiveTermFilter(filters, "toppingStat", params.getToppingStat());
        addEffectiveTermFilter(filters, "columnIds", params.getColumnId());
        if (StringUtils.isNotBlank(params.getUrlSlug())) {
            filters.add(term("urlSlug", params.getUrlSlug().trim()));
        }

        int size = normalizePageSize(pageSize);
        return search(params.getKeyword().trim(), keywordBootstrapSize(offset, size), true, true,
                buildSearchBody(offset, size, must, filters,
                        EsFieldConstant.ES_FIELD_TITLE, EsFieldConstant.ES_FIELD_SHORT_TITLE,
                        EsFieldConstant.ES_FIELD_SUMMARY, EsFieldConstant.ES_FIELD_CONTENT));
    }

    @Async
    @Override
    public void syncAdminKeyword(SearchArticleParams params) {
        if (params == null || StringUtils.isBlank(params.getKeyword())) {
            return;
        }
        long pageNum = params.getPageNum() <= 0 ? PageParam.DEFAULT_PAGE_NUM : params.getPageNum();
        long pageSize = params.getPageSize() <= 0 ? PageParam.DEFAULT_PAGE_SIZE : params.getPageSize();
        long offset = (pageNum - 1) * pageSize;
        syncKeyword(params.getKeyword().trim(), keywordBootstrapSize(offset, normalizePageSize(pageSize)), true, true);
    }

    @Override
    public void rebuildArticleIndex() {
        if (!enabled()) {
            return;
        }
        try {
            deleteIndexIfExists();
            createIndex();
            indexChecked.set(true);
            indexCoverageChecked.set(true);
            fullIndexAvailable = false;
            keywordBootstrapInFlight.clear();
            resetIndexGeneration();

            refreshIndex();
            log.info("reset article search index: index={}", indexName());
        } catch (Exception e) {
            indexChecked.set(false);
            indexCoverageChecked.set(false);
            fullIndexAvailable = false;
            log.error("failed to rebuild article search index: {}", indexName(), e);
        }
    }

    @Override
    public void syncArticle(Long articleId) {
        if (!enabled() || articleId == null) {
            return;
        }
        try {
            if (!indexExists() || !articleDocumentExists(articleId)) {
                return;
            }
            ArticleSearchDocumentDTO document = articleDao.queryArticleSearchDocument(articleId);
            if (document == null
                    || Objects.equals(YesOrNoEnum.YES.getCode(), document.getDeleted())
                    || !Objects.equals(PushStatusEnum.ONLINE.getCode(), document.getStatus())) {
                deleteArticle(articleId);
                return;
            }
            indexOne(document);
            refreshIndex();
        } catch (Exception e) {
            log.warn("failed to sync article search index, articleId={}", articleId, e);
        }
    }

    @Override
    public void deleteArticle(Long articleId) {
        if (!enabled() || articleId == null) {
            return;
        }
        try {
            Request request = new Request("DELETE", "/" + indexName() + "/_doc/" + articleId);
            restClient().performRequest(request);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND) {
                log.warn("failed to delete article search document, articleId={}", articleId, e);
            }
        } catch (Exception e) {
            log.warn("failed to delete article search document, articleId={}", articleId, e);
        }
    }

    private ArticleSearchResult search(String keyword, int bootstrapSize, boolean includeBody, boolean onlineOnly, Map<String, Object> body) {
        if (!enabled()) {
            return null;
        }
        try {
            if (!ensureIndex()) {
                return null;
            }
            if (isKeywordBootstrapChecked(keyword, bootstrapSize, includeBody, onlineOnly)) {
                return executeSearch(keyword, body);
            }
            return null;
        } catch (Exception e) {
            log.warn("failed to search article index: {}", indexName(), e);
            return null;
        }
    }

    private ArticleSearchResult executeSearch(String keyword, Map<String, Object> body) throws IOException {
        Request request = new Request("GET", "/" + indexName() + "/_search");
        request.setJsonEntity(objectMapper.writeValueAsString(body));
        Response response = restClient().performRequest(request);
        return parseSearchResponse(keyword, EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
    }

    private void syncKeyword(String keyword, int size, boolean includeBody, boolean onlineOnly) {
        try {
            if (!enabled() || !ensureIndex()) {
                return;
            }
            bootstrapKeywordDocumentsOnce(keyword, size, includeBody, onlineOnly);
        } catch (Exception e) {
            log.warn("failed to bootstrap article search index by keyword: index={}, keyword={}", indexName(), keyword, e);
        }
    }

    private ArticleSearchResult parseSearchResponse(String keyword, String responseBody) throws IOException {
        Map<String, Object> root = objectMapper.readValue(responseBody, MAP_TYPE);
        Map<String, Object> hitsNode = castMap(root.get("hits"));
        List<Map<String, Object>> hits = castList(hitsNode.get("hits"));

        ArticleSearchResult result = new ArticleSearchResult();
        List<Long> articleIds = new ArrayList<>();
        Map<Long, String> highlights = new HashMap<>();
        Map<Long, List<ArticleSearchSnippetDTO>> snippets = new HashMap<>();
        for (Map<String, Object> hit : hits) {
            Map<String, Object> source = castMap(hit.get("_source"));
            Long articleId = asLong(source.get("articleId"));
            if (articleId == null) {
                articleId = asLong(hit.get("_id"));
            }
            if (articleId == null) {
                continue;
            }
            articleIds.add(articleId);
            List<ArticleSearchSnippetDTO> hitSnippets = highlightSnippets(castMap(hit.get("highlight")));
            ArticleSearchSnippetDTO exactSnippet = exactKeywordSnippet(keyword, source);
            if (exactSnippet != null) {
                hitSnippets.removeIf(snippet -> !containsKeyword(snippet.getFragment(), keyword));
                hitSnippets.add(0, exactSnippet);
            }
            if (hitSnippets.isEmpty() && exactSnippet != null) {
                hitSnippets.add(exactSnippet);
            }
            if (!hitSnippets.isEmpty()) {
                snippets.put(articleId, hitSnippets);
                highlights.put(articleId, hitSnippets.get(0).getFragment());
            }
        }
        result.setArticleIds(articleIds);
        result.setHighlights(highlights);
        result.setSnippets(snippets);
        result.setTotal(parseTotal(hitsNode.get("total")));
        return result;
    }

    private ArticleSearchSnippetDTO exactKeywordSnippet(String keyword, Map<String, Object> source) {
        if (StringUtils.isBlank(keyword) || source == null || source.isEmpty()) {
            return null;
        }
        String[] fields = {EsFieldConstant.ES_FIELD_TITLE, EsFieldConstant.ES_FIELD_SHORT_TITLE,
                EsFieldConstant.ES_FIELD_SUMMARY, EsFieldConstant.ES_FIELD_CONTENT};
        for (String field : fields) {
            Object value = source.get(field);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).replaceAll("\\s+", " ").trim();
            int index = text.indexOf(keyword);
            if (index < 0) {
                continue;
            }
            int start = Math.max(0, index - 24);
            int end = Math.min(text.length(), index + keyword.length() + 72);
            return buildSnippet(field, (start > 0 ? "..." : "") + text.substring(start, end) + (end < text.length() ? "..." : ""));
        }
        return null;
    }

    private boolean containsKeyword(String text, String keyword) {
        return StringUtils.isNotBlank(text) && StringUtils.isNotBlank(keyword) && text.contains(keyword.trim());
    }

    private long parseTotal(Object total) {
        if (total instanceof Number) {
            return ((Number) total).longValue();
        }
        if (total instanceof Map) {
            Object value = ((Map<?, ?>) total).get("value");
            return value instanceof Number ? ((Number) value).longValue() : 0L;
        }
        return 0L;
    }

    private List<ArticleSearchSnippetDTO> highlightSnippets(Map<String, Object> highlight) {
        List<ArticleSearchSnippetDTO> snippets = new ArrayList<>();
        if (highlight == null || highlight.isEmpty()) {
            return snippets;
        }
        String[] fields = {EsFieldConstant.ES_FIELD_TITLE, EsFieldConstant.ES_FIELD_SHORT_TITLE, EsFieldConstant.ES_FIELD_SUMMARY, EsFieldConstant.ES_FIELD_CONTENT};
        for (String field : fields) {
            Object values = highlight.get(field);
            if (!(values instanceof List)) {
                continue;
            }
            for (Object value : (List<?>) values) {
                if (value == null) {
                    continue;
                }
                String fragment = String.valueOf(value).replaceAll("\\s+", " ").trim();
                if (StringUtils.isNotBlank(fragment)) {
                    snippets.add(buildSnippet(field, fragment));
                }
            }
        }
        return snippets;
    }

    private ArticleSearchSnippetDTO buildSnippet(String field, String fragment) {
        ArticleSearchSnippetDTO snippet = new ArticleSearchSnippetDTO();
        snippet.setField(field);
        snippet.setFieldName(fieldName(field));
        snippet.setFragment(fragment);
        return snippet;
    }

    private String fieldName(String field) {
        if (EsFieldConstant.ES_FIELD_TITLE.equals(field)) {
            return "标题";
        }
        if (EsFieldConstant.ES_FIELD_SHORT_TITLE.equals(field)) {
            return "教程名";
        }
        if (EsFieldConstant.ES_FIELD_SUMMARY.equals(field)) {
            return "摘要";
        }
        if (EsFieldConstant.ES_FIELD_CONTENT.equals(field)) {
            return "正文";
        }
        return "命中";
    }

    private Map<String, Object> buildSearchBody(long offset, int size, List<Map<String, Object>> must,
                                                List<Map<String, Object>> filters, String... highlightFields) {
        Map<String, Object> bool = new LinkedHashMap<>();
        if (must != null && !must.isEmpty()) {
            bool.put("must", must);
        }
        if (filters != null && !filters.isEmpty()) {
            bool.put("filter", filters);
        }

        Map<String, Object> query = new LinkedHashMap<>();
        query.put("bool", bool);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", Math.max(0, offset));
        body.put("size", size);
        body.put("query", query);
        List<String> sourceFields = new ArrayList<>();
        Collections.addAll(sourceFields, "articleId", EsFieldConstant.ES_FIELD_TITLE, EsFieldConstant.ES_FIELD_SHORT_TITLE,
                EsFieldConstant.ES_FIELD_SUMMARY, EsFieldConstant.ES_FIELD_CONTENT);
        body.put("_source", sourceFields);
        body.put("sort", recencySort());
        body.put("highlight", highlight(highlightFields));
        return body;
    }

    private List<Map<String, Object>> recencySort() {
        List<Map<String, Object>> sort = new ArrayList<>();
        sort.add(scoreSort());
        sort.add(sortField("updateTime", "date"));
        return sort;
    }

    private Map<String, Object> scoreSort() {
        return Collections.singletonMap("_score", Collections.singletonMap("order", "desc"));
    }

    private Map<String, Object> sortField(String field, String unmappedType) {
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("order", "desc");
        order.put("unmapped_type", unmappedType);
        return Collections.singletonMap(field, order);
    }

    private Map<String, Object> highlight(String... fields) {
        Map<String, Object> highlight = new LinkedHashMap<>();
        highlight.put("pre_tags", Collections.singletonList(""));
        highlight.put("post_tags", Collections.singletonList(""));
        highlight.put("fragment_size", 160);
        highlight.put("number_of_fragments", 3);

        Map<String, Object> fieldMap = new LinkedHashMap<>();
        for (String field : fields) {
            fieldMap.put(field, Collections.emptyMap());
        }
        highlight.put("fields", fieldMap);
        return highlight;
    }

    private List<Map<String, Object>> onlineFilters() {
        List<Map<String, Object>> filters = new ArrayList<>();
        filters.add(term("deleted", YesOrNoEnum.NO.getCode()));
        filters.add(term("status", PushStatusEnum.ONLINE.getCode()));
        return filters;
    }

    private Map<String, Object> multiMatch(String keyword, String... fields) {
        Map<String, Object> multiMatch = new LinkedHashMap<>();
        multiMatch.put("query", keyword);
        List<String> fieldList = new ArrayList<>();
        Collections.addAll(fieldList, fields);
        multiMatch.put("fields", fieldList);
        multiMatch.put("type", "best_fields");
        multiMatch.put("operator", "and");
        return Collections.singletonMap("multi_match", multiMatch);
    }

    private Map<String, Object> articleKeywordQuery(String keyword, String... fields) {
        List<Map<String, Object>> should = new ArrayList<>();
        for (String field : fields) {
            should.add(wildcardContains(exactField(field), keyword, fieldBoost(field)));
        }
        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("should", should);
        bool.put("minimum_should_match", 1);
        return Collections.singletonMap("bool", bool);
    }

    private String exactField(String field) {
        if (EsFieldConstant.ES_FIELD_TITLE.equals(field)) {
            return EsFieldConstant.ES_FIELD_TITLE_EXACT;
        }
        if (EsFieldConstant.ES_FIELD_SHORT_TITLE.equals(field)) {
            return EsFieldConstant.ES_FIELD_SHORT_TITLE_EXACT;
        }
        if (EsFieldConstant.ES_FIELD_SUMMARY.equals(field)) {
            return EsFieldConstant.ES_FIELD_SUMMARY_EXACT;
        }
        if (EsFieldConstant.ES_FIELD_CONTENT.equals(field)) {
            return EsFieldConstant.ES_FIELD_CONTENT_EXACT;
        }
        return field;
    }

    private float fieldBoost(String field) {
        if (EsFieldConstant.ES_FIELD_TITLE.equals(field)) {
            return 5.0f;
        }
        if (EsFieldConstant.ES_FIELD_SHORT_TITLE.equals(field)) {
            return 4.0f;
        }
        if (EsFieldConstant.ES_FIELD_SUMMARY.equals(field)) {
            return 2.0f;
        }
        return 1.0f;
    }

    private Map<String, Object> matchPhrase(String field, String value) {
        return Collections.singletonMap("match_phrase", Collections.singletonMap(field, value));
    }

    private Map<String, Object> matchPhrase(String field, String value, float boost) {
        Map<String, Object> phrase = new LinkedHashMap<>();
        phrase.put("query", value);
        phrase.put("slop", 0);
        if (boost > 1.0f) {
            phrase.put("boost", boost);
        }
        return Collections.singletonMap("match_phrase", Collections.singletonMap(field, phrase));
    }

    private Map<String, Object> wildcardContains(String field, String value, float boost) {
        Map<String, Object> wildcard = new LinkedHashMap<>();
        wildcard.put("value", "*" + escapeWildcard(value) + "*");
        wildcard.put("case_insensitive", true);
        if (boost > 1.0f) {
            wildcard.put("boost", boost);
        }
        return Collections.singletonMap("wildcard", Collections.singletonMap(field, wildcard));
    }

    private String escapeWildcard(String value) {
        return value.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
    }

    private Map<String, Object> term(String field, Object value) {
        return Collections.singletonMap("term", Collections.singletonMap(field, value));
    }

    private void addTermFilter(List<Map<String, Object>> filters, String field, Object value) {
        if (value != null) {
            filters.add(term(field, value));
        }
    }

    private void addEffectiveTermFilter(List<Map<String, Object>> filters, String field, Number value) {
        if (value != null && value.longValue() != -1L) {
            filters.add(term(field, value));
        }
    }

    private boolean hasAdminStructuredFilters(SearchArticleParams params) {
        return params.getArticleId() != null
                || params.getUserId() != null
                || StringUtils.isNotBlank(params.getUserName())
                || StringUtils.isNotBlank(params.getTitle())
                || StringUtils.isNotBlank(params.getUrlSlug())
                || (params.getOfficalStat() != null && params.getOfficalStat() != -1)
                || (params.getToppingStat() != null && params.getToppingStat() != -1)
                || (params.getColumnId() != null && params.getColumnId() != -1);
    }

    private int normalizePageSize(long pageSize) {
        if (pageSize <= 0) {
            return PageParam.DEFAULT_PAGE_SIZE.intValue();
        }
        return (int) Math.min(pageSize, 100);
    }

    private int keywordBootstrapSize(long offset, int pageSize) {
        long targetSize = Math.max(pageSize, offset + pageSize);
        return (int) Math.min(targetSize, KEYWORD_BOOTSTRAP_MAX_SIZE);
    }

    private ArticleSearchResult emptyResult() {
        ArticleSearchResult result = new ArticleSearchResult();
        result.setArticleIds(Collections.emptyList());
        result.setHighlights(Collections.emptyMap());
        result.setSnippets(Collections.emptyMap());
        result.setTotal(0L);
        return result;
    }

    private void indexOne(ArticleSearchDocumentDTO document) throws IOException {
        Request request = new Request("PUT", "/" + indexName() + "/_doc/" + document.getArticleId());
        request.setJsonEntity(objectMapper.writeValueAsString(toIndexDocument(document)));
        restClient().performRequest(request);
    }

    private void bulkIndex(List<ArticleSearchDocumentDTO> documents) throws IOException {
        StringBuilder body = new StringBuilder();
        for (ArticleSearchDocumentDTO document : documents) {
            body.append("{\"index\":{\"_index\":\"").append(indexName()).append("\",\"_id\":\"")
                    .append(document.getArticleId()).append("\"}}\n");
            body.append(objectMapper.writeValueAsString(toIndexDocument(document))).append("\n");
        }
        Request request = new Request("POST", "/_bulk");
        request.setEntity(new StringEntity(body.toString(), ContentType.create("application/x-ndjson", Consts.UTF_8)));
        Response response = restClient().performRequest(request);
        Map<String, Object> responseBody = objectMapper.readValue(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8), MAP_TYPE);
        if (Boolean.TRUE.equals(responseBody.get("errors"))) {
            throw new IOException("bulk index article search documents failed: " + firstBulkError(responseBody));
        }
    }

    private Map<String, Object> toIndexDocument(ArticleSearchDocumentDTO document) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("articleId", document.getArticleId());
        body.put("authorId", document.getAuthorId());
        body.put("authorName", document.getAuthorName());
        body.put("title", document.getTitle());
        body.put(EsFieldConstant.ES_FIELD_TITLE_EXACT, document.getTitle());
        body.put("shortTitle", document.getShortTitle());
        body.put(EsFieldConstant.ES_FIELD_SHORT_TITLE_EXACT, document.getShortTitle());
        body.put("urlSlug", document.getUrlSlug());
        body.put("summary", document.getSummary());
        body.put(EsFieldConstant.ES_FIELD_SUMMARY_EXACT, document.getSummary());
        body.put("content", document.getContent());
        body.put(EsFieldConstant.ES_FIELD_CONTENT_EXACT, document.getContent());
        body.put("status", document.getStatus());
        body.put("officalStat", document.getOfficalStat());
        body.put("toppingStat", document.getToppingStat());
        body.put("deleted", document.getDeleted());
        body.put("columnIds", document.parseColumnIds());
        body.put("updateTime", document.getUpdateTime());
        return body;
    }

    private boolean ensureIndex() {
        if (indexChecked.get()) {
            return true;
        }
        synchronized (indexChecked) {
            if (indexChecked.get()) {
                return true;
            }
            try {
                if (!indexExists()) {
                    createIndex();
                    markIndexCoverage(false);
                    resetIndexGeneration();
                } else if (ensureExactFieldsMapping()) {
                    markIndexCoverage(false);
                    resetIndexGeneration();
                    keywordBootstrapInFlight.clear();
                }
                indexChecked.set(true);
                return true;
            } catch (Exception e) {
                log.error("failed to ensure article search index: {}", indexName(), e);
                return false;
            }
        }
    }

    private void markIndexCoverage(boolean fullIndex) {
        fullIndexAvailable = fullIndex;
        indexCoverageChecked.set(true);
    }

    private boolean indexExists() throws IOException {
        try {
            Response response = restClient().performRequest(new Request("HEAD", "/" + indexName()));
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES;
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    private void deleteIndexIfExists() throws IOException {
        try {
            restClient().performRequest(new Request("DELETE", "/" + indexName()));
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND) {
                throw e;
            }
        }
    }

    private boolean ensureExactFieldsMapping() throws IOException {
        if (hasExactFieldsMapping()) {
            return false;
        }
        Request request = new Request("PUT", "/" + indexName() + "/_mapping");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("properties", exactFieldsMapping());
        request.setJsonEntity(objectMapper.writeValueAsString(body));
        restClient().performRequest(request);
        log.info("added exact article search fields to index mapping: index={}", indexName());
        return true;
    }

    private boolean hasExactFieldsMapping() throws IOException {
        Request request = new Request("GET", "/" + indexName() + "/_mapping");
        Response response = restClient().performRequest(request);
        Map<String, Object> root = objectMapper.readValue(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8), MAP_TYPE);
        Map<String, Object> index = castMap(root.get(indexName()));
        Map<String, Object> mappings = castMap(index.get("mappings"));
        Map<String, Object> properties = castMap(mappings.get("properties"));
        return properties.containsKey(EsFieldConstant.ES_FIELD_TITLE_EXACT)
                && properties.containsKey(EsFieldConstant.ES_FIELD_SHORT_TITLE_EXACT)
                && properties.containsKey(EsFieldConstant.ES_FIELD_SUMMARY_EXACT)
                && properties.containsKey(EsFieldConstant.ES_FIELD_CONTENT_EXACT);
    }

    private boolean bootstrapKeywordDocumentsOnce(String keyword, int size, boolean includeBody, boolean onlineOnly) throws IOException {
        String trimmedKeyword = StringUtils.trimToEmpty(keyword);
        if (StringUtils.isBlank(trimmedKeyword)) {
            return false;
        }

        int limit = normalizeBootstrapSize(size);
        String doneKey = keywordBootstrapDoneKey(trimmedKeyword, limit, includeBody, onlineOnly);
        if (StringUtils.isNotBlank(RedisClient.getStr(doneKey))) {
            return false;
        }
        if (!keywordBootstrapInFlight.add(doneKey)) {
            return false;
        }
        String lockKey = keywordBootstrapLockKey(doneKey);
        if (!Boolean.TRUE.equals(RedisClient.setStrIfAbsentWithExpire(lockKey, "1", KEYWORD_BOOTSTRAP_LOCK_TTL_SECONDS))) {
            keywordBootstrapInFlight.remove(doneKey);
            return false;
        }

        try {
            List<ArticleSearchDocumentDTO> documents = articleDao.listArticleSearchDocumentsByKeyword(trimmedKeyword, includeBody, onlineOnly, limit);
            if (documents == null || documents.isEmpty()) {
                log.info("no article search documents found for keyword bootstrap: index={}, keyword={}", indexName(), trimmedKeyword);
                return false;
            }

            bulkIndex(documents);
            refreshIndex();
            RedisClient.setStrWithExpire(doneKey, String.valueOf(documents.size()), KEYWORD_BOOTSTRAP_DONE_TTL_SECONDS);
            log.info("bootstrapped article search index by keyword: index={}, keyword={}, indexed={}",
                    indexName(), trimmedKeyword, documents.size());
            return true;
        } finally {
            keywordBootstrapInFlight.remove(doneKey);
            RedisClient.del(lockKey);
        }
    }

    private boolean isKeywordBootstrapChecked(String keyword, int size, boolean includeBody, boolean onlineOnly) {
        String trimmedKeyword = StringUtils.trimToEmpty(keyword);
        return StringUtils.isNotBlank(trimmedKeyword)
                && StringUtils.isNotBlank(RedisClient.getStr(keywordBootstrapDoneKey(trimmedKeyword, normalizeBootstrapSize(size), includeBody, onlineOnly)));
    }

    private String keywordBootstrapKey(String keyword, int size, boolean includeBody, boolean onlineOnly) {
        return (onlineOnly ? "online:" : "all:") + (includeBody ? "body:" : "title:") + size + ":" + keyword.toLowerCase(Locale.ROOT);
    }

    private String keywordBootstrapDoneKey(String keyword, int size, boolean includeBody, boolean onlineOnly) {
        return "article_search:keyword_bootstrap:done:" + indexName() + ":" + indexGeneration() + ":"
                + keywordBootstrapKey(keyword, size, includeBody, onlineOnly);
    }

    private String keywordBootstrapLockKey(String doneKey) {
        return doneKey.replace(":done:", ":lock:");
    }

    private String indexGenerationKey() {
        return "article_search:index_generation:" + indexName();
    }

    private String indexGeneration() {
        String key = indexGenerationKey();
        String generation = RedisClient.getStr(key);
        if (StringUtils.isNotBlank(generation)) {
            return generation;
        }
        generation = String.valueOf(System.currentTimeMillis());
        RedisClient.setStr(key, generation);
        return generation;
    }

    private void resetIndexGeneration() {
        RedisClient.setStr(indexGenerationKey(), String.valueOf(System.currentTimeMillis()));
    }

    private int normalizeBootstrapSize(int size) {
        return Math.min(Math.max(size, 1), KEYWORD_BOOTSTRAP_MAX_SIZE);
    }

    private boolean isFullIndexAvailable() throws IOException {
        if (indexCoverageChecked.get()) {
            return fullIndexAvailable;
        }
        synchronized (indexCoverageChecked) {
            if (indexCoverageChecked.get()) {
                return fullIndexAvailable;
            }
            long indexedCount = countIndexDocuments();
            Long dbCount = articleDao.countArticle();
            fullIndexAvailable = dbCount != null && dbCount > 0 && indexedCount >= dbCount;
            indexCoverageChecked.set(true);
            return fullIndexAvailable;
        }
    }

    private long countIndexDocuments() throws IOException {
        Request request = new Request("GET", "/" + indexName() + "/_count");
        Response response = restClient().performRequest(request);
        Map<String, Object> body = objectMapper.readValue(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8), MAP_TYPE);
        Long count = asLong(body.get("count"));
        return count == null ? 0L : count;
    }

    private boolean articleDocumentExists(Long articleId) throws IOException {
        try {
            Response response = restClient().performRequest(new Request("HEAD", "/" + indexName() + "/_doc/" + articleId));
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES;
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    private void refreshIndex() throws IOException {
        restClient().performRequest(new Request("POST", "/" + indexName() + "/_refresh"));
    }

    private void createIndex() throws IOException {
        Request request = new Request("PUT", "/" + indexName());
        request.setJsonEntity(objectMapper.writeValueAsString(indexDefinition()));
        restClient().performRequest(request);
    }

    private Map<String, Object> indexDefinition() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("number_of_shards", 1);
        settings.put("number_of_replicas", 0);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("settings", settings);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("articleId", type("long"));
        properties.put("authorId", type("long"));
        properties.put("authorName", textField());
        properties.put(EsFieldConstant.ES_FIELD_TITLE, textField());
        properties.put(EsFieldConstant.ES_FIELD_SHORT_TITLE, textField());
        properties.put("urlSlug", keywordField());
        properties.put(EsFieldConstant.ES_FIELD_SUMMARY, textField());
        properties.put(EsFieldConstant.ES_FIELD_CONTENT, textField());
        properties.putAll(exactFieldsMapping());
        properties.put("status", type("integer"));
        properties.put("deleted", type("integer"));
        properties.put("officalStat", type("integer"));
        properties.put("toppingStat", type("integer"));
        properties.put("columnIds", type("long"));
        properties.put("updateTime", dateField());

        Map<String, Object> mappings = new LinkedHashMap<>();
        mappings.put("properties", properties);
        body.put("mappings", mappings);
        return body;
    }

    private Map<String, Object> exactFieldsMapping() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(EsFieldConstant.ES_FIELD_TITLE_EXACT, wildcardField());
        properties.put(EsFieldConstant.ES_FIELD_SHORT_TITLE_EXACT, wildcardField());
        properties.put(EsFieldConstant.ES_FIELD_SUMMARY_EXACT, wildcardField());
        properties.put(EsFieldConstant.ES_FIELD_CONTENT_EXACT, wildcardField());
        return properties;
    }

    private Map<String, Object> textField() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", "text");
        field.put("analyzer", "ik_max_word");
        field.put("search_analyzer", "ik_smart");
        Map<String, Object> keyword = new LinkedHashMap<>();
        keyword.put("type", "keyword");
        keyword.put("ignore_above", 256);
        field.put("fields", Collections.singletonMap("keyword", keyword));
        return field;
    }

    private Map<String, Object> dateField() {
        Map<String, Object> field = type("date");
        field.put("format", "yyyy-MM-dd HH:mm:ss||strict_date_optional_time||epoch_millis");
        return field;
    }

    private Map<String, Object> keywordField() {
        Map<String, Object> field = type("keyword");
        field.put("ignore_above", 256);
        return field;
    }

    private Map<String, Object> wildcardField() {
        return type("wildcard");
    }

    private Map<String, Object> type(String type) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", type);
        return field;
    }

    private String firstBulkError(Map<String, Object> responseBody) {
        List<Map<String, Object>> items = castList(responseBody.get("items"));
        for (Map<String, Object> item : items) {
            Map<String, Object> index = castMap(item.get("index"));
            Map<String, Object> error = castMap(index.get("error"));
            if (!error.isEmpty()) {
                return error.toString();
            }
        }
        return "unknown bulk error";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object value) {
        return value instanceof List ? (List<Map<String, Object>>) value : Collections.emptyList();
    }

    private Long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String && StringUtils.isNumeric((String) value)) {
            return Long.valueOf((String) value);
        }
        return null;
    }

    private String indexName() {
        return StringUtils.defaultIfBlank(articleIndex, EsIndexConstant.ES_INDEX_ARTICLE);
    }

    private RestClient restClient() {
        RestHighLevelClient client = restHighLevelClientProvider.getIfAvailable();
        return client == null ? null : client.getLowLevelClient();
    }
}
