package com.github.paicoding.forum.service.knowledge.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeDocDO;
import com.github.paicoding.forum.service.knowledge.repository.mapper.KnowledgeDocMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KnowledgeDocDao extends ServiceImpl<KnowledgeDocMapper, KnowledgeDocDO> {

    public List<KnowledgeDocDO> queryPublishedDocs(Long categoryId, String keyword, long pageNum, long pageSize) {
        long offset = Math.max(0, (pageNum - 1) * pageSize);
        return baseMapper.queryPublishedDocs(categoryId, keyword, offset, pageSize);
    }

    public Long countPublishedDocs(Long categoryId, String keyword) {
        return baseMapper.countPublishedDocs(categoryId, keyword);
    }

    public List<KnowledgeDocDO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return lambdaQuery().in(KnowledgeDocDO::getId, ids)
                .eq(KnowledgeDocDO::getDeleted, YesOrNoEnum.NO.getCode())
                .list();
    }
}
