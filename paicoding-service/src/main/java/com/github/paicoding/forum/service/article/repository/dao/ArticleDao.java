package com.github.paicoding.forum.service.article.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.OfficalStatEnum;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleAdminDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.YearArticleDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.article.conveter.ArticleConverter;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDetailDO;
import com.github.paicoding.forum.service.article.repository.entity.ReadCountDO;
import com.github.paicoding.forum.service.article.repository.mapper.ArticleDetailMapper;
import com.github.paicoding.forum.service.article.repository.mapper.ArticleMapper;
import com.github.paicoding.forum.service.article.repository.mapper.ReadCountMapper;
import com.github.paicoding.forum.service.article.repository.params.SearchArticleParams;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章相关DB操作
 * <p>
 * 多表结构的操作封装，只与DB操作相关
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Repository
public class ArticleDao extends ServiceImpl<ArticleMapper, ArticleDO> {
    @Resource
    private ArticleDetailMapper articleDetailMapper;
    @Resource
    private ReadCountMapper readCountMapper;
    @Resource
    private ArticleMapper articleMapper;


    /**
     * 查询文章详情
     *
     * @param articleId
     * @return
     */
    public ArticleDTO queryArticleDetail(Long articleId) {
        // 查询文章记录
        ArticleDO article = baseMapper.selectById(articleId);
        if (article == null || Objects.equals(article.getDeleted(), YesOrNoEnum.YES.getCode())) {
            return null;
        }

        // 查询文章正文
        ArticleDTO dto = ArticleConverter.toDto(article);
        if (showReviewContent(article)) {
            ArticleDetailDO detail = findLatestDetail(articleId);
            dto.setContent(detail.getContent());
        } else {
            // 对于审核中的文章，只有作者本人才能看到原文
            dto.setContent("### 文章审核中，请稍后再看");
        }
        return dto;
    }

    private boolean showReviewContent(ArticleDO article) {
        if (article.getStatus() != PushStatusEnum.REVIEW.getCode()) {
            return true;
        }

        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        if (user == null) {
            return false;
        }

        // 作者本人和admin超管可以看到审核内容
        return user.getUserId().equals(article.getUserId()) || (user.getRole() != null && user.getRole().equalsIgnoreCase(UserRole.ADMIN.name()));
    }


    // ------------ article content  ----------------

    private ArticleDetailDO findLatestDetail(long articleId) {
        // 查询文章内容
        LambdaQueryWrapper<ArticleDetailDO> contentQuery = Wrappers.lambdaQuery();
        contentQuery.eq(ArticleDetailDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDetailDO::getArticleId, articleId)
                .orderByDesc(ArticleDetailDO::getVersion);
        return articleDetailMapper.selectList(contentQuery).get(0);
    }

    /**
     * 保存文章正文
     *
     * @param articleId
     * @param content
     * @return
     */
    public Long saveArticleContent(Long articleId, String content) {
        ArticleDetailDO detail = new ArticleDetailDO();
        detail.setArticleId(articleId);
        detail.setContent(content);
        detail.setVersion(1L);
        articleDetailMapper.insert(detail);
        return detail.getId();
    }

    /**
     * 更正文章正文
     *
     * @param articleId
     * @param content
     * @param update    true 表示更新最后一条记录； false 表示新插入一个新的记录
     */
    public void updateArticleContent(Long articleId, String content, boolean update) {
        if (update) {
            articleDetailMapper.updateContent(articleId, content);
        } else {
            ArticleDetailDO latest = findLatestDetail(articleId);
            latest.setVersion(latest.getVersion() + 1);
            latest.setId(null);
            latest.setContent(content);
            articleDetailMapper.insert(latest);
        }
    }

    // ------------- 文章列表查询 --------------

