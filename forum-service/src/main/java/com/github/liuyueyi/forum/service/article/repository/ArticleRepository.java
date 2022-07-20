package com.github.liuyueyi.forum.service.article.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liuyueyi.forum.core.model.req.PageParam;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.CategoryDO;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;
import com.github.liuyueyi.forum.service.common.enums.PushStatusEnum;

import java.util.List;

/**
 * 文章相关DB操作
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface ArticleRepository {

    /**
     * 添加类目
     *
     * @param categoryDTO
     * @return
     */
    Long addCategory(CategoryDO categoryDTO);

    /**
     * 更新类目
     *
     * @param categoryId
     * @param categoryName
     */
    void updateCategory(Long categoryId, String categoryName);

    /**
     * 删除类目
     *
     * @param categoryId
     */
    void deleteCategory(Long categoryId);

    /**
     * 操作类目
     *
     * @param categoryId
     */
    void operateCategory(Long categoryId, PushStatusEnum pushStatusEnum);

    /**
     * 类目分页查询
     *
     * @return
     */
    IPage<CategoryDO> getCategoryByPage(PageParam pageParam);

    /**
     * 添加标签
     *
     * @param tagDTO
     * @return
     */
    Long addTag(TagDO tagDTO);

    /**
     * 更新标签
     *
     * @param tagId
     * @param tagName
     */
    void updateTag(Long tagId, String tagName);

    /**
     * 删除标签
     *
     * @param tagId
     */
    void deleteTag(Long tagId);

    /**
     * 上线/下线标签
     *
     * @param tagId
     */
    void operateTag(Long tagId, PushStatusEnum pushStatusEnum);

    /**
     * 标签分页查询
     *
     * @return
     */
    IPage<TagDO> getTagByPage(PageParam pageParam);

    /**
     * 根据类目ID查询标签列表
     *
     * @param categoryId
     * @return
     */
    List<TagDO> getTagListByCategoryId(Long categoryId);

    /**
     * 新增文章
     *
     * @param articleDTO
     * @return
     */
    Long addArticle(ArticleDO articleDTO);

    /**
     * 更新文章
     *
     * @param articleDTO
     */
    void updateArticle(ArticleDO articleDTO);

    /**
     * 删除文章
     *
     * @param articleId
     */
    void deleteArticle(Long articleId);

    /**
     * 上线/下线文章
     *
     * @param articleId
     * @param pushStatusEnum
     */
    void operateArticle(Long articleId, PushStatusEnum pushStatusEnum);

    /**
     * 分页获取文章列表
     *
     * @param pageParam
     * @return
     */
    IPage<ArticleDO> getArticleByPage(PageParam pageParam);
}
