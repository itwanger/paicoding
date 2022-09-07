package com.github.liuyueyi.forum.web.front.home.helper;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.CarouseDTO;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.service.article.service.CategoryService;
import com.github.liuyueyi.forum.service.sidebar.service.SidebarService;
import com.github.liuyueyi.forum.service.user.service.UserService;
import com.github.liuyueyi.forum.web.front.home.vo.IndexVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页推荐相关
 *
 * @author YiHui
 * @date 2022/9/6
 */
@Component
public class IndexRecommendHelper {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ArticleReadService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private SidebarService sidebarService;

    public IndexVo buildIndexVo(String activeTab) {
        IndexVo vo = new IndexVo();
        Long categoryId = categories(activeTab, vo);
        vo.setArticles(articleList(categoryId, PageParam.DEFAULT_PAGE_NUM, PageParam.DEFAULT_PAGE_SIZE));
        vo.setHomeCarouselList(homeCarouselList());
        vo.setSideBarItems(sidebarService.queryHomeSidebarList());
        vo.setCurrentCategory(categoryId == null ? "全部": activeTab);
        vo.setUser(loginInfo());
        return vo;
    }

    public IndexVo buildSearchVo(String key) {
        IndexVo vo = new IndexVo();
        vo.setArticles(articleService.queryArticlesBySearchKey(key, PageParam.newPageInstance()));
        vo.setSideBarItems(sidebarService.queryHomeSidebarList());
        return vo;
    }

    /**
     * 轮播图
     *
     * @return
     */
    private List<CarouseDTO> homeCarouselList() {
        List<CarouseDTO> list = new ArrayList<>();
        list.add(new CarouseDTO().setName("Spring社区").setImgUrl("https://spring.hhui.top/spring-blog/imgs/220425/logo.jpg").setActionUrl("https://spring.hhui.top"));
        list.add(new CarouseDTO().setName("一灰灰社区").setImgUrl("https://spring.hhui.top/spring-blog/imgs/220422/logo.jpg").setActionUrl("https://hhui.top"));
        return list;
    }

    /**
     * 文章列表
     */
    private PageListVo<ArticleDTO> articleList(Long categoryId, Long page, Long size) {
        if (page == null) page = PageParam.DEFAULT_PAGE_NUM;
        if (size == null) size = PageParam.DEFAULT_PAGE_SIZE;
        return articleService.queryArticlesByCategory(categoryId, PageParam.newPageInstance(page, size));
    }


    /**
     * 返回分类列表
     *
     * @param active
     * @return
     */
    private Long categories(String active, IndexVo vo) {
        List<CategoryDTO> list = categoryService.loadAllCategories();
        list.add(0, new CategoryDTO(0L, CategoryDTO.DEFAULT_TOTAL_CATEGORY, false));
        Long selectCategoryId = null;
        for (CategoryDTO c : list) {
            if (c.getCategory().equalsIgnoreCase(active)) {
                selectCategoryId = c.getCategoryId();
                c.setSelected(true);
            } else {
                c.setSelected(false);
            }
        }

        if (selectCategoryId == null) {
            // 未匹配时，默认选全部
            list.get(0).setSelected(true);
        }
        vo.setCategories(list);
        return selectCategoryId;
    }


    private UserStatisticInfoDTO loginInfo() {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        if (userId != null) {
            return userService.queryUserInfoWithStatistic(userId);
        }
        return null;
    }


}
