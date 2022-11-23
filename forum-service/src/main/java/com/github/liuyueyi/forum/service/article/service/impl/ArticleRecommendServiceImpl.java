package com.github.liuyueyi.forum.service.article.service.impl;

import com.github.liueyueyi.forum.api.model.enums.ConfigTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.SidebarStyleEnum;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarItemDto;
import com.github.liuyueyi.forum.service.article.repository.dao.ArticleDao;
import com.github.liuyueyi.forum.service.article.repository.dao.ArticleTagDao;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleTagDO;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.service.article.service.ArticleRecommendService;
import com.github.liuyueyi.forum.service.sidebar.service.SidebarService;
import com.github.liuyueyi.forum.service.sidebar.service.SidebarServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2022/9/26
 */
@Service
public class ArticleRecommendServiceImpl implements ArticleRecommendService {
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private ArticleTagDao articleTagDao;
    @Autowired
    private ArticleReadService articleReadService;
    @Autowired
    private SidebarService sidebarService;

    @Override
    public List<SideBarDTO> recommend(ArticleDTO articleDO) {
        // 推荐文章
        SideBarDTO recommend = recommendByAuthor(articleDO.getAuthor(), articleDO.getArticleId(), PageParam.DEFAULT_PAGE_SIZE);

        // PDF
        SideBarDTO pdf = sidebarService.pdfSideBar();

        return Arrays.asList(pdf, recommend);
    }

    /**
     * 作者的文章列表推荐
     *
     * @param authorId
     * @param size
     * @return
     */
    public SideBarDTO recommendByAuthor(Long authorId, Long articleId, long size) {
        List<SimpleArticleDTO> list = articleDao.listAuthorHotArticles(authorId, PageParam.newPageInstance(PageParam.DEFAULT_PAGE_NUM, size));
        List<SideBarItemDto> items = list.stream().filter(s -> !s.getId().equals(articleId))
                .map(s -> new SideBarItemDto()
                        .setTitle(s.getTitle()).setUrl("/article/detail/" + s.getId())
                        .setTime(s.getCreateTime().getTime()))
                .collect(Collectors.toList());
        return new SideBarDTO().setTitle("相关文章").setItems(items).setStyle(SidebarStyleEnum.ARTICLES.getStyle());
    }

    /**
     * 扫描关注
     *
     * @return
     */
    public SideBarDTO joinUs() {
        return new SideBarDTO().setTitle("扫码进群").setSubTitle("加入交流社区")
                .setImg("https://spring.hhui.top/spring-blog/imgs/info/wx.jpg")
                .setContent("联系信息:<br/> yihuihuiyi@gmail.com")
                .setStyle(SidebarStyleEnum.RECOMMEND.getStyle());
    }


    /**
     * 查询文章关联推荐列表
     *
     * @param articleId
     * @param page
     * @return
     */
    @Override
    public PageListVo<ArticleDTO> relatedRecommend(Long articleId, PageParam page) {
        ArticleDO article = articleDao.getById(articleId);
        if (article == null) {
            return PageListVo.emptyVo();
        }
        List<Long> tagIds = articleTagDao.listArticleTags(articleId).stream()
                .map(ArticleTagDO::getTagId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tagIds)) {
            return PageListVo.emptyVo();
        }

        List<ArticleDO> recommendArticles = articleDao.listRelatedArticlesOrderByReadCount(article.getCategoryId(), tagIds, page);
        return articleReadService.buildArticleListVo(recommendArticles, page.getPageSize());
    }
}
