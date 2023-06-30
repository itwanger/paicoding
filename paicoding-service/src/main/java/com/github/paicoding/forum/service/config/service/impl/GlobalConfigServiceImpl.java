package com.github.paicoding.forum.service.config.service.impl;

import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.config.GlobalConfigReq;
import com.github.paicoding.forum.api.model.vo.config.SearchGlobalConfigReq;
import com.github.paicoding.forum.api.model.vo.config.dto.GlobalConfigDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.config.converter.ConfigStructMapper;
import com.github.paicoding.forum.service.config.repository.dao.ConfigDao;
import com.github.paicoding.forum.service.config.repository.entity.GlobalConfigDO;
import com.github.paicoding.forum.service.config.repository.params.SearchGlobalConfigParams;
import com.github.paicoding.forum.service.config.service.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 6/30/23
 */
@Service
public class GlobalConfigServiceImpl implements GlobalConfigService {
    @Autowired
    private ConfigDao configDao;

    @Override
    public PageVo<GlobalConfigDTO> getList(SearchGlobalConfigReq req) {
        ConfigStructMapper mapper = ConfigStructMapper.INSTANCE;
        // 转换
        SearchGlobalConfigParams params = mapper.toSearchGlobalParams(req);
        // 查询
        List<GlobalConfigDO> list = configDao.listGlobalConfig(params);
        // 总数
        Long total = configDao.countGlobalConfig(params);

        return PageVo.build(mapper.toGlobalDTOS(list), params.getPageSize(), params.getPageNum(), total);
    }

    @Override
    public void save(GlobalConfigReq req) {
        GlobalConfigDO globalConfigDO = ConfigStructMapper.INSTANCE.toGlobalDO(req);
        // id 不为空
        if (NumUtil.nullOrZero(globalConfigDO.getId())) {
            configDao.save(globalConfigDO);
        } else {
            configDao.updateById(globalConfigDO);
        }
    }

    @Override
    public void delete(Long id) {
        GlobalConfigDO globalConfigDO = configDao.getGlobalConfigById(id);
        if (globalConfigDO != null) {
            configDao.delete(globalConfigDO);
        } else {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS,"记录不存在");
        }
    }
}
