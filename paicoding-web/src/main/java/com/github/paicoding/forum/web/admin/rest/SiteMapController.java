package com.github.paicoding.forum.web.admin.rest;

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
@RequestMapping(path = {"/api/admin/login", "/admin/login"})
public class SiteMapController {
    @Resource
    private SitemapService sitemapService;

    @GetMapping(path = "sitemap")
    public String sitemap() {
        return "";
    }

}
