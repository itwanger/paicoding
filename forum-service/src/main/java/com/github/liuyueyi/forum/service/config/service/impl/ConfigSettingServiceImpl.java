package com.github.liuyueyi.forum.service.config.service.impl;

import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.banner.ConfigReq;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.config.converter.ConfigConverter;
import com.github.liuyueyi.forum.service.config.repository.dao.ConfigDao;
import com.github.liuyueyi.forum.service.config.repository.entity.ConfigDO;
import com.github.liuyueyi.forum.service.config.service.ConfigSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Banner后台接口
 *
 * @author louzai
 * @date 2022-07-24
 */
@Service
public class ConfigSettingServiceImpl implements ConfigSettingService {

    @Autowired
    private ConfigDao configDao;

    @Override
    public void saveConfig(ConfigReq configReq) {
        ConfigDO configDO = ConfigConverter.ToDO(configReq);
        if (NumUtil.nullOrZero(configReq.getConfigId())) {
            configDao.save(configDO);
        } else {
            configDO.setId(configReq.getConfigId());
            configDao.updateById(configDO);
        }
    }

    @Override
    public void deleteConfig(Integer configId) {
        ConfigDO configDO = configDao.getById(configId);
        if (configDO != null){
            configDO.setDeleted(YesOrNoEnum.YES.getCode());
            configDao.updateById(configDO);
        }
    }

    @Override
    public void operateConfig(Integer configId, Integer operateType) {
        ConfigDO configDO = configDao.getById(configId);
        if (configDO != null){
            configDO.setStatus(operateType);
            configDao.updateById(configDO);
        }
    }

    @Override
    public PageVo<ConfigDTO> getBannerList(PageParam pageParam) {
        List<ConfigDTO> configDTOS = configDao.listBanner(pageParam);
        Integer totalCount = configDao.countBanner();
        return PageVo.build(configDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }

    @Override
    public PageVo<ConfigDTO> getNoticeList(PageParam pageParam) {
        List<ConfigDTO> configDTOS = configDao.listNotice(pageParam);
        Integer totalCount = configDao.countNotice();
        return PageVo.build(configDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }
}
