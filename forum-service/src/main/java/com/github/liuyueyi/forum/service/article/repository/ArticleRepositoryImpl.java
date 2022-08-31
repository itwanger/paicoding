package com.github.liuyueyi.forum.service.article.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.liueyueyi.forum.api.model.enums.DocumentTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liuyueyi.forum.service.article.conveter.ArticleConverter;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDetailDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleTagDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ReadCountDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleDetailMapper;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleMapper;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleTagMapper;
import com.github.liuyueyi.forum.service.article.repository.mapper.ReadCountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 文章相关DB操作
 * <p>
 * 多表结构的操作封装，只与DB操作相关
 *
 * @author louzai
 * @date 2022-07-18
 */
@Repository
public class ArticleRepositoryImpl implements ArticleRepository {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleDetailMapper articleDetailMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private ReadCountMapper readCountMapper;
    @Autowired
    private ArticleConverter articleConverter;

    /**
     * 查询文章详情
     *
     * @param articleId
     * @return
     */
    @Override
    public ArticleDTO queryArticleDetail(Long articleId) {
        // 查询文章记录
        ArticleDO article = articleMapper.selectById(articleId);
        if (article == null) {
            return null;
        }


        // 查询文章关联标签
        ArticleDTO dto = articleConverter.toDTO(article);
        ArticleDetailDO detail = findLatestDetail(articleId);
        dto.setContent(detail.getContent());
        return dto;
    }

    private ArticleDetailDO findLatestDetail(long articleId) {
        // 查询文章内容
        LambdaQueryWrapper<ArticleDetailDO> contentQuery = Wrappers.lambdaQuery();
        contentQuery.eq(ArticleDetailDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDetailDO::getArticleId, articleId)
                .orderByDesc(ArticleDetailDO::getVersion);
        return articleDetailMapper.selectOne(contentQuery);
    }

    @Override
    public Long saveArticle(ArticleDO article, String content, Set<Long> tags) {
        if (article.getId() != null) {
            updateArticle(article, content, tags);
        } else {
            insertArticle(article, content, tags);
        }
        return article.getId();
    }

    private void updateArticle(ArticleDO article, String content, Set<Long> tags) {
        // 更新文章
        articleMapper.updateById(article);
        // 更新内容
        doUpdateArticleDetail(article.getId(), content);

        // 标签更新，先查出之前的标签列表
        updateTags(article.getId(), tags);
    }

    private void doUpdateArticleDetail(long articleId, String content) {
        articleDetailMapper.updateContent(articleId, content);
    }

    private void updateTags(Long articleId, Set<Long> newTags) {
        List<ArticleTagDO> dbTags = articleTagMapper.queryArticleTags(articleId);
        // 在旧的里面，不在新的里面的标签，设置为删除
        List<Long> toDeleted = new ArrayList<>();
        dbTags.forEach(tag -> {
            if (!newTags.contains(tag.getTagId())) {
                toDeleted.add(tag.getId());
            } else {
                // 移除已经存在的记录
                newTags.remove(tag.getTagId());
            }
        });
        if (!toDeleted.isEmpty()) {
            articleTagMapper.deleteBatchIds(toDeleted);
        }

        if (!newTags.isEmpty()) {
            saveTags(articleId, newTags);
        }
    }

    private void insertArticle(ArticleDO article, String content, Set<Long> tags) {
        // article + article_detail + tag  三张表的数据变更
        articleMapper.insert(article);
        Long articleId = article.getId();

        ArticleDetailDO detail = new ArticleDetailDO();
        detail.setArticleId(articleId);
        detail.setContent(content);
        detail.setVersion(1L);
        articleDetailMapper.insert(detail);

        saveTags(articleId, tags);
    }

    private void saveTags(Long articleId, Set<Long> tags) {
        List<ArticleTagDO> insertList = new ArrayList<>(tags.size());
        tags.forEach(s -> {
            ArticleTagDO tag = new ArticleTagDO();
            tag.setTagId(s);
            tag.setArticleId(articleId);
            tag.setDeleted(YesOrNoEnum.NO.getCode());
            insertList.add(tag);
        });
        articleTagMapper.batchInsert(insertList);
    }


    @Override
    public ArticleDO getSimpleArticle(Long articleId) {
        return articleMapper.selectById(articleId);
    }

    @Override
    public List<ArticleDO> getArticleListByUserId(Long userId, PageParam pageParam) {
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .eq(ArticleDO::getUserId, userId)
                .last(PageParam.getLimitSql(pageParam))
                .orderByDesc(ArticleDO::getId);
        return articleMapper.selectList(query);
    }

    @Override
    public List<ArticleDO> getArticleListByCategoryId(Long categoryId, PageParam pageParam) {
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode());
        Optional.ofNullable(categoryId).ifPresent(cid -> query.eq(ArticleDO::getCategoryId, cid));
        query.last(PageParam.getLimitSql(pageParam))
                .orderByDesc(ArticleDO::getId);
        return articleMapper.selectList(query);
    }

    @Override
    public List<ArticleDO> getArticleListByBySearchKey(String key, PageParam pageParam) {
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
        return articleMapper.selectList(query);
    }

    @Override
    public int count(Long articleId) {
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
}