package com.github.liuyueyi.forum.service.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liuyueyi.forum.core.common.enums.PushStatusEnum;
import com.github.liuyueyi.forum.service.repository.entity.ArticleDTO;
import com.github.liuyueyi.forum.service.repository.entity.CategoryDTO;
import com.github.liuyueyi.forum.service.repository.entity.TagDTO;
import com.github.liuyueyi.forum.service.repository.param.PageParam;

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
     * @param categoryDTO
     * @return
     */
    Long addCategory(CategoryDTO categoryDTO);

    /**
     * 更新类目
     * @param categoryId
     * @param categoryName
     */
    void updateCategory(Integer categoryId, String categoryName);

    /**
     * 删除类目
     * @param categoryId
     */
    void deleteCategory(Integer categoryId);

    /**
     * 操作类目
     * @param categoryId
     */
    void operateCategory(Integer categoryId, PushStatusEnum pushStatusEnum);

    /**
     * 类目分页查询
     * @return
     */
    IPage<CategoryDTO> getCategoryByPage(PageParam pageParam);

    /**
     * 添加标签
     * @param tagDTO
     * @return
     */
    Long addTag(TagDTO tagDTO);

    /**
     * 更新标签
     * @param tagId
     * @param tagName
     */
    void updateTag(Integer tagId, String tagName);

    /**
     * 删除标签
     * @param tagId
     */
    void deleteTag(Integer tagId);

    /**
     * 上线/下线标签
     * @param tagId
     */
    void operateTag(Integer tagId, PushStatusEnum pushStatusEnum);

    /**
     * 标签分页查询
     * @return
     */
    IPage<TagDTO> getTagByPage(PageParam pageParam);

    /**
     * 根据类目ID查询标签列表
     * @param categoryId
     * @return
     */
    List<TagDTO> getTagListByCategoryId(Long categoryId);

    /**
     * 新增文章
     * @param articleDTO
     * @return
     */
    Long addArticle(ArticleDTO articleDTO);

    /**
     * 更新文章
     * @param articleDTO
     */
    void updateArticle(ArticleDTO articleDTO);

    /**
     * 删除文章
     * @param articleId
     */
    void deleteArticle(Long articleId);

    /**
     * 上线/下线文章
     * @param articleId
     * @param pushStatusEnum
     */
    void opreateArticle(Long articleId, PushStatusEnum pushStatusEnum);

    /**
     * 分页获取文章列表
     * @param pageParam
     * @return
     */
    IPage<ArticleDTO> getArticleByPage(PageParam pageParam);
}
