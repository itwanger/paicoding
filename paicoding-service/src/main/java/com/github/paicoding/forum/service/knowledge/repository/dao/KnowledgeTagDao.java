package com.github.paicoding.forum.service.knowledge.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeTagDO;
import com.github.paicoding.forum.service.knowledge.repository.mapper.KnowledgeTagMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class KnowledgeTagDao extends ServiceImpl<KnowledgeTagMapper, KnowledgeTagDO> {

    public List<KnowledgeTagDO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(KnowledgeTagDO::getId, ids)
                .eq(KnowledgeTagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .list();
    }

    public List<KnowledgeTagDO> listAllOnline() {
        return lambdaQuery()
                .eq(KnowledgeTagDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(KnowledgeTagDO::getStatus, 1)
                .orderByDesc(KnowledgeTagDO::getId)
                .list();
    }
}
