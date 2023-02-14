package com.github.paicoding.forum.service.sitemap.service;

import com.github.paicoding.forum.service.sitemap.model.SiteMapVo;

/**
 * @author YiHui
 * @date 2023/2/13
 */
public interface SitemapService {

    SiteMapVo getSiteMap();

    void refreshSitemap();
}
