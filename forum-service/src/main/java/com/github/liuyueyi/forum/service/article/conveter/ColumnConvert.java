package com.github.liuyueyi.forum.service.article.conveter;

import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnArticleReq;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnReq;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnInfoDO;

import java.util.ArrayList;
import java.util.List;

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

    public static List<ColumnDTO> toDtos(List<ColumnInfoDO> columnInfoDOS) {
        List<ColumnDTO> columnDTOS = new ArrayList<>();
        columnInfoDOS.forEach(info -> columnDTOS.add(ColumnConvert.toDto(info)));
        return columnDTOS;
    }

    public static ColumnInfoDO toDo(ColumnReq columnReq) {
        if (columnReq == null) {
            return null;
        }
        ColumnInfoDO columnInfoDO = new ColumnInfoDO();
        columnInfoDO.setColumnName(columnReq.getColumnName());
        columnInfoDO.setUserId(columnReq.getUserId());
        columnInfoDO.setIntroduction(columnReq.getIntroduction());
        columnInfoDO.setCover(columnReq.getCover());
        columnInfoDO.setState(columnReq.getState());
        columnInfoDO.setPublishTime(columnReq.getPublishTime());
        return columnInfoDO;
    }

    public static ColumnArticleDO toDo(ColumnArticleReq columnArticleReq) {
        if (columnArticleReq == null) {
            return null;
        }
        ColumnArticleDO columnArticleDO = new ColumnArticleDO();
        columnArticleDO.setColumnId(columnArticleReq.getColumnId());
        columnArticleDO.setArticleId(columnArticleReq.getArticleId());
        columnArticleDO.setSection(columnArticleReq.getSection());
        return columnArticleDO;
    }
}
