package com.github.liuyueyi.forum.service.article.service.impl;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.article.CategoryReq;
import com.github.liueyueyi.forum.api.model.vo.article.TagReq;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.article.conveter.ArticleConverter;
import com.github.liuyueyi.forum.service.article.repository.dao.CategoryDao;
import com.github.liuyueyi.forum.service.article.repository.entity.CategoryDO;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;
import com.github.liuyueyi.forum.service.article.service.CategorySettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类后台接口
 *
 * @author louzai
 * @date 2022-09-17
 */
@Service
public class CategorySettingServiceImpl implements CategorySettingService {

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public void saveCategory(CategoryReq categoryReq) {
        CategoryDO categoryDO = ArticleConverter.toDO(categoryReq);
        if (NumUtil.nullOrZero(categoryReq.getCategoryId())) {
            categoryDao.save(categoryDO);
        } else {
            categoryDO.setId(categoryReq.getCategoryId());
            categoryDao.updateById(categoryDO);
        }
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        CategoryDO categoryDO = categoryDao.getById(categoryId);
        if (categoryDO != null){
            categoryDao.removeById(categoryDO);
        }
    }

    @Override
    public void operateCategory(Integer categoryId, Integer pushStatus) {
        CategoryDO categoryDO = categoryDao.getById(categoryId);
        if (categoryDO != null){
            categoryDO.setStatus(pushStatus);
            categoryDao.updateById(categoryDO);
        }
    }

    @Override
    public PageVo<CategoryDTO> getCategoryList(PageParam pageParam) {
        List<CategoryDTO> tagDTOS = categoryDao.listCategory(pageParam);
        Integer totalCount = categoryDao.countCategory();
        return PageVo.build(tagDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }
}
