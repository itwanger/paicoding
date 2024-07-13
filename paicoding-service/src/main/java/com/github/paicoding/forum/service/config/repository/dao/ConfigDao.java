package com.github.paicoding.forum.service.config.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.ConfigTypeEnum;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.paicoding.forum.service.config.converter.ConfigConverter;
import com.github.paicoding.forum.service.config.converter.ConfigStructMapper;
import com.github.paicoding.forum.service.config.repository.entity.ConfigDO;
import com.github.paicoding.forum.service.config.repository.entity.GlobalConfigDO;
import com.github.paicoding.forum.service.config.repository.mapper.ConfigMapper;
import com.github.paicoding.forum.service.config.repository.mapper.GlobalConfigMapper;
import com.github.paicoding.forum.service.config.repository.params.SearchConfigParams;
import com.github.paicoding.forum.service.config.repository.params.SearchGlobalConfigParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Repository
public class ConfigDao extends ServiceImpl<ConfigMapper, ConfigDO> {
    @Resource
    private GlobalConfigMapper globalConfigMapper;

    /**
     * 根据类型获取配置列表（无需分页）
     *
     * @param type
     * @return
     */
    public List<ConfigDTO> listConfigByType(Integer type) {
        List<ConfigDO> configDOS = lambdaQuery()
                .eq(ConfigDO::getType, type)
                .eq(ConfigDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .eq(ConfigDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByAsc(ConfigDO::getRank)
                .list();
        return ConfigConverter.toDTOS(configDOS);
    }

    private LambdaQueryChainWrapper<ConfigDO> createConfigQuery(SearchConfigParams params) {
        return lambdaQuery()
                .eq(ConfigDO::getDeleted, YesOrNoEnum.NO.getCode())
                .like(StringUtils.isNotBlank(params.getName()), ConfigDO::getName, params.getName())
                .eq(params.getType() != null && params.getType() != -1, ConfigDO::getType, params.getType());
    }

    /**
     * 获取所有 Banner 列表（分页）
     *
     * @return
     */
    public List<ConfigDTO> listBanner(SearchConfigParams params) {
        List<ConfigDO> configDOS = createConfigQuery(params)
                .orderByDesc(ConfigDO::getUpdateTime)
                .orderByAsc(ConfigDO::getRank)
                .last(PageParam.getLimitSql(
                        PageParam.newPageInstance(params.getPageNum(), params.getPageSize())))
                .list();
        return ConfigStructMapper.INSTANCE.toDTOS(configDOS);
    }

    /**
     * 获取所有 Banner 总数（分页）
     *
     * @return
     */
    public Long countConfig(SearchConfigParams params) {
        return createConfigQuery(params)
                .count();
    }

    /**
     * 获取所有公告列表（分页）
     *
     * @return
     */
    public List<ConfigDTO> listNotice(PageParam pageParam) {
        List<ConfigDO> configDOS = lambdaQuery()
                .eq(ConfigDO::getType, ConfigTypeEnum.NOTICE.getCode())
                .eq(ConfigDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByDesc(ConfigDO::getCreateTime)
                .last(PageParam.getLimitSql(pageParam))
                .list();
        return ConfigConverter.toDTOS(configDOS);
    }

    /**
     * 获取所有公告总数（分页）
     *
     * @return
     */
    public Integer countNotice() {
        return lambdaQuery()
                .eq(ConfigDO::getType, ConfigTypeEnum.NOTICE.getCode())
                .eq(ConfigDO::getDeleted, YesOrNoEnum.NO.getCode())
                .count()
                .intValue();
    }

    /**
     * 更新阅读相关计数
     */
    public void updatePdfConfigVisitNum(long configId, String extra) {
        lambdaUpdate().set(ConfigDO::getExtra, extra)
                .eq(ConfigDO::getId, configId)
                .update();
    }

    public List<GlobalConfigDO> listGlobalConfig(SearchGlobalConfigParams params) {
        LambdaQueryWrapper<GlobalConfigDO> query = buildQuery(params);
        query.select(GlobalConfigDO::getId,
                GlobalConfigDO::getKey,
                GlobalConfigDO::getValue,
                GlobalConfigDO::getComment);
        return globalConfigMapper.selectList(query);
    }

    public Long countGlobalConfig(SearchGlobalConfigParams params) {
        return globalConfigMapper.selectCount(buildQuery(params));
    }

    private LambdaQueryWrapper<GlobalConfigDO> buildQuery(SearchGlobalConfigParams params) {
        LambdaQueryWrapper<GlobalConfigDO> query = Wrappers.lambdaQuery();

        query.and(!StringUtils.isEmpty(params.getKey()),
                        k -> k.like(GlobalConfigDO::getKey, params.getKey()))
                .and(!StringUtils.isEmpty(params.getValue()),
                        v -> v.like(GlobalConfigDO::getValue, params.getValue()))
                .and(!StringUtils.isEmpty(params.getComment()),
                        c -> c.like(GlobalConfigDO::getComment, params.getComment()))
                .eq(GlobalConfigDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByDesc(GlobalConfigDO::getUpdateTime);
        return query;
    }

    public void save(GlobalConfigDO globalConfigDO) {
        globalConfigMapper.insert(globalConfigDO);
    }

    public void updateById(GlobalConfigDO globalConfigDO) {
        globalConfigDO.setUpdateTime(new Date());
        globalConfigMapper.updateById(globalConfigDO);
    }

    /**
     * 根据id查询全局配置
     *
     * @param id
     * @return
     */
    public GlobalConfigDO getGlobalConfigById(Long id) {
        // 查询的时候 deleted 为 0
        LambdaQueryWrapper<GlobalConfigDO> query = Wrappers.lambdaQuery();
        query.select(GlobalConfigDO::getId, GlobalConfigDO::getKey, GlobalConfigDO::getValue, GlobalConfigDO::getComment)
                .eq(GlobalConfigDO::getId, id)
                .eq(GlobalConfigDO::getDeleted, YesOrNoEnum.NO.getCode());
        return globalConfigMapper.selectOne(query);
    }

    /**
     * 根据key查询全局配置
     *
     * @param key
     * @return
     */
    public GlobalConfigDO getGlobalConfigByKey(String key) {
        // 查询的时候 deleted 为 0
        LambdaQueryWrapper<GlobalConfigDO> query = Wrappers.lambdaQuery();
        query.select(GlobalConfigDO::getId, GlobalConfigDO::getKey, GlobalConfigDO::getValue, GlobalConfigDO::getComment)
                .eq(GlobalConfigDO::getKey, key)
                .eq(GlobalConfigDO::getDeleted, YesOrNoEnum.NO.getCode());
        return globalConfigMapper.selectOne(query);
    }

    public void delete(GlobalConfigDO globalConfigDO) {
        globalConfigDO.setDeleted(YesOrNoEnum.YES.getCode());
        globalConfigMapper.updateById(globalConfigDO);
    }
}
