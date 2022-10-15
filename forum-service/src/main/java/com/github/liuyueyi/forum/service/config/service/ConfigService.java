package com.github.liuyueyi.forum.service.config.service;

import com.github.liueyueyi.forum.api.model.enums.ConfigTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.banner.dto.ConfigDTO;

import java.util.List;

/**
 * Banner前台接口
 *
 * @author louzai
 * @date 2022-07-24
 */
public interface ConfigService {

    /**
     * 获取 Banner 列表
     *
     * @param configTypeEnum
     * @return
     */
    List<ConfigDTO> getConfigList(ConfigTypeEnum configTypeEnum);
}
