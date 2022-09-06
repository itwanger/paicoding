package com.github.liuyueyi.forum.service.sidebar.service;

import com.github.liueyueyi.forum.api.model.vo.sidebar.SideBarDto;

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
    List<SideBarDto> queryHomeSidebarList();
}
