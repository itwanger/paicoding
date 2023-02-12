package com.github.paicoding.forum.service.sidebar.service;

import com.github.paicoding.forum.api.model.enums.ConfigTypeEnum;
import com.github.paicoding.forum.api.model.enums.SidebarStyleEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.paicoding.forum.api.model.vo.recommend.RateVisitDTO;
import com.github.paicoding.forum.api.model.vo.recommend.SideBarDTO;
import com.github.paicoding.forum.api.model.vo.recommend.SideBarItemDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.config.service.ConfigService;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
        List<SideBarDTO> list = new ArrayList<>();
        list.add(noticeSideBar());
        list.add(columnSideBar());
        list.add(hotArticles());
        return list;
    }

    @Override
    public List<SideBarDTO> queryColumnSidebarList() {
        List<SideBarDTO> list = new ArrayList<>();
        list.add(subscribeSideBar());
        return list;
    }

    @Override
    public List<SideBarDTO> queryArticleDetailSidebarList() {
        List<SideBarDTO> list = new ArrayList<>();
        list.add(pdfSideBar());
        return list;
    }

    /**
     * 推荐教程的侧边栏
     *
     * @return
     */
    private SideBarDTO columnSideBar() {
        List<ConfigDTO> columnList = configService.getConfigList(ConfigTypeEnum.COLUMN);
        List<SideBarItemDTO> items = new ArrayList<>(columnList.size());
        columnList.forEach(configDTO -> {
            SideBarItemDTO item = new SideBarItemDTO();
            item.setName(configDTO.getName());
            item.setTitle(configDTO.getContent());
            item.setUrl(configDTO.getJumpUrl());
            item.setImg(configDTO.getBannerUrl());
            items.add(item);
        });
        return new SideBarDTO().setTitle("精选教程").setItems(items).setStyle(SidebarStyleEnum.COLUMN.getStyle());
    }

    /**
     * PDF 优质资源
     *
     * @return
     */
    private SideBarDTO pdfSideBar() {
        List<ConfigDTO> pdfList = configService.getConfigList(ConfigTypeEnum.PDF);
        List<SideBarItemDTO> items = new ArrayList<>(pdfList.size());
        pdfList.forEach(configDTO -> {
            SideBarItemDTO dto = new SideBarItemDTO();
            dto.setName(configDTO.getName());
            dto.setUrl(configDTO.getJumpUrl());
            dto.setImg(configDTO.getBannerUrl());
            RateVisitDTO visit;
            if (StringUtils.isNotBlank(configDTO.getExtra())) {
                visit = (JsonUtil.toObj(configDTO.getExtra(), RateVisitDTO.class));
            } else {
                visit = new RateVisitDTO();
            }
            visit.incrVisit();
            // 更新阅读计数
            configService.updateVisit(configDTO.getId(), JsonUtil.toStr(visit));
            dto.setVisit(visit);
            items.add(dto);
        });
        return new SideBarDTO().setTitle("优质PDF").setItems(items).setStyle(SidebarStyleEnum.PDF.getStyle());
    }

    /**
     * 订阅微信
     *
     * @return
     */
    private SideBarDTO recommendSideBar() {
        return new SideBarDTO().setTitle("加入\"社区技术交流群\"").setSubTitle("")
                .setIcon("https://tool.hhui.top/icon.svg")
                .setImg("//cdn.tobebetterjavaer.com/paicoding/a768cfc54f59d4a056f79d1c959dcae9.jpg")
                .setContent("群主微信：<br/> lml200701158（楼仔）<br/> qing_gee（沉默王二）")
                .setStyle(SidebarStyleEnum.RECOMMEND.getStyle());
    }

    /**
     * 订阅公众号
     *
     * @return
     */
    private SideBarDTO subscribeSideBar() {
        return new SideBarDTO().setTitle("订阅").setSubTitle("楼仔")
                .setImg("//cdn.tobebetterjavaer.com/paicoding/a768cfc54f59d4a056f79d1c959dcae9.jpg")
                .setContent("10本校招必刷八股文")
                .setStyle(SidebarStyleEnum.SUBSCRIBE.getStyle());
    }

    /**
     * 公告信息
     *
     * @return
     */
    private SideBarDTO noticeSideBar() {
        List<ConfigDTO> noticeList = configService.getConfigList(ConfigTypeEnum.NOTICE);
        List<SideBarItemDTO> items = new ArrayList<>(noticeList.size());
        noticeList.forEach(configDTO -> {
            List<Integer> configTags;
            if (StringUtils.isBlank(configDTO.getTags())) {
                configTags = Collections.emptyList();
            } else {
                configTags = Splitter.on(",").splitToStream(configDTO.getTags()).map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList());
            }
            items.add(new SideBarItemDTO()
                    .setName(configDTO.getName())
                    .setTitle(configDTO.getContent())
                    .setUrl(configDTO.getJumpUrl())
                    .setTime(configDTO.getCreateTime().getTime())
                    .setTags(configTags)
            );
        });
        return new SideBarDTO()
                .setTitle("关于技术派")
                // TODO 知识星球的
                .setImg("https://cdn.tobebetterjavaer.com/paicoding/main/paicoding-zsxq.jpg")
                .setUrl("https://paicoding.com/article/detail/169")
                .setItems(items)
                .setStyle(SidebarStyleEnum.NOTICE.getStyle());
    }

    /**
     * 热门文章
     *
     * @return
     */
    private SideBarDTO hotArticles() {
        PageListVo<SimpleArticleDTO> vo = articleReadService.queryHotArticlesForRecommend(PageParam.newPageInstance(1, 8));
        List<SideBarItemDTO> items = vo.getList().stream().map(s -> new SideBarItemDTO().setTitle(s.getTitle()).setUrl("/article/detail/" + s.getId()).setTime(s.getCreateTime().getTime())).collect(Collectors.toList());
        return new SideBarDTO().setTitle("热门文章").setItems(items).setStyle(SidebarStyleEnum.ARTICLES.getStyle());
    }
}
