package com.github.liuyueyi.forum.service.sidebar.service;

import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarDTO;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/6
 */
public interface SidebarService {

    /**
     * 查询首页的侧边栏信息
     *
     * @return
     */
    List<SideBarDTO> queryHomeSidebarList();

    /**
     * 查询 PDF 的侧边栏
     *
     * @return
     */
    SideBarDTO pdfSideBar();
}
