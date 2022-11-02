package com.github.liuyueyi.forum.service.config.converter;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.banner.ConfigReq;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.liuyueyi.forum.service.config.repository.entity.ConfigDO;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Banner转换
 *
 * @author louzai
 * @date 2022-09-20
 */
public class ConfigConverter {

    public static List<ConfigDTO> toDTOS(List<ConfigDO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        return records.stream().map(ConfigConverter::toDTO).collect(Collectors.toList());
    }

    public static ConfigDTO toDTO(ConfigDO configDO) {
        if (configDO == null) {
            return null;
        }
        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setType(configDO.getType());
        configDTO.setName(configDO.getName());
        configDTO.setBannerUrl(configDO.getBannerUrl());
        configDTO.setJumpUrl(configDO.getJumpUrl());
        configDTO.setContent(configDO.getContent());
        configDTO.setRank(configDO.getRank());
        configDTO.setStatus(configDO.getStatus());
        configDTO.setId(configDO.getId());
        configDTO.setTags(configDO.getTags());
        configDTO.setCreateTime(configDO.getCreateTime());
        configDTO.setUpdateTime(configDO.getUpdateTime());
        return configDTO;
    }

    public static ConfigDO toDO(ConfigReq configReq) {
        if (configReq == null) {
            return null;
        }
        ConfigDO configDO = new ConfigDO();
        configDO.setType(configReq.getType());
        configDO.setName(configReq.getName());
        configDO.setBannerUrl(configReq.getBannerUrl());
        configDO.setJumpUrl(configReq.getJumpUrl());
        configDO.setContent(configReq.getContent());
        configDO.setRank(configReq.getRank());
        configDO.setStatus(PushStatusEnum.OFFLINE.getCode());
        configDO.setTags(configReq.getTags());
        return configDO;
    }
}
