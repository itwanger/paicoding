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
     * 根据类目ID查询标签列表
     *
     * @param categoryId
     * @return
     */
    List<TagDO> getTagListByCategoryId(Long categoryId);
}
