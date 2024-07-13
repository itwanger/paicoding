package com.github.paicoding.forum.service.config.service.impl;

import com.github.paicoding.forum.api.model.enums.ConfigTypeEnum;
import com.github.paicoding.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.paicoding.forum.service.config.repository.dao.ConfigDao;
import com.github.paicoding.forum.service.config.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Banner前台接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigDao configDao;

    @Override
    public List<ConfigDTO> getConfigList(ConfigTypeEnum configTypeEnum) {
        return configDao.listConfigByType(configTypeEnum.getCode());
    }

    /**
     * 配置发生变更之后，失效本地缓存，这里主要是配合 SidebarServiceImpl 中的缓存使用
     *
     * @param configId
     * @param extra
     */
    @Override
    public void updateVisit(long configId, String extra) {
        configDao.updatePdfConfigVisitNum(configId, extra);
    }
}
