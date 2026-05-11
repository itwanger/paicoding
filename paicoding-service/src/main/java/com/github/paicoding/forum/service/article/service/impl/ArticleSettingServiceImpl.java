package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.enums.ArticleEventEnum;
import com.github.paicoding.forum.api.model.enums.OperateArticleEnum;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.event.ArticleMsgEvent;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.AiSlugGenerateReq;
import com.github.paicoding.forum.api.model.vo.article.ArticlePostReq;
import com.github.paicoding.forum.api.model.vo.article.SearchArticleReq;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleAdminDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleSearchSnippetDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.conveter.ArticleStructMapper;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ColumnArticleDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleSearchDocumentDTO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.repository.params.SearchArticleParams;
import com.github.paicoding.forum.service.article.service.ArticleSettingService;
import com.github.paicoding.forum.service.article.service.ColumnSettingService;
import com.github.paicoding.forum.service.article.service.SlugGeneratorService;
import com.github.paicoding.forum.service.article.service.search.ArticleSearchResult;
import com.github.paicoding.forum.service.article.service.search.ArticleSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 文章后台
 *
 * @author louzai
 * @date 2022-09-19
 */
@Service
public class ArticleSettingServiceImpl implements ArticleSettingService {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ColumnArticleDao columnArticleDao;

    @Autowired
    private ColumnSettingService columnSettingService;

    @Autowired
    private SlugGeneratorService slugGeneratorService;

    @Autowired
    private ArticleSearchService articleSearchService;