    public List<ArticleDO> listArticlesByUserId(Long userId, PageParam pageParam) {
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getUserId, userId)
                .last(PageParam.getLimitSql(pageParam))
                .orderByDesc(ArticleDO::getId);
        if (!Objects.equals(ReqInfoContext.getReqInfo().getUserId(), userId)) {
            // 作者本人，可以查看草稿、审核、上线文章；其他用户，只能查看上线的文章
            query.eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode());
        }
        return baseMapper.selectList(query);
    }


    /**
     * 根据用户id分页查询文章列表
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    public IPage<ArticleDO> listArticlesByUserIdPagination(Long userId, int currentPage, int pageSize) {
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getUserId, userId)
                .orderByDesc(ArticleDO::getUpdateTime);
        if (!Objects.equals(ReqInfoContext.getReqInfo().getUserId(), userId)) {
            // 作者本人，可以查看草稿、审核、上线文章；其他用户，只能查看上线的文章
            query.eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode());
        }
        Page<ArticleDO> page = new Page<>(currentPage, pageSize);
        return baseMapper.selectPage(page, query);
    }

    /**
     * 根据用户id分页查询最近的浏览文章的列表
     * @param userId
     * @param page
     * @return
     */
    public IPage<ArticleDO> listHistoryArticlesByUserIdPagination(Page<ArticleDO> page, Long userId) {
        return articleMapper.listHistoryArticlesByUserId(page, userId);
    }

    /**
     * 根据用户id分页查询最近的浏览文章的列表
     * @param userId
     * @param page
     * @return
     */
    public IPage<ArticleDO> listStarArticlesByUserIdPagination(Page<ArticleDO> page, Long userId) {
        return articleMapper.listStarArticlesByUserId(page, userId);
    }




    public List<ArticleDO> listArticlesByCategoryId(Long categoryId, PageParam pageParam) {
        if (categoryId != null && categoryId <= 0) {
            // 分类不存在时，表示查所有
            categoryId = null;
        }
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode());

        // 如果分页中置顶的四条数据，需要加上官方的查询条件
        // 说明是查询官方的文章，非置顶的文章，只限制全部分类
        if (categoryId == null && pageParam.getPageSize() == PageParam.TOP_PAGE_SIZE) {
            query.eq(ArticleDO::getOfficalStat, OfficalStatEnum.OFFICAL.getCode());
        }

        Optional.ofNullable(categoryId).ifPresent(cid -> query.eq(ArticleDO::getCategoryId, cid));
        query.last(PageParam.getLimitSql(pageParam))
                .orderByDesc(ArticleDO::getToppingStat,  ArticleDO::getCreateTime);
        return baseMapper.selectList(query);
    }

    /**
     * 根据分类id查询文章
     * @param currentPage
     * @param pageSize
     * @param categoryId
     * @return
     */
    public IPage<ArticleDO> listArticlesByCategoryIdPagination(int currentPage, int pageSize, Long categoryId) {
        if (categoryId != null && categoryId <= 0) {
            // 分类不存在时，表示查所有
            categoryId = null;
        }

        Page<ArticleDO> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode());
        Optional.ofNullable(categoryId).ifPresent(cid -> query.eq(ArticleDO::getCategoryId, cid));

        // 查询结果排序：先是官方的，然后是置顶的，最后是按照创建时间倒序
        query.orderByDesc(ArticleDO::getOfficalStat)
                .orderByDesc(ArticleDO::getToppingStat, ArticleDO::getCreateTime);

        return baseMapper.selectPage(page, query);
    }

    /**
     * 根据分类id查询文章
     * @param currentPage
     * @param pageSize
     * @param tagId
     * @return
     */
    public IPage<ArticleDO> listArticlesByTagIdPagination(int currentPage, int pageSize, Long tagId) {
        Page<ArticleDO> page = new Page<>(currentPage, pageSize);
        if (tagId == null || tagId <= 0) {
            // tag不存在时，说明查所有
            LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
            query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                    .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode());
            return baseMapper.selectPage(page, query);
        }else{
            return articleMapper.selectArticlesByTag(page, tagId);
        }
    }

    public Long countArticleByCategoryId(Long categoryId) {
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .eq(ArticleDO::getCategoryId, categoryId);
        return baseMapper.selectCount(query);
    }

    /**
     * 按照分类统计文章的数量
     *
     * @return key: categoryId, value: count
     */
    public Map<Long, Long> countArticleByCategoryId() {
        QueryWrapper<ArticleDO> query = Wrappers.query();
        query.select("category_id, count(*) as cnt")
                .eq("deleted", YesOrNoEnum.NO.getCode())
                .eq("status", PushStatusEnum.ONLINE.getCode()).groupBy("category_id");
        List<Map<String, Object>> mapList = baseMapper.selectMaps(query);
        Map<Long, Long> result = Maps.newHashMapWithExpectedSize(mapList.size());
        for (Map<String, Object> mp : mapList) {
            Long cnt = (Long) mp.get("cnt");
            if (cnt != null && cnt > 0) {
                result.put((Long) mp.get("category_id"), cnt);
            }
        }
        return result;
    }

    public List<ArticleDO> listArticlesByBySearchKey(String key, PageParam pageParam) {
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .and(!StringUtils.isEmpty(key),
                        v -> v.like(ArticleDO::getTitle, key)
                                .or()
                                .like(ArticleDO::getShortTitle, key)
                                .or()
                                .like(ArticleDO::getSummary, key));
        query.last(PageParam.getLimitSql(pageParam))
                .orderByDesc(ArticleDO::getId);
        return baseMapper.selectList(query);
    }

    /**
     * 通过关键词，从标题中找出相似的进行推荐，只返回主键 + 标题
     *
     * @param key
     * @return
     */
    public List<ArticleDO> listSimpleArticlesByBySearchKey(String key) {
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .and(!StringUtils.isEmpty(key),
                        v -> v.like(ArticleDO::getTitle, key)
                                .or()
                                .like(ArticleDO::getShortTitle, key)
                );
        query.select(ArticleDO::getId, ArticleDO::getTitle, ArticleDO::getShortTitle)
                .last("limit 10")
                .orderByDesc(ArticleDO::getId);
        return baseMapper.selectList(query);
    }


    /**
     * 阅读计数
     *
     * @param articleId
     * @return
     */
    public int incrReadCount(Long articleId) {
        LambdaQueryWrapper<ReadCountDO> query = Wrappers.lambdaQuery();
        query.eq(ReadCountDO::getDocumentId, articleId).eq(ReadCountDO::getDocumentType, DocumentTypeEnum.ARTICLE.getCode());
        ReadCountDO record = readCountMapper.selectOne(query);
        if (record == null) {
            record = new ReadCountDO().setDocumentId(articleId).setDocumentType(DocumentTypeEnum.ARTICLE.getCode()).setCnt(1);
            readCountMapper.insert(record);
        } else {
            // fixme: 这里存在并发覆盖问题，推荐使用 update read_count set cnt = cnt + 1 where id = xxx
            record.setCnt(record.getCnt() + 1);
            readCountMapper.updateById(record);
        }
        return record.getCnt();
    }

    /**
     * 统计用户的文章计数
     *
     * @param userId
     * @return
     */
    public int countArticleByUser(Long userId) {
        return lambdaQuery().eq(ArticleDO::getUserId, userId)
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count().intValue();
    }


    /**
     * 热门文章推荐，适用于首页的侧边栏
     *
     * @param pageParam
     * @return
     */
    public List<SimpleArticleDTO> listHotArticles(PageParam pageParam) {
        return baseMapper.listArticlesByReadCounts(pageParam);
    }

    /**
     * 作者的热门文章推荐，适用于作者的详情页侧边栏
     *
     * @param userId
     * @param pageParam
     * @return
     */
    public List<SimpleArticleDTO> listAuthorHotArticles(long userId, PageParam pageParam) {
        return baseMapper.listArticlesByUserIdOrderByReadCounts(userId, pageParam);
    }

    /**
     * 根据相同的类目 + 标签进行推荐
     *
     * @param categoryId
     * @param tagIds
     * @return
     */
    public List<ArticleDO> listRelatedArticlesOrderByReadCount(Long categoryId, List<Long> tagIds, PageParam pageParam) {
        List<ReadCountDO> list = baseMapper.listArticleByCategoryAndTags(categoryId, tagIds, pageParam);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<Long> ids = list.stream().map(ReadCountDO::getDocumentId).collect(Collectors.toList());
        List<ArticleDO> result = baseMapper.selectBatchIds(ids);
        result.sort((o1, o2) -> {
            int i1 = ids.indexOf(o1.getId());
            int i2 = ids.indexOf(o2.getId());
            return Integer.compare(i1, i2);
        });
        return result;
    }


    /**
     * 根据用户ID获取创作历程
     *
     * @param userId
     * @return
     */
    public List<YearArticleDTO> listYearArticleByUserId(Long userId) {
        return baseMapper.listYearArticleByUserId(userId);
    }

    /**
     * 抽取样板代码
     */
    private LambdaQueryChainWrapper<ArticleDO> buildQuery(SearchArticleParams searchArticleParams) {
        return lambdaQuery()
                .like(StringUtils.isNotBlank(searchArticleParams.getTitle()), ArticleDO::getTitle, searchArticleParams.getTitle())
                // ID 不为空
                .eq(Objects.nonNull(searchArticleParams.getArticleId()), ArticleDO::getId, searchArticleParams.getArticleId())
                .eq(Objects.nonNull(searchArticleParams.getUserId()), ArticleDO::getUserId, searchArticleParams.getUserId())
                .eq(Objects.nonNull(searchArticleParams.getStatus()) && searchArticleParams.getStatus() != -1, ArticleDO::getStatus, searchArticleParams.getStatus())
                .eq(Objects.nonNull(searchArticleParams.getOfficalStat())&& searchArticleParams.getOfficalStat() != -1, ArticleDO::getOfficalStat, searchArticleParams.getOfficalStat())
                .eq(Objects.nonNull(searchArticleParams.getToppingStat())&& searchArticleParams.getToppingStat() != -1, ArticleDO::getToppingStat, searchArticleParams.getToppingStat())
                .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode());
    }


    /**
     * 文章列表（用于后台）
     *
     */
    public List<ArticleAdminDTO> listArticlesByParams(SearchArticleParams params) {
        return articleMapper.listArticlesByParams(params,
                PageParam.newPageInstance(params.getPageNum(), params.getPageSize()));
    }

    /**
     * 文章总数（用于后台）
     *
     */
    public Long countArticleByParams(SearchArticleParams searchArticleParams) {
        return articleMapper.countArticlesByParams(searchArticleParams);
    }

    /**
     * 文章总数（用于后台）
     *
     * @return
     */
    public Long countArticle() {
        return lambdaQuery()
                .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count();
    }

    public List<ArticleDO> selectByIds(List<Integer> ids) {

        List<ArticleDO> articleDOS = baseMapper.selectBatchIds(ids);
        return articleDOS;

    }
}