package com.github.paicoding.forum.service.config.service;

import com.github.paicoding.forum.api.model.vo.config.NavbarConfigDTO;
import com.github.paicoding.forum.api.model.vo.config.NavbarItemDTO;

import java.util.List;

public interface NavbarConfigService {
    NavbarConfigDTO getConfig();

    List<NavbarItemDTO> getEnabledItems();

    void save(NavbarConfigDTO config);
}
