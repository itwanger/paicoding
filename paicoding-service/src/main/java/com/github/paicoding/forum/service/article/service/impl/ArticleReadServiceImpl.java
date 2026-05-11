package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.enums.CollectionStatEnum;
import com.github.paicoding.forum.api.model.enums.CommentStatEnum;
import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.HomeSelectEnum;
import com.github.paicoding.forum.api.model.enums.OperateTypeEnum;
import com.github.paicoding.forum.api.model.enums.PraiseStatEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.senstive.SensitiveService;
import com.github.paicoding.forum.core.util.ArticleUtil;
import com.github.paicoding.forum.service.article.conveter.ArticleConverter;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ArticleTagDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleSearchDocumentDTO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.CategoryService;
import com.github.paicoding.forum.service.article.service.search.ArticleSearchResult;
import com.github.paicoding.forum.service.article.service.search.ArticleSearchService;
import com.github.paicoding.forum.service.sensitive.service.SensitiveBypassService;
import com.github.paicoding.forum.service.statistics.service.CountService;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.github.paicoding.forum.service.user.service.UserFootService;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章查询相关服务类
 *
 * @author louzai
 * @date 2022-07-20
 */
@Slf4j
@Service
public class ArticleReadServiceImpl implements ArticleReadService {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ArticleTagDao articleTagDao;

    @Autowired
    private CategoryService categoryService;
    /**
     * 在一个项目中，UserFootService 就是内部服务调用
     * 拆微服务时，这个会作为远程服务访问
     */
    @Autowired
    private UserFootService userFootService;

    @Autowired
    private CountService countService;

    @Autowired
    private UserService userService;

    @Autowired
    private SensitiveService sensitiveService;

    @Autowired
    private SensitiveBypassService sensitiveBypassService;

    @Autowired
    private ArticleSearchService articleSearchService;

    @Override
    public ArticleDO queryBasicArticle(Long articleId) {
        return articleDao.getById(articleId);
    }

    @Override
    public String generateSummary(String content) {
        return ArticleUtil.pickSummary(content);
    }

    @Override
    public PageVo<TagDTO> queryTagsByArticleId(Long articleId) {
        List<TagDTO> tagDTOS = articleTagDao.queryArticleTagDetails(articleId);
        return PageVo.build(tagDTOS, 1, 10, tagDTOS.size());
    }

    /**
     * 查询文章的内容，给ai用于分析
     *
     * @param articleId
     * @return
     */
    @Override
    public String queryArticleContentForAI(Long articleId) {
        return articleDao.findLatestDetail(articleId).getContent();
    }

    @Override
    public ArticleDTO queryDetailArticleInfo(Long articleId) {
        ArticleDTO article = articleDao.queryArticleDetail(articleId);
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
        // 更新分类相关信息
        CategoryDTO category = article.getCategory();
        category.setCategory(categoryService.queryCategoryName(category.getCategoryId()));

        // 更新标签信息
        article.setTags(articleTagDao.queryArticleTagDetails(articleId));
        return article;
    }

    /**
     * 查询文章所有的关联信息，正文，分类，标签，阅读计数，当前登录用户是否点赞、评论过
     *
     * @param articleId
     * @param readUser
     * @return
     */
    @Override
    public ArticleDTO queryFullArticleInfo(Long articleId, Long readUser) {
        ArticleDTO article = queryDetailArticleInfo(articleId);

        // 文章阅读计数+1
        countService.incrArticleReadCount(article.getAuthor(), articleId);

        // 文章的操作标记
        if (readUser != null) {
            // 更新用于足迹，并判断是否点赞、评论、收藏
            UserFootDO foot = userFootService.saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleId,
                    article.getAuthor(), readUser, OperateTypeEnum.READ);
            article.setPraised(Objects.equals(foot.getPraiseStat(), PraiseStatEnum.PRAISE.getCode()));
            article.setCommented(Objects.equals(foot.getCommentStat(), CommentStatEnum.COMMENT.getCode()));
            article.setCollected(Objects.equals(foot.getCollectionStat(), CollectionStatEnum.COLLECTION.getCode()));
        } else {
            // 未登录，全部设置为未处理
            article.setPraised(false);
            article.setCommented(false);
            article.setCollected(false);
        }

        // 更新文章统计计数
        article.setCount(countService.queryArticleStatisticInfo(articleId));

