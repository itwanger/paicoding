package com.github.liuyueyi.forum.web.front.home.vo;

import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liueyueyi.forum.api.model.vo.sidebar.SideBarDto;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author YiHui
 * @date 2022/9/6
 */
@Data
public class IndexVo {
    /**
     * 当前选中的分类
     */
    private String currentArticle;
    /**
     * 分类列表
     */
    List<CategoryDTO> categories;

    /**
     * 文章列表
     */
    ArticleListDTO articles;

    /**
     * 登录用户信息
     */
    UserStatisticInfoDTO user;

    /**
     * 侧边栏信息
     */
    List<SideBarDto> sideBarItems;

    /**
     * 轮播图，fixme 待调整
     */
    List<Map<String, Object>> homeCarouselList;
}
