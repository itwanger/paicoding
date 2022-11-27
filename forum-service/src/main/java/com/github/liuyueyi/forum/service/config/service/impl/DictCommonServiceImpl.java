package com.github.liuyueyi.forum.service.config.service.impl;

import com.github.liueyueyi.forum.api.model.vo.article.dto.DictCommonDTO;
import com.github.liuyueyi.forum.core.util.MapUtils;
import com.github.liuyueyi.forum.service.config.converter.DictCommonConverter;
import com.github.liuyueyi.forum.service.config.repository.dao.DictCommonDao;
import com.github.liuyueyi.forum.service.config.service.DictCommonService;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 字典Service
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class DictCommonServiceImpl implements DictCommonService {

    @Resource
    private DictCommonDao dictCommonDao;

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

        result.putAll(dictCommonMap);
        return result;
    }

}
