package com.github.liuyueyi.forum.service.sidebar.service;

import com.github.liueyueyi.forum.api.model.enums.SidebarStyleEnum;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarItemDto;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2022/9/6
 */
@Service
public class SidebarServiceImpl implements SidebarService {
    @Autowired
    private ArticleReadService articleReadService;

    @Override
    public List<SideBarDTO> queryHomeSidebarList() {
        return Arrays.asList(noticeSideBar(), hotArticles(), recommendSideBar(), aboutSideBar());
    }

    private SideBarDTO aboutSideBar() {
        return new SideBarDTO().setTitle("关于社区").setContent("一个技术爱好者的交流社区").setStyle(SidebarStyleEnum.ABOUT.getStyle());
    }

    private SideBarDTO recommendSideBar() {
        return new SideBarDTO().setTitle("微信公众号扫码").setSubTitle("加入交流社区").setIcon("https://tool.hhui.top/icon.svg").setImg("https://spring.hhui.top/spring-blog/imgs/info/wx.jpg").setContent("联系信息:\n yihuihuiyi@gmail.com").setStyle(SidebarStyleEnum.RECOMMEND.getStyle());

    }

    /**
     * 公告信息
     *
     * @return
     */
    private SideBarDTO noticeSideBar() {
        List<SideBarItemDto> items = new ArrayList<>();
        items.add(new SideBarItemDto().setTitle("学习加油站点 - Java程序员进阶之路").setUrl("https://tobebetterjavaer.com/").setTime(System.currentTimeMillis()));
        items.add(new SideBarItemDto().setTitle("学习加油站点 - 一灰灰的站点").setUrl("https://hhui.top").setTime(System.currentTimeMillis()));
        return new SideBarDTO().setTitle("公告").setItems(items).setStyle(SidebarStyleEnum.NOTICE.getStyle());
    }

    /**
     * 热门文章
     *
     * @return
     */
    private SideBarDTO hotArticles() {
        PageListVo<SimpleArticleDTO> vo = articleReadService.queryHotArticlesForRecommend(PageParam.newPageInstance());
        List<SideBarItemDto> items = vo.getList().stream().map(s -> new SideBarItemDto().setTitle(s.getTitle()).setUrl("/article/detail/" + s.getId()).setTime(s.getCreateTime().getTime())).collect(Collectors.toList());
        return new SideBarDTO().setTitle("热门推荐").setItems(items).setStyle(SidebarStyleEnum.ARTICLES.getStyle());
    }
}
