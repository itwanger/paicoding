package com.github.paicoding.forum.service.sitemap.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * @author YiHui
 * @date 2023/2/13
 */
@Data
@JacksonXmlRootElement(localName = "urlset", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
public class SiteMapVo {
    @JacksonXmlProperty(localName = "url")
    private List<SiteUrlVo> url;
}
