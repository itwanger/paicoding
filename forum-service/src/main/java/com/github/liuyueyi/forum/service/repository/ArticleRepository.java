package com.github.liuyueyi.forum.service.repository;

import com.github.liuyueyi.forum.service.repository.entity.CategoryDTO;
import com.github.liuyueyi.forum.service.repository.entity.TagDTO;

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
    Integer addCategory(CategoryDTO categoryDTO);

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
     * 发布类目
     * @param categoryId
     */
    void pushCategory(Integer categoryId);

    /**
     * 添加标签
     * @param tagDTO
     * @return
     */
    Integer addTag(TagDTO tagDTO);

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
     * 发布标签
     * @param tagId
     */
    void pushTag(Integer tagId);
}
