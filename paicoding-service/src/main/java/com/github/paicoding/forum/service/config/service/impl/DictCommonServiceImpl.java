package com.github.paicoding.forum.service.config.service.impl;

import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.DictCommonDTO;
import com.github.paicoding.forum.service.article.service.CategoryService;
import com.github.paicoding.forum.service.config.repository.dao.DictCommonDao;
import com.github.paicoding.forum.service.config.service.DictCommonService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典Service
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Service
public class DictCommonServiceImpl implements DictCommonService {

    @Resource
    private DictCommonDao dictCommonDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public Map<String, Object> getDict() {
        Map<String, Object> result = Maps.newLinkedHashMap();

        List<DictCommonDTO> dictCommonList = dictCommonDao.getDictList();

        Map<String, Map<String, String>> dictCommonMap = Maps.newLinkedHashMap();
        for (DictCommonDTO dictCommon : dictCommonList) {
                Map<String, String> codeMap = dictCommonMap.get(dictCommon.getTypeCode());
                if (codeMap == null || codeMap.isEmpty()) {
                    codeMap = Maps.newLinkedHashMap();
                    dictCommonMap.put(dictCommon.getTypeCode(), codeMap);
                }
                codeMap.put(dictCommon.getDictCode(), dictCommon.getDictDesc());
        }

        // 获取分类的字典信息
        List<CategoryDTO> categoryDTOS = categoryService.loadAllCategories();
        Map<String, String> codeMap = new HashMap<>();
        categoryDTOS.forEach(categoryDTO -> codeMap.put(categoryDTO.getCategoryId().toString(), categoryDTO.getCategory()));
        dictCommonMap.put("CategoryType", codeMap);

        result.putAll(dictCommonMap);
        return result;
    }

}
