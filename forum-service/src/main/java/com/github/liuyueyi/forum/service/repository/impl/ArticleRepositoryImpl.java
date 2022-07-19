package com.github.liuyueyi.forum.service.repository.impl;

import com.github.liuyueyi.forum.core.common.enums.YesOrNoEnum;
import com.github.liuyueyi.forum.service.repository.ArticleRepository;
import com.github.liuyueyi.forum.service.repository.entity.CategoryDTO;
import com.github.liuyueyi.forum.service.repository.entity.TagDTO;
import com.github.liuyueyi.forum.service.repository.mapper.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    public Integer addCategory(CategoryDTO categoryDTO) {
        categoryMapper.insert(categoryDTO);
        return categoryDTO.getId();
    }

    @Override
    public void updateCategory(Integer categoryId, String categoryName) {
        CategoryDTO categoryDTO = categoryMapper.selectById(categoryId);
        if (categoryDTO != null) {
            categoryDTO.setCategoryName(categoryName);
            categoryDTO.setStatus(YesOrNoEnum.NO.getCode());
            categoryMapper.updateById(categoryDTO);
        }
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        CategoryDTO categoryDTO = categoryMapper.selectById(categoryId);
        if (categoryDTO != null) {
            categoryDTO.setDeleted(YesOrNoEnum.YES.getCode());
            categoryMapper.updateById(categoryDTO);
        }
    }

    @Override
    public void pushCategory(Integer categoryId) {
        CategoryDTO categoryDTO = categoryMapper.selectById(categoryId);
        if (categoryDTO != null) {
            categoryDTO.setStatus(YesOrNoEnum.YES.getCode());
            categoryMapper.updateById(categoryDTO);
        }
    }

    @Override
    public Integer addTag(TagDTO tagDTO) {
        tagMapper.insert(tagDTO);
        return tagDTO.getId();
    }

    @Override
    public void updateTag(Integer tagId, String tagName) {
        TagDTO tagDTO = tagMapper.selectById(tagId);
        if (tagDTO != null) {
            tagDTO.setTagName(tagName);
            tagDTO.setStatus(YesOrNoEnum.NO.getCode());
            tagMapper.updateById(tagDTO);
        }
    }

    @Override
    public void deleteTag(Integer tagId) {
        TagDTO tagDTO = tagMapper.selectById(tagId);
        if (tagDTO != null) {
            tagDTO.setDeleted(YesOrNoEnum.YES.getCode());
            tagMapper.updateById(tagDTO);
        }
    }

    @Override
    public void pushTag(Integer tagId) {
        TagDTO tagDTO = tagMapper.selectById(tagId);
        if (tagDTO != null) {
            tagDTO.setStatus(YesOrNoEnum.YES.getCode());
            tagMapper.updateById(tagDTO);
        }
    }


}