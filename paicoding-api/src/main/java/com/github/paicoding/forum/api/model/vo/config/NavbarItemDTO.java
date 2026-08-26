package com.github.paicoding.forum.api.model.vo.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 顶部导航中的一个可配置入口。
 */
@Data
public class NavbarItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String url;
    private Boolean enabled;
    private Boolean openInNewWindow;

    /**
     * 站内地址按首段路径匹配当前栏目，用于复用现有导航高亮样式。
     */
    @JsonIgnore
    public String getActiveDomain() {
        if (url == null || !url.startsWith("/") || "/".equals(url)) {
            return "";
        }
        String path = url.substring(1);
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex >= 0) {
            path = path.substring(0, fragmentIndex);
        }
        int splitIndex = path.indexOf('/');
        return splitIndex < 0 ? path : path.substring(0, splitIndex);
    }
}
