package com.github.paicoding.forum.web.controller.article.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.recommend.SideBarDTO;
import lombok.Data;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class ColumnVo {
    /**
     * 专栏列表
     */
    private PageListVo<ColumnDTO> columns;

    /**
     * mybatis-plus分页
     * 专栏列表
     */
    private IPage<ColumnDTO> columnPage;

    /**
     * 侧边栏信息
     */
    private List<SideBarDTO> sideBarItems;

}
