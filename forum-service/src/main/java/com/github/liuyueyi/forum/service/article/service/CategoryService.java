package com.github.liuyueyi.forum.service.article.service;

import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;

import java.util.List;

/**
 * 标签Service
 *
 * @author louzai
 * @date 2022-07-20
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
     * 查询所有的分离
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
}
