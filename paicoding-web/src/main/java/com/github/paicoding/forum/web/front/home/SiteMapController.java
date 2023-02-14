package com.github.paicoding.forum.web.front.home;

import com.github.paicoding.forum.service.sitemap.model.SiteMapVo;
import com.github.paicoding.forum.service.sitemap.service.SitemapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 生成 sitemap.xml
 *
 * @author YiHui
 * @date 2023/2/13
 */
@RestController
public class SiteMapController {
    @Resource
    private SitemapService sitemapService;

    @RequestMapping(path = "/sitemap",
            produces = "application/xml;charset=utf-8")
    public SiteMapVo sitemap() {
        return sitemapService.getSiteMap();
    }

    @GetMapping(path = "/sitemap/refresh")
    public Boolean refresh() {
        sitemapService.refreshSitemap();
        return true;
    }
}
