package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;

import java.util.List;

/**
 * 标签Service
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface CategoryService {
    /**
     * 查询类目名
     *
     * @param categoryId
     * @return
     */
    String queryCategoryName(Long categoryId);


    /**
     * 查询所有的分类
     *
     * @return
     */
    List<CategoryDTO> loadAllCategories();

    /**
     * 查询类目id
     *
     * @param category
     * @return
     */
    Long queryCategoryId(String category);


    /**
     * 刷新缓存
     */
    public void refreshCache();
}
