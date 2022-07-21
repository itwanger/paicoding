package com.github.liuyueyi.forum.service.article.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.liuyueyi.forum.core.model.req.PageParam;
import com.github.liuyueyi.forum.service.article.ArticleService;
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
 * 文章Service
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class ArticleServiceImpl implements ArticleService {

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

    /**
     * 更新类目
     *
     * @param categoryId
     * @param categoryName
     */
    private void updateCategory(Long categoryId, String categoryName) {
        CategoryDO categoryDTO = categoryMapper.selectById(categoryId);
        if (categoryDTO != null) {
            categoryDTO.setCategoryName(categoryName);
            categoryDTO.setStatus(YesOrNoEnum.NO.getCode());
            categoryMapper.updateById(categoryDTO);
        }
    }

    /**
     * 删除类目
     *
     * @param categoryId
     */
    private void deleteCategory(Long categoryId) {
        CategoryDO categoryDTO = categoryMapper.selectById(categoryId);
        if (categoryDTO != null) {
            categoryDTO.setDeleted(YesOrNoEnum.YES.getCode());
            categoryMapper.updateById(categoryDTO);
        }
    }

    /**
     * 操作类目
     *
     * @param categoryId
     */
    private void operateCategory(Long categoryId, PushStatusEnum pushStatusEnum) {
        CategoryDO categoryDTO = categoryMapper.selectById(categoryId);
        if (categoryDTO != null) {
            categoryDTO.setStatus(pushStatusEnum.getCode());
            categoryMapper.updateById(categoryDTO);
        }
    }

    /**
     * 类目分页查询
     *
     * @return
     */
    private IPage<CategoryDO> getCategoryByPage(PageParam pageParam) {
        LambdaQueryWrapper<CategoryDO> query = Wrappers.lambdaQuery();
        query.eq(CategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(CategoryDO::getStatus, PushStatusEnum.ONLINE.getCode());
        Page<CategoryDO> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        return categoryMapper.selectPage(page, query);
    }

    /**
     * 更新标签
     *
     * @param tagId
     * @param tagName
     */
    private void updateTag(Long tagId, String tagName) {
        TagDO tagDTO = tagMapper.selectById(tagId);
        if (tagDTO != null) {
            tagDTO.setTagName(tagName);
            tagDTO.setStatus(YesOrNoEnum.NO.getCode());
            tagMapper.updateById(tagDTO);
        }
    }

    /**
     * 删除标签
     *
     * @param tagId
     */
    private void deleteTag(Long tagId) {
        TagDO tagDTO = tagMapper.selectById(tagId);
        if (tagDTO != null) {
            tagDTO.setDeleted(YesOrNoEnum.YES.getCode());
            tagMapper.updateById(tagDTO);
        }
    }

    /**
     * 上线/下线标签
     *
     * @param tagId
     */
    private void operateTag(Long tagId, PushStatusEnum pushStatusEnum) {
        TagDO tagDTO = tagMapper.selectById(tagId);
        if (tagDTO != null) {
            tagDTO.setStatus(pushStatusEnum.getCode());
            tagMapper.updateById(tagDTO);
        }
    }

    /**
     * 标签分页查询
     *
     * @return
     */
    private IPage<TagDO> getTagByPage(PageParam pageParam) {
        LambdaQueryWrapper<TagDO> query = Wrappers.lambdaQuery();
        query.eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(TagDO::getStatus, PushStatusEnum.ONLINE.getCode());
        Page<TagDO> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        return tagMapper.selectPage(page, query);
    }

    /**
     * 根据类目ID查询标签列表
     *
     * @param categoryId
     * @return
     */
    private List<TagDO> getTagListByCategoryId(Long categoryId) {
        LambdaQueryWrapper<TagDO> query = Wrappers.lambdaQuery();
        query.eq(TagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(TagDO::getCategoryId, categoryId);
        return tagMapper.selectList(query);
    }

    /**
     * 更新文章
     *
     * @param articleDTO
     */
    private void updateArticle(ArticleDO articleDTO) {
        ArticleDO updateArticle = articleMapper.selectById(articleDTO.getId());
        if (updateArticle != null) {
            articleMapper.updateById(articleDTO);
        }
    }

    /**
     * 删除文章
     *
     * @param articleId
     */
    private void deleteArticle(Long articleId) {
        ArticleDO articleDTO = articleMapper.selectById(articleId);
        if (articleDTO != null) {
            articleDTO.setDeleted(YesOrNoEnum.YES.getCode());
            articleMapper.updateById(articleDTO);
        }
    }

    /**
     * 上线/下线文章
     *
     * @param articleId
     * @param pushStatusEnum
     */
    private void operateArticle(Long articleId, PushStatusEnum pushStatusEnum) {
        ArticleDO articleDTO = articleMapper.selectById(articleId);
        if (articleDTO != null) {
            articleDTO.setStatus(pushStatusEnum.getCode());
            articleMapper.updateById(articleDTO);
        }
    }

    /**
     * 分页获取文章列表
     *
     * @param pageParam
     * @return
     */
    private IPage<ArticleDO> getArticleByPage(PageParam pageParam) {
        LambdaQueryWrapper<ArticleDO> query = Wrappers.lambdaQuery();
        query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode());
        Page<ArticleDO> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        return articleMapper.selectPage(page, query);
    }
}
