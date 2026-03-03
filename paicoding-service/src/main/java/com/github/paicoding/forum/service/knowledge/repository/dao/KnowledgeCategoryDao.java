package com.github.paicoding.forum.service.knowledge.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeCategoryDO;
import com.github.paicoding.forum.service.knowledge.repository.mapper.KnowledgeCategoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KnowledgeCategoryDao extends ServiceImpl<KnowledgeCategoryMapper, KnowledgeCategoryDO> {

    public List<KnowledgeCategoryDO> listByStatus(Integer status) {
        LambdaQueryWrapper<KnowledgeCategoryDO> query = Wrappers.lambdaQuery();
        query.eq(KnowledgeCategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(status != null, KnowledgeCategoryDO::getStatus, status)
                .orderByAsc(KnowledgeCategoryDO::getLevel)
                .orderByDesc(KnowledgeCategoryDO::getRank)
                .orderByAsc(KnowledgeCategoryDO::getId);
        return list(query);
    }

    public List<KnowledgeCategoryDO> listByParentId(Long parentId) {
        return lambdaQuery()
                .eq(KnowledgeCategoryDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(KnowledgeCategoryDO::getParentId, parentId)
                .orderByDesc(KnowledgeCategoryDO::getRank)
                .list();
    }
}
