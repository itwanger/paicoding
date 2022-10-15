package com.github.liuyueyi.forum.service.sidebar.service;

import com.github.liueyueyi.forum.api.model.enums.ConfigTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.SidebarStyleEnum;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarItemDto;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.service.config.service.ConfigService;
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

    @Autowired
    private ConfigService configService;

    @Override
    public List<SideBarDTO> queryHomeSidebarList() {
        return Arrays.asList(noticeSideBar(), hotArticles(), recommendSideBar(), aboutSideBar());
    }

    private SideBarDTO aboutSideBar() {
        return new SideBarDTO().setTitle("关于社区").setContent("技术社区是一个技术学习交流平台，你可以从中获取到大量的学习资料，甚至从 0 到 1 搭建该社区的全套教程，无论是工作、学习，还是面试，都能给予非常大的帮助，同时该平台也能帮你答疑解惑，一个人可以走得很快，但是一群人才能走得更远，欢迎加入我们！").setStyle(SidebarStyleEnum.ABOUT.getStyle());
    }

    private SideBarDTO recommendSideBar() {
        return new SideBarDTO().setTitle("加入\"社区技术交流群\"").setSubTitle("")
                .setIcon("https://tool.hhui.top/icon.svg")
                .setImg("https://spring.hhui.top/spring-blog/imgs/info/wx.jpg")
                .setContent("创始人微信：<br/> lml200701158（楼仔）<br/> qing_gee（沉默王二）")
                .setStyle(SidebarStyleEnum.RECOMMEND.getStyle());
    }

    /**
     * 公告信息
     *
     * @return
     */
    private SideBarDTO noticeSideBar() {
        List<ConfigDTO> configDTOS = configService.getConfigList(ConfigTypeEnum.NOTICE);
        List<SideBarItemDto> items = new ArrayList<>();
        configDTOS.forEach(configDTO -> {
            items.add(new SideBarItemDto().setTitle(configDTO.getContent()).setUrl(configDTO.getJumpUrl()).setTime(configDTO.getCreateTime().getTime()));
        });
        return new SideBarDTO().setTitle("公告").setItems(items).setStyle(SidebarStyleEnum.NOTICE.getStyle());
    }

    /**
     * 热门文章
     *
     * @return
     */
    private SideBarDTO hotArticles() {
        PageListVo<SimpleArticleDTO> vo = articleReadService.queryHotArticlesForRecommend(PageParam.newPageInstance(1,5));
        List<SideBarItemDto> items = vo.getList().stream().map(s -> new SideBarItemDto().setTitle(s.getTitle()).setUrl("/article/detail/" + s.getId()).setTime(s.getCreateTime().getTime())).collect(Collectors.toList());
        return new SideBarDTO().setTitle("热门推荐").setItems(items).setStyle(SidebarStyleEnum.ARTICLES.getStyle());
    }
}
