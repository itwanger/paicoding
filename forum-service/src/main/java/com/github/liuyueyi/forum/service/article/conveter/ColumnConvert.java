package com.github.liuyueyi.forum.service.article.conveter;

import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnArticleReq;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnReq;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.liuyueyi.forum.core.util.DateUtil;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ColumnInfoDO;

import java.util.ArrayList;
import java.util.Date;
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
        dto.setNums(info.getNums());
        dto.setAuthor(info.getUserId());
        dto.setSection(info.getSection());
        dto.setPublishTime(info.getPublishTime().getTime());
        dto.setType(info.getType());
        dto.setFreeStartTime(info.getFreeStartTime().getTime());
        dto.setFreeEndTime(info.getFreeEndTime().getTime());
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
        columnInfoDO.setColumnName(columnReq.getColumn());
        columnInfoDO.setUserId(columnReq.getAuthor());
        columnInfoDO.setIntroduction(columnReq.getIntroduction());
        columnInfoDO.setCover(columnReq.getCover());
        columnInfoDO.setState(columnReq.getState());
        columnInfoDO.setSection(columnReq.getSection());
        columnInfoDO.setNums(columnReq.getNums());
        columnInfoDO.setType(columnInfoDO.getType());
        columnInfoDO.setFreeStartTime(columnInfoDO.getFreeStartTime());
        columnInfoDO.setFreeEndTime(columnInfoDO.getFreeEndTime());
        return columnInfoDO;
    }

    public static ColumnArticleDO toDo(ColumnArticleReq columnArticleReq) {
        if (columnArticleReq == null) {
            return null;
        }
        ColumnArticleDO columnArticleDO = new ColumnArticleDO();
        columnArticleDO.setColumnId(columnArticleReq.getColumnId());
        columnArticleDO.setArticleId(columnArticleReq.getArticleId());
        columnArticleDO.setSection(columnArticleReq.getSort());
        return columnArticleDO;
    }
}
