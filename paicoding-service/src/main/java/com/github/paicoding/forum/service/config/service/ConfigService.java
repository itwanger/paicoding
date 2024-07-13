package com.github.paicoding.forum.service.config.service;

import com.github.paicoding.forum.api.model.enums.ConfigTypeEnum;
import com.github.paicoding.forum.api.model.vo.banner.dto.ConfigDTO;

import java.util.List;

/**
 * Banner前台接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface ConfigService {

    /**
     * 获取 Banner 列表
     *
     * @param configTypeEnum
     * @return
     */
    List<ConfigDTO> getConfigList(ConfigTypeEnum configTypeEnum);

    /**
     * 阅读次数+1
     *
     * @param configId
     * @param extra
     */
    void updateVisit(long configId, String extra);
}
