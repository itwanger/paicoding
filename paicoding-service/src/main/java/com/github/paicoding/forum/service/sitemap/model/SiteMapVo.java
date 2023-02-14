package com.github.paicoding.forum.service.sitemap.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YiHui
 * @date 2023/2/13
 */
@Data
@JacksonXmlRootElement(localName = "urlset", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
public class SiteMapVo {
    /**
     * 将列表数据转为XML节点， useWrapping = false 表示不要外围标签名
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "url")
    private List<SiteUrlVo> url;

    public SiteMapVo() {
        url = new ArrayList<>();
    }

    public void addUrl(SiteUrlVo xmlUrl) {
        url.add(xmlUrl);
    }
}
