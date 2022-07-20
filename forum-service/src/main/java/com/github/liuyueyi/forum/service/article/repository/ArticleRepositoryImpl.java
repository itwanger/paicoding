package com.github.liuyueyi.forum.service.article.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.liuyueyi.forum.core.model.req.PageParam;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.CategoryDO;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.*;
import com.github.liuyueyi.forum.service.common.enums.PushStatusEnum;
import com.github.liuyueyi.forum.service.common.enums.YesOrNoEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文章相关DB操作
 *
 * @author louzai
 * @date 2022-07-18
 */
@Service
public class ArticleRepositoryImpl implements ArticleRepository {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticleTagMapper articleTagMapper;

    @Resource
    private ArticleDetailMapper articleDetailMapper;

    @Override
    public Long addCategory(CategoryDO categoryDTO) {
        categoryMapper.insert(categoryDTO);
        return categoryDTO.getId();
    }

    @Override
    public void updateCategory(Long categoryId, String categoryName) {
        CategoryDO categoryDTO = categoryMapper.selectById(categoryId);
        if (categoryDTO != null) {
            categoryDTO.setCategoryName(categoryName);
            categoryDTO.setStatus(YesOrNoEnum.NO.getCode());
            categoryMapper.updateById(categoryDTO);
        }
    }

    @Override
    public void deleteCategory(Long categoryId) {
        CategoryDO categoryDTO = categoryMapper.selectById(categoryId);
        if (categoryDTO != null) {
            categoryDTO.setDeleted(YesOrNoEnum.YES.getCode());
            categoryMapper.updateById(categoryDTO);
        }
    }

    @Override
    public void operateCategory(Long categoryId, PushStatusEnum pushStatusEnum) {
        CategoryDO categoryDTO = categoryMapper.selectById(categoryId);
        if (categoryDTO != null) {
            categoryDTO.setStatus(pushStatusEnum.getCode());
            categoryMapper.updateById(categoryDTO);
        }
    }

    @Override
    public IPage<CategoryDO> getCategoryByPage(PageParam pageParam) {
        LambdaQueryWrapper<CategoryDO> query = Wrappers.lambdaQuery();
        query.eq(CategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(CategoryDO::getStatus, PushStatusEnum.ONLINE.getCode());
        Page<CategoryDO> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        return categoryMapper.selectPage(page, query);
    }

    @Override
    public Long addTag(TagDO tagDTO) {
        tagMapper.insert(tagDTO);
        return tagDTO.getId();
    }

    @Override
    public void updateTag(Long tagId, String tagName) {
        TagDO tagDTO = tagMapper.selectById(tagId);
        if (tagDTO != null) {
            tagDTO.setTagName(tagName);
            tagDTO.setStatus(YesOrNoEnum.NO.getCode());
            tagMapper.updateById(tagDTO);
        }
    }

    @Override
    public void deleteTag(Long tagId) {
        TagDO tagDTO = tagMapper.selectById(tagId);
        if (tagDTO != null) {
            tagDTO.setDeleted(YesOrNoEnum.YES.getCode());
            tagMapper.updateById(tagDTO);
        }
    }

    @Override
    public void operateTag(Long tagId, PushStatusEnum pushStatusEnum) {
        TagDO tagDTO = tagMapper.selectById(tagId);
        if (tagDTO != null) {
            tagDTO.setStatus(pushStatusEnum.getCode());
            tagMapper.updateById(tagDTO);
        }
    }

    @Override
    public IPage<TagDO> getTagByPage(PageParam pageParam) {
        LambdaQueryWrapper<TagDO> query = Wrappers.lambdaQuery();
        query.eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(TagDO::getStatus, PushStatusEnum.ONLINE.getCode());
        Page<TagDO> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        return tagMapper.selectPage(page, query);
    }

    @Override
    public List<TagDO> getTagListByCategoryId(Long categoryId) {
        LambdaQueryWrapper<TagDO> query = Wrappers.lambdaQuery();
        query.eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(TagDO::getCategoryId, categoryId);
        return tagMapper.selectList(query);
    }

    @Override
    public Long addArticle(ArticleDO articleDTO) {
        articleMapper.insert(articleDTO);
        return articleDTO.getId();
    }

    @Override
    public void updateArticle(ArticleDO articleDTO) {
        ArticleDO updateArticle = articleMapper.selectById(articleDTO.getId());
        if (updateArticle != null) {
            articleMapper.updateById(articleDTO);
        }
    }

    @Override
    public void deleteArticle(Long articleId) {
        ArticleDO articleDTO = articleMapper.selectById(articleId);
        if (articleDTO != null) {
            articleDTO.setDeleted(YesOrNoEnum.YES.getCode());
            articleMapper.updateById(articleDTO);
        }
    }

    @Override
    public void operateArticle(Long articleId, PushStatusEnum pushStatusEnum) {
        ArticleDO articleDTO = articleMapper.selectById(articleId);
        if (articleDTO != null) {
            articleDTO.setStatus(pushStatusEnum.getCode());
            articleMapper.updateById(articleDTO);
        }
    }

    @Override
    public IPage<ArticleDO> getArticleByPage(PageParam pageParam) {
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode());
        Page<ArticleDO> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        return articleMapper.selectPage(page, query);
    }
}