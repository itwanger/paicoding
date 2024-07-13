package com.github.paicoding.forum.service.config.converter;

import com.github.paicoding.forum.api.model.vo.article.dto.DictCommonDTO;
import com.github.paicoding.forum.service.config.repository.entity.DictCommonDO;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Banner转换
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public class DictCommonConverter {

    public static List<DictCommonDTO> toDTOS(List<DictCommonDO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        return records.stream().map(DictCommonConverter::toDTO).collect(Collectors.toList());
    }

    public static DictCommonDTO toDTO(DictCommonDO dictCommonDO) {
        if (dictCommonDO == null) {
            return null;
        }
        DictCommonDTO dictCommonDTO = new DictCommonDTO();
        dictCommonDTO.setTypeCode(dictCommonDO.getTypeCode());
        dictCommonDTO.setDictCode(dictCommonDO.getDictCode());
        dictCommonDTO.setDictDesc(dictCommonDO.getDictDesc());
        dictCommonDTO.setSortNo(dictCommonDO.getSortNo());
        return dictCommonDTO;
    }
}
