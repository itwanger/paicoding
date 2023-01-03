package com.github.liuyueyi.forum.service.config.service.impl;

import com.github.liueyueyi.forum.api.model.enums.ConfigTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.liuyueyi.forum.service.config.repository.dao.ConfigDao;
import com.github.liuyueyi.forum.service.config.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Banner前台接口
 *
 * @author louzai
 * @date 2022-07-24
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigDao configDao;

    @Override
    public List<ConfigDTO> getConfigList(ConfigTypeEnum configTypeEnum) {
        return configDao.listConfigByType(configTypeEnum.getCode());
    }

    @Override
    public void updateVisit(long configId, String extra) {
        configDao.updatePdfConfigVisitNum(configId, extra);
    }
}
