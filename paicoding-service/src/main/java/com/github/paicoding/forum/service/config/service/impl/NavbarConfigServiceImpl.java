package com.github.paicoding.forum.service.config.service.impl;

import com.github.paicoding.forum.api.model.event.ConfigRefreshEvent;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.config.NavbarConfigDTO;
import com.github.paicoding.forum.api.model.vo.config.NavbarItemDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.config.property.NavbarConfigProperties;
import com.github.paicoding.forum.service.config.repository.dao.ConfigDao;
import com.github.paicoding.forum.service.config.repository.entity.GlobalConfigDO;
import com.github.paicoding.forum.service.config.service.NavbarConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NavbarConfigServiceImpl implements NavbarConfigService {
    public static final String CONFIG_KEY = "view.site.navbar-items-config";
    private static final int MAX_ITEMS = 8;

    @Autowired
    private NavbarConfigProperties properties;

    @Autowired
    private ConfigDao configDao;

    @Override
    public NavbarConfigDTO getConfig() {
        return properties.resolve();
    }

    @Override
    public List<NavbarItemDTO> getEnabledItems() {
        return getConfig().getItems().stream()
                .filter(item -> !Boolean.FALSE.equals(item.getEnabled()))
                .filter(this::isSafeToRender)
                .collect(Collectors.toList());
    }

    @Override
    public void save(NavbarConfigDTO config) {
        NavbarConfigDTO normalized = normalizeAndValidate(config);
        String value = JsonUtil.toStr(normalized);
        GlobalConfigDO stored = configDao.getGlobalConfigByKey(CONFIG_KEY);
        if (stored == null) {
            stored = new GlobalConfigDO();
            stored.setKey(CONFIG_KEY);
            stored.setValue(value);
            stored.setComment("顶部导航配置：首页和教程以外的入口");
            configDao.save(stored);
        } else {
            stored.setValue(value);
            stored.setComment("顶部导航配置：首页和教程以外的入口");
            configDao.updateById(stored);
        }
        SpringUtil.publishEvent(new ConfigRefreshEvent(this, CONFIG_KEY, value));
    }

    NavbarConfigDTO normalizeAndValidate(NavbarConfigDTO config) {
        if (config == null || config.getItems() == null) {
            throw illegal("导航配置不能为空");
        }
        if (config.getItems().size() > MAX_ITEMS) {
            throw illegal("可配置入口最多 " + MAX_ITEMS + " 个");
        }

        List<NavbarItemDTO> items = new ArrayList<>();
        Set<String> names = new HashSet<>();
        Set<String> ids = new HashSet<>();
        for (NavbarItemDTO source : config.getItems()) {
            if (source == null) {
                throw illegal("导航项不能为空");
            }
            NavbarItemDTO item = new NavbarItemDTO();
            item.setId(StringUtils.defaultIfBlank(StringUtils.trim(source.getId()), "nav-" + UUID.randomUUID()));
            item.setName(StringUtils.trim(source.getName()));
            item.setUrl(StringUtils.trim(source.getUrl()));
            item.setEnabled(!Boolean.FALSE.equals(source.getEnabled()));
            item.setOpenInNewWindow(Boolean.TRUE.equals(source.getOpenInNewWindow()));

            if (StringUtils.isBlank(item.getName()) || item.getName().length() > 12) {
                throw illegal("入口名称需为 1-12 个字符");
            }
            if ("首页".equals(item.getName()) || "教程".equals(item.getName())) {
                throw illegal("首页和教程是固定入口，无需重复添加");
            }
            if (!names.add(item.getName().toLowerCase(Locale.ROOT))) {
                throw illegal("入口名称不能重复: " + item.getName());
            }
            if (!ids.add(item.getId())) {
                throw illegal("导航项标识不能重复");
            }
            validateUrl(item.getUrl());
            items.add(item);
        }

        NavbarConfigDTO normalized = new NavbarConfigDTO();
        normalized.setItems(items);
        return normalized;
    }

    private void validateUrl(String url) {
        if (StringUtils.isBlank(url) || url.length() > 500) {
            throw illegal("入口链接不能为空且不能超过 500 个字符");
        }
        if (url.startsWith("/") && !url.startsWith("//")) {
            if ("/".equals(url) || "/column".equals(url) || url.startsWith("/column/")) {
                throw illegal("首页和教程是固定入口，无需重复添加");
            }
            return;
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw illegal("入口链接只支持站内 /path 或 http(s) 地址");
            }
        } catch (URISyntaxException e) {
            throw illegal("入口链接格式不正确");
        }
    }

    private boolean isSafeToRender(NavbarItemDTO item) {
        if (item == null || StringUtils.isBlank(item.getName()) || item.getName().length() > 12) {
            return false;
        }
        try {
            validateUrl(item.getUrl());
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private RuntimeException illegal(String message) {
        return ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, message);
    }
}
