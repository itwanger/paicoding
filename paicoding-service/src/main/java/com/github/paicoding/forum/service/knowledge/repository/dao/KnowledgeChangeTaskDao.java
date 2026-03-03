package com.github.paicoding.forum.service.knowledge.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeChangeTaskDO;
import com.github.paicoding.forum.service.knowledge.repository.mapper.KnowledgeChangeTaskMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KnowledgeChangeTaskDao extends ServiceImpl<KnowledgeChangeTaskMapper, KnowledgeChangeTaskDO> {

    public List<KnowledgeChangeTaskDO> listByStatus(String status, long offset, long pageSize) {
        return lambdaQuery()
                .eq(status != null && !status.isEmpty(), KnowledgeChangeTaskDO::getStatus, status)
                .orderByDesc(KnowledgeChangeTaskDO::getCreateTime)
                .last("limit " + offset + "," + pageSize)
                .list();
    }

    public Long countByStatus(String status) {
        return lambdaQuery()
                .eq(status != null && !status.isEmpty(), KnowledgeChangeTaskDO::getStatus, status)
                .count();
    }
}
