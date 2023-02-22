package com.github.paicoding.forum.service.sitemap.service;

import com.github.paicoding.forum.service.sitemap.model.SiteMapVo;

/**
 * @author YiHui
 * @date 2023/2/13
 */
public interface SitemapService {

    /**
     * 查询站点地图
     *
     * @return
     */
    SiteMapVo getSiteMap();

    /**
     * 刷新站点地图
     */
    void refreshSitemap();

    /**
     * 新增文章并上线
     *
     * @param articleId
     */
    void addArticle(Long articleId);

    /**
     * 删除文章、or文章下线
     *
     * @param articleId
     */
    void rmArticle(Long articleId);
}
