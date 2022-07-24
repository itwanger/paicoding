package com.github.liuyueyi.forum.service.article.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.liueyueyi.forum.api.model.enums.SourceTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liuyueyi.forum.service.article.dto.ArticleDTO;
import com.github.liuyueyi.forum.service.article.dto.CategoryDTO;
import com.github.liuyueyi.forum.service.article.dto.TagDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDetailDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleTagDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleDetailMapper;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleMapper;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章相关DB操作
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

    /**
     * 查询文章详情
     *
     * @param articleId
     * @return
     */
    public ArticleDTO queryArticleDetail(Long articleId) {
        // 查询文章记录
        ArticleDO article = articleMapper.selectById(articleId);
        if (article == null) {
            return null;
        }


        // 查询文章关联标签
        ArticleDetailDO detail = findLatestDetail(articleId);
        List<ArticleTagDO> tagList = findArticleTags(articleId);

        ArticleDTO dto = new ArticleDTO();
        dto.setAuthor(article.getUserId());
        dto.setArticleId(articleId);
        dto.setTitle(article.getTitle());
        dto.setShortTitle(article.getShortTitle());
        dto.setSummary(article.getSummary());
        dto.setCover(article.getPicture());
        dto.setSourceType(SourceTypeEnum.formCode(article.getSource()).getDesc());
        dto.setSourceUrl(article.getSourceUrl());
        dto.setStatus(article.getStatus());
        dto.setLastUpdateTime(article.getUpdateTime().getTime());
        dto.setContent(detail.getContent());
        // 设置类目id
        dto.setCategory(new CategoryDTO(article.getCategoryId(), null));
        // 设置标签列表
        dto.setTags(tagList.stream().map(s -> new TagDTO(s.getTagId())).collect(Collectors.toList()));
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

    private List<ArticleTagDO> findArticleTags(long articleId) {
        LambdaQueryWrapper<ArticleTagDO> contentQuery = Wrappers.lambdaQuery();
        return articleTagMapper.selectList(contentQuery.eq(ArticleTagDO::getArticleId, articleId)
                .eq(ArticleTagDO::getDeleted, YesOrNoEnum.NO.getCode()));
    }


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
        List<ArticleTagDO> dbTags = findArticleTags(articleId);
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
        articleTagMapper.deleteBatchIds(toDeleted);

        if (!newTags.isEmpty()) {
            saveTags(articleId, newTags);
        }
    }

    private void insertArticle(ArticleDO article, String content, Set<Long> tags) {
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

}