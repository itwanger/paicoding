package com.github.liuyueyi.forum.service.article.conveter;

import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnInfoDO;

/**
 * @author YiHui
 * @date 2022/9/15
 */
public class ColumnConvert {

    public static ColumnDTO toDto(ColumnInfoDO info) {
        ColumnDTO dto = new ColumnDTO();
        dto.setColumnId(info.getId());
        dto.setColumn(info.getColumnName());
        dto.setCover(info.getCover());
        dto.setIntroduction(info.getIntroduction());
        dto.setState(info.getState());
        dto.setAuthor(info.getUserId());
        dto.setPublishTime(info.getPublishTime().getTime());
        return dto;
    }

}
