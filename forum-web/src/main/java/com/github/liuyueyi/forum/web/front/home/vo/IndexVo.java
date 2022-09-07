package com.github.liuyueyi.forum.web.front.home.vo;

import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.CarouseDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarDTO;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.Data;

import java.util.List;

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
    List<SideBarDTO> sideBarItems;

    /**
     * 轮播图
     */
    List<CarouseDTO> homeCarouselList;
}