        // 设置文章的点赞列表
        article.setPraisedUsers(userFootService.queryArticlePraisedUsers(articleId));
        return sanitizeArticleForDisplay(article);
    }


    /**
     * 查询文章列表
     *
     * @param categoryId
     * @param page
     * @return
     */
    @Override
    public PageListVo<ArticleDTO> queryArticlesByCategory(Long categoryId, PageParam page) {
        List<ArticleDO> records = articleDao.listArticlesByCategoryId(categoryId, page);
        return buildArticleListVo(records, page.getPageSize());
    }

    /**
     * 查询置顶的文章列表
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<ArticleDTO> queryTopArticlesByCategory(Long categoryId) {
        PageParam page = PageParam.newPageInstance(PageParam.DEFAULT_PAGE_NUM, PageParam.TOP_PAGE_SIZE);
        List<ArticleDO> articleDTOS = articleDao.listArticlesByCategoryId(categoryId, page);
        return articleDTOS.stream().map(this::fillArticleRelatedInfo).collect(Collectors.toList());
    }

    @Override
    public Long queryArticleCountByCategory(Long categoryId) {
        return articleDao.countArticleByCategoryId(categoryId);
    }

    @Override
    public Map<Long, Long> queryArticleCountsByCategory() {
        return articleDao.countArticleByCategoryId();
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesByTag(Long tagId, PageParam page) {
        List<ArticleDO> records = articleDao.listRelatedArticlesOrderByReadCount(null, Arrays.asList(tagId), page);
        return buildArticleListVo(records, page.getPageSize());
    }

    @Override
    public List<SimpleArticleDTO> querySimpleArticleBySearchKey(String key) {
        // todo 当key为空时，返回热门推荐
        if (StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }
        key = key.trim();
        ArticleSearchResult searchResult = articleSearchService.searchHintArticleIds(key, 10);
        if (searchResult != null && !CollectionUtils.isEmpty(searchResult.getArticleIds())) {
            return buildSimpleArticleList(searchResult.getArticleIds());
        }

        List<ArticleDO> records = articleDao.listSimpleArticlesByBySearchKey(key);
        List<SimpleArticleDTO> result = records.stream()
                .map(s -> new SimpleArticleDTO().setId(s.getId()).setAuthorId(s.getUserId()).setTitle(s.getTitle()))
                .map(this::sanitizeSimpleArticleForDisplay)
                .collect(Collectors.toList());
        articleSearchService.syncHintKeyword(key, 10);
        return result;
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesBySearchKey(String key, PageParam page) {
        if (StringUtils.isNotBlank(key)) {
            ArticleSearchResult searchResult = articleSearchService.searchOnlineArticleIds(key.trim(), page);
            if (searchResult != null && !CollectionUtils.isEmpty(searchResult.getArticleIds())) {
                return buildArticleListVo(searchResult.getArticleIds(), searchResult.getHighlights(), page.getPageSize());
            }
        }
        PageListVo<ArticleDTO> result;
        if (StringUtils.isNotBlank(key)) {
            result = buildArticleSearchFallbackVo(key.trim(), page);
        } else {
            List<ArticleDO> records = articleDao.listArticlesByBySearchKey(key, page);
            result = buildArticleListVo(records, page.getPageSize());
        }
        if (StringUtils.isNotBlank(key)) {
            articleSearchService.syncOnlineKeyword(key.trim(), page);
        }
        return result;
    }


    @Override
    public PageListVo<ArticleDTO> queryArticlesByUserAndType(Long userId, PageParam pageParam, HomeSelectEnum select) {
        List<ArticleDO> records = null;
        if (select == HomeSelectEnum.ARTICLE) {
            // 用户的文章列表
            records = articleDao.listArticlesByUserId(userId, pageParam);
        } else if (select == HomeSelectEnum.READ) {
            // 用户的阅读记录
            List<Long> articleIds = userFootService.queryUserReadArticleList(userId, pageParam);
            records = CollectionUtils.isEmpty(articleIds) ? Collections.emptyList() : articleDao.listByIds(articleIds);
            records = sortByIds(articleIds, records);
        } else if (select == HomeSelectEnum.COLLECTION) {
            // 用户的收藏列表
            List<Long> articleIds = userFootService.queryUserCollectionArticleList(userId, pageParam);
            records = CollectionUtils.isEmpty(articleIds) ? Collections.emptyList() : articleDao.listByIds(articleIds);
            records = sortByIds(articleIds, records);
        }

        if (CollectionUtils.isEmpty(records)) {
            return PageListVo.emptyVo();
        }
        return buildArticleListVo(records, pageParam.getPageSize());
    }

    /**
     * fixme @楼仔 这个排序逻辑看着像是有问题的样子
     *
     * @param articleIds
     * @param records
     * @return
     */
    private List<ArticleDO> sortByIds(List<Long> articleIds, List<ArticleDO> records) {
        List<ArticleDO> articleDOS = new ArrayList<>();
        Map<Long, ArticleDO> articleDOMap = records.stream().collect(Collectors.toMap(ArticleDO::getId, t -> t));
        articleIds.forEach(articleId -> {
            if (articleDOMap.containsKey(articleId)) {
                articleDOS.add(articleDOMap.get(articleId));
            }
        });
        return articleDOS;
    }

    private List<SimpleArticleDTO> buildSimpleArticleList(List<Long> articleIds) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return Collections.emptyList();
        }
        List<ArticleDO> records = sortByIds(articleIds, articleDao.listByIds(articleIds));
        return records.stream()
                .map(s -> new SimpleArticleDTO().setId(s.getId()).setAuthorId(s.getUserId()).setTitle(s.getTitle()))
                .map(this::sanitizeSimpleArticleForDisplay)
                .collect(Collectors.toList());
    }

    private PageListVo<ArticleDTO> buildArticleListVo(List<Long> articleIds, Map<Long, String> highlights, long pageSize) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return PageListVo.emptyVo();
        }
        List<ArticleDO> records = sortByIds(articleIds, articleDao.listByIds(articleIds));
        List<ArticleDTO> result = records.stream()
                .map(this::fillArticleRelatedInfo)
                .peek(article -> article.setSearchHit(highlights.get(article.getArticleId())))
                .collect(Collectors.toList());
        return PageListVo.newVo(result, pageSize);
    }

    private PageListVo<ArticleDTO> buildArticleSearchFallbackVo(String key, PageParam page) {
        long offset = page == null ? 0 : page.getOffset();
        long pageSize = page == null ? PageParam.DEFAULT_PAGE_SIZE : page.getPageSize();
        int limit = keywordBootstrapSize(offset, pageSize);
        List<ArticleSearchDocumentDTO> documents = articleDao.listArticleSearchDocumentsByKeyword(key, true, true, limit);
        if (CollectionUtils.isEmpty(documents)) {
            return PageListVo.emptyVo();
        }
        List<Long> articleIds = documents.stream()
                .map(ArticleSearchDocumentDTO::getArticleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Long> pageArticleIds = sliceIds(articleIds, offset, pageSize);
        return buildArticleListVo(pageArticleIds, Collections.emptyMap(), pageSize);
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
        long normalizedPageSize = pageSize <= 0 ? PageParam.DEFAULT_PAGE_SIZE : Math.min(pageSize, 100);
        long targetSize = Math.max(normalizedPageSize, offset + normalizedPageSize);
        return (int) Math.min(targetSize, 50);
    }

    @Override
    public PageListVo<ArticleDTO> buildArticleListVo(List<ArticleDO> records, long pageSize) {
        List<ArticleDTO> result = records.stream().map(this::fillArticleRelatedInfo).collect(Collectors.toList());
        return PageListVo.newVo(result, pageSize);
    }

    /**
     * 补全文章的阅读计数、作者、分类、标签等信息
     *
     * @param record
     * @return
     */
    private ArticleDTO fillArticleRelatedInfo(ArticleDO record) {
        ArticleDTO dto = ArticleConverter.toDto(record);
        // 分类信息
        dto.getCategory().setCategory(categoryService.queryCategoryName(record.getCategoryId()));
        // 标签列表
        dto.setTags(articleTagDao.queryArticleTagDetails(record.getId()));
        // 阅读计数统计
        dto.setCount(countService.queryArticleStatisticInfo(record.getId()));
        // 作者信息
        BaseUserInfoDTO author = userService.queryBasicUserInfo(dto.getAuthor());
        dto.setAuthorName(author.getUserName());
        dto.setAuthorAvatar(author.getPhoto());
        return sanitizeArticleForDisplay(dto);
    }

    @Override
    public PageListVo<SimpleArticleDTO> queryHotArticlesForRecommend(PageParam pageParam) {
        List<SimpleArticleDTO> list = articleDao.listHotArticles(pageParam);
        list.forEach(this::sanitizeSimpleArticleForDisplay);
        return PageListVo.newVo(list, pageParam.getPageSize());
    }

    private ArticleDTO sanitizeArticleForDisplay(ArticleDTO article) {
        if (article == null) {
            return null;
        }
        if (shouldBypassSensitiveFilter(article.getAuthor())) {
            return article;
        }
        article.setTitle(sanitizeText(article.getTitle()));
        article.setShortTitle(sanitizeText(article.getShortTitle()));
        article.setSummary(sanitizeText(article.getSummary()));
        article.setContent(sanitizeText(article.getContent()));
        return article;
    }

    private SimpleArticleDTO sanitizeSimpleArticleForDisplay(SimpleArticleDTO article) {
        if (article == null) {
            return null;
        }
        if (sensitiveBypassService.shouldBypassByUserId(article.getAuthorId())) {
            return article;
        }
        article.setTitle(sanitizeText(article.getTitle()));
        article.setColumn(sanitizeText(article.getColumn()));
        article.setGroupName(sanitizeText(article.getGroupName()));
        return article;
    }

    private String sanitizeText(String text) {
        return text == null ? null : sensitiveService.replace(text);
    }

    private boolean shouldBypassSensitiveFilter(Long authorId) {
        return sensitiveBypassService.shouldBypassByUserId(authorId);
    }

    @Override
    public int queryArticleCount(long authorId) {
        return articleDao.countArticleByUser(authorId);
    }

    @Override
    public Long getArticleCount() {
        return articleDao.countArticle();
    }
}