    @Override
    @CacheEvict(key = "'sideBar_' + #req.articleId", cacheManager = "caffeineCacheManager", cacheNames = "article")
    public void updateArticle(ArticlePostReq req) {
        if (req.getStatus() != PushStatusEnum.OFFLINE.getCode()
                && req.getStatus() != PushStatusEnum.ONLINE.getCode()
                && req.getStatus() != PushStatusEnum.REVIEW.getCode()) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "发布状态不合法!");
        }
        ArticleDO article = articleDao.getById(req.getArticleId());
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "文章不存在!");
        }

        if (StringUtils.isNotBlank(req.getTitle())) {
            article.setTitle(req.getTitle());
        }
        if (StringUtils.isNotBlank(req.getShortTitle())) {
            article.setShortTitle(req.getShortTitle());
        }
        if (req.getUrlSlug() != null) {
            article.setUrlSlug(StringUtils.trim(req.getUrlSlug()));
        }

        ArticleEventEnum operateEvent = null;
        if (req.getStatus() != null) {
            article.setStatus(req.getStatus());
            if (req.getStatus() == PushStatusEnum.OFFLINE.getCode()) {
                operateEvent = ArticleEventEnum.OFFLINE;
            } else if (req.getStatus() == PushStatusEnum.REVIEW.getCode()) {
                operateEvent = ArticleEventEnum.REVIEW;
            } else if (req.getStatus() == PushStatusEnum.ONLINE.getCode()) {
                operateEvent = ArticleEventEnum.ONLINE;
            }
        }
        articleDao.updateById(article);

        if (operateEvent != null) {
            // 发布文章待审核、上线、下线事件
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, operateEvent, article));
        }
    }

    @Override
    public String generateUrlSlug(AiSlugGenerateReq req) {
        String titleForSlug = StringUtils.defaultIfBlank(req.getShortTitle(), req.getTitle());
        if (StringUtils.isBlank(titleForSlug)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "标题不能为空!");
        }
        return slugGeneratorService.generateSlugWithAI(titleForSlug);
    }

    @Override
    public PageVo<ArticleAdminDTO> getArticleList(SearchArticleReq req) {
        // 转换参数，从前端获取的参数转换为数据库查询参数
        SearchArticleParams searchArticleParams = ArticleStructMapper.INSTANCE.toSearchParams(req);
        boolean pureKeywordSearch = false;
        if (StringUtils.isNotBlank(searchArticleParams.getKeyword())) {
            searchArticleParams.setStatus(PushStatusEnum.ONLINE.getCode());
            pureKeywordSearch = isPureKeywordSearch(searchArticleParams);
            if (pureKeywordSearch) {
                PageVo<ArticleAdminDTO> searchResult = queryArticleListBySearchIndex(searchArticleParams);
                if (searchResult != null) {
                    return searchResult;
                }
                PageVo<ArticleAdminDTO> fallbackResult = queryArticleListByKeywordFallback(searchArticleParams);
                articleSearchService.syncAdminKeyword(searchArticleParams);
                return fallbackResult;
            }
            if (StringUtils.isBlank(searchArticleParams.getTitle())) {
                searchArticleParams.setTitle(searchArticleParams.getKeyword().trim());
            }
        }

        // 查询文章列表，分页
        List<ArticleAdminDTO> articleDTOS = articleDao.listArticlesByParams(searchArticleParams);

        // 查询文章总数
        Long totalCount = articleDao.countArticleByParams(searchArticleParams);
        PageVo<ArticleAdminDTO> result = PageVo.build(articleDTOS, req.getPageSize(), req.getPageNumber(), totalCount);
        return result;
    }

    @Override
    public void rebuildArticleSearchIndex() {
        articleSearchService.rebuildArticleIndex();
    }

    private PageVo<ArticleAdminDTO> queryArticleListBySearchIndex(SearchArticleParams searchArticleParams) {
        ArticleSearchResult searchResult = articleSearchService.searchAdminArticleIds(searchArticleParams);
        if (searchResult == null) {
            return null;
        }

        long pageSize = searchArticleParams.getPageSize() <= 0 ? 10 : searchArticleParams.getPageSize();
        long pageNum = searchArticleParams.getPageNum() <= 0 ? 1 : searchArticleParams.getPageNum();
        if (searchResult.getArticleIds().isEmpty()) {
            return PageVo.build(Collections.emptyList(), pageSize, pageNum, searchResult.getTotal());
        }

        List<ArticleAdminDTO> articleDTOS = articleDao.listAdminArticlesByIds(searchResult.getArticleIds());
        Map<Long, ArticleAdminDTO> articleMap = articleDTOS.stream()
                .collect(Collectors.toMap(ArticleAdminDTO::getArticleId, item -> item));
        List<ArticleAdminDTO> orderedList = searchResult.getArticleIds().stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .peek(item -> {
                    item.setSearchHit(searchResult.getHighlights().get(item.getArticleId()));
                    item.setSearchSnippets(searchResult.getSnippets().get(item.getArticleId()));
                })
                .collect(Collectors.toList());
        return PageVo.build(orderedList, pageSize, pageNum, searchResult.getTotal());
    }

    private PageVo<ArticleAdminDTO> queryArticleListByKeywordFallback(SearchArticleParams searchArticleParams) {
        long pageSize = searchArticleParams.getPageSize() <= 0 ? 10 : searchArticleParams.getPageSize();
        long pageNum = searchArticleParams.getPageNum() <= 0 ? 1 : searchArticleParams.getPageNum();
        long offset = (pageNum - 1) * pageSize;
        int limit = keywordBootstrapSize(offset, pageSize);
        List<ArticleSearchDocumentDTO> documents = articleDao.listArticleSearchDocumentsByKeyword(searchArticleParams.getKeyword(), true, true, limit);
        if (CollectionUtils.isEmpty(documents)) {
            return PageVo.build(Collections.emptyList(), pageSize, pageNum, 0);
        }
        List<Long> articleIds = documents.stream()
                .map(ArticleSearchDocumentDTO::getArticleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Long> pageArticleIds = sliceIds(articleIds, offset, pageSize);
        if (CollectionUtils.isEmpty(pageArticleIds)) {
            return PageVo.build(Collections.emptyList(), pageSize, pageNum, articleIds.size());
        }
        Map<Long, ArticleSearchDocumentDTO> documentMap = documents.stream()
                .filter(item -> item.getArticleId() != null)
                .collect(Collectors.toMap(ArticleSearchDocumentDTO::getArticleId, item -> item, (a, b) -> a));
        List<ArticleAdminDTO> articleDTOS = articleDao.listAdminArticlesByIds(pageArticleIds);
        Map<Long, ArticleAdminDTO> articleMap = articleDTOS.stream()
                .collect(Collectors.toMap(ArticleAdminDTO::getArticleId, item -> item));
        List<ArticleAdminDTO> orderedList = pageArticleIds.stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .peek(item -> {
                    ArticleSearchSnippetDTO snippet = buildFallbackSnippet(searchArticleParams.getKeyword(), documentMap.get(item.getArticleId()));
                    if (snippet != null) {
                        item.setSearchHit(snippet.getFragment());
                        item.setSearchSnippets(Collections.singletonList(snippet));
                    }
                })
                .collect(Collectors.toList());
        return PageVo.build(orderedList, pageSize, pageNum, articleIds.size());
    }

    private ArticleSearchSnippetDTO buildFallbackSnippet(String keyword, ArticleSearchDocumentDTO document) {
        if (StringUtils.isBlank(keyword) || document == null) {
            return null;
        }
        ArticleSearchSnippetDTO snippet = firstFallbackSnippet(keyword.trim(), "title", "标题", document.getTitle());
        if (snippet != null) {
            return snippet;
        }
        snippet = firstFallbackSnippet(keyword.trim(), "shortTitle", "教程名", document.getShortTitle());
        if (snippet != null) {
            return snippet;
        }
        snippet = firstFallbackSnippet(keyword.trim(), "summary", "摘要", document.getSummary());
        if (snippet != null) {
            return snippet;
        }
        return firstFallbackSnippet(keyword.trim(), "content", "正文", document.getContent());
    }

    private ArticleSearchSnippetDTO firstFallbackSnippet(String keyword, String field, String fieldName, String text) {
        if (StringUtils.isBlank(keyword) || StringUtils.isBlank(text)) {
            return null;
        }
        String normalizedText = text.replaceAll("\\s+", " ").trim();
        int index = normalizedText.indexOf(keyword);
        if (index < 0) {
            return null;
        }
        int start = Math.max(0, index - 24);
        int end = Math.min(normalizedText.length(), index + keyword.length() + 72);
        ArticleSearchSnippetDTO snippet = new ArticleSearchSnippetDTO();
        snippet.setField(field);
        snippet.setFieldName(fieldName);
        snippet.setFragment((start > 0 ? "..." : "") + normalizedText.substring(start, end) + (end < normalizedText.length() ? "..." : ""));
        return snippet;
    }

    private List<Long> sliceIds(List<Long> articleIds, long offset, long pageSize) {
        if (CollectionUtils.isEmpty(articleIds) || offset >= articleIds.size()) {
            return Collections.emptyList();
        }
        int from = (int) Math.max(0, offset);
        int to = (int) Math.min(articleIds.size(), offset + pageSize);
        return articleIds.subList(from, to);
    }

    private int keywordBootstrapSize(long offset, long pageSize) {
        long normalizedPageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        long targetSize = Math.max(normalizedPageSize, offset + normalizedPageSize);
        return (int) Math.min(targetSize, 50);
    }

    private boolean isPureKeywordSearch(SearchArticleParams params) {
        return params.getArticleId() == null
                && params.getUserId() == null
                && StringUtils.isBlank(params.getUserName())
                && StringUtils.isBlank(params.getTitle())
                && StringUtils.isBlank(params.getUrlSlug())
                && (params.getOfficalStat() == null || params.getOfficalStat() == -1)
                && (params.getToppingStat() == null || params.getToppingStat() == -1)
                && (params.getColumnId() == null || params.getColumnId() == -1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId) {
        ArticleDO dto = articleDao.getById(articleId);
        if (dto != null && dto.getDeleted() != YesOrNoEnum.YES.getCode()) {
            List<ColumnArticleDO> relations = columnArticleDao.listByArticleId(articleId);
            if (!relations.isEmpty()) {
                for (ColumnArticleDO relation : relations) {
                    columnSettingService.deleteColumnArticle(relation.getId());
                }
            }

            dto.setDeleted(YesOrNoEnum.YES.getCode());
            articleDao.updateById(dto);

            // 发布文章删除事件
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.DELETE, dto));
        } else {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
    }

    @Override
    public void operateArticle(Long articleId, OperateArticleEnum operate) {
        ArticleDO articleDO = articleDao.getById(articleId);
        if (articleDO == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
        setArticleStat(articleDO, operate);
        articleDao.updateById(articleDO);
    }

    private void setArticleStat(ArticleDO articleDO, OperateArticleEnum operate) {
        switch (operate) {
            case OFFICAL:
            case CANCEL_OFFICAL:
                compareAndUpdate(articleDO::getOfficalStat, articleDO::setOfficalStat, operate.getDbStatCode());
                return;
            case TOPPING:
            case CANCEL_TOPPING:
                compareAndUpdate(articleDO::getToppingStat, articleDO::setToppingStat, operate.getDbStatCode());
                return;
            case CREAM:
            case CANCEL_CREAM:
                compareAndUpdate(articleDO::getCreamStat, articleDO::setCreamStat, operate.getDbStatCode());
                return;
            default:
        }
    }

    /**
     * 相同则直接返回false不用更新；不同则更新,返回true
     *
     * @param <T>
     * @param supplier
     * @param consumer
     * @param input
     */
    private <T> void compareAndUpdate(Supplier<T> supplier, Consumer<T> consumer, T input) {
        if (Objects.equals(supplier.get(), input)) {
            return;
        }
        consumer.accept(input);
    }
}
