package com.github.paicoding.forum.service.config.property;

import com.github.paicoding.forum.api.model.vo.config.NavbarConfigDTO;
import com.github.paicoding.forum.api.model.vo.config.NavbarItemDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 顶部导航动态配置。
 *
 * <p>未在 Admin 保存过时，沿用旧版派聪明/派聊聊配置，并默认追加派简历。</p>
 */
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "view.site")
public class NavbarConfigProperties {
    private String navbarItemsConfig;
    private String paiChatName;
    private String paiSmartName;
    private String paiSmartUrl;

    public NavbarConfigDTO resolve() {
        if (StringUtils.isNotBlank(navbarItemsConfig)) {
            try {
                NavbarConfigDTO config = JsonUtil.toObj(navbarItemsConfig, NavbarConfigDTO.class);
                if (config != null && config.getItems() != null) {
                    return config;
                }
            } catch (RuntimeException e) {
                log.error("顶部导航配置无法解析，使用安全默认值: {}", e.getMessage());
            }
        }
        return defaultConfig();
    }

    private NavbarConfigDTO defaultConfig() {
        NavbarConfigDTO config = new NavbarConfigDTO();
        config.setItems(Arrays.asList(
                item("pai-smart", StringUtils.defaultIfBlank(paiSmartName, "派聪明"),
                        StringUtils.defaultIfBlank(paiSmartUrl, "https://smart.paicoding.com/"), true),
                item("pai-chat", StringUtils.defaultIfBlank(paiChatName, "派聊聊"), "/chat", false),
                item("pai-resume", "派简历", "https://resume.paicoding.com/", true)
        ));
        return config;
    }

    private NavbarItemDTO item(String id, String name, String url, boolean openInNewWindow) {
        NavbarItemDTO item = new NavbarItemDTO();
        item.setId(id);
        item.setName(name);
        item.setUrl(url);
        item.setEnabled(true);
        item.setOpenInNewWindow(openInNewWindow);
        return item;
    }
}
