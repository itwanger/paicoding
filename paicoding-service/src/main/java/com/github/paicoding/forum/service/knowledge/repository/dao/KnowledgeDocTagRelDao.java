package com.github.paicoding.forum.service.knowledge.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeDocTagRelDO;
import com.github.paicoding.forum.service.knowledge.repository.mapper.KnowledgeDocTagRelMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class KnowledgeDocTagRelDao extends ServiceImpl<KnowledgeDocTagRelMapper, KnowledgeDocTagRelDO> {

    public List<KnowledgeDocTagRelDO> listByDocId(Long docId) {
        if (docId == null) {
            return Collections.emptyList();
        }
        return lambdaQuery().eq(KnowledgeDocTagRelDO::getDocId, docId).list();
    }

    public List<KnowledgeDocTagRelDO> listByTagId(Long tagId) {
        if (tagId == null) {
            return Collections.emptyList();
        }
        return lambdaQuery().eq(KnowledgeDocTagRelDO::getTagId, tagId).list();
    }

    public void removeByDocId(Long docId) {
        remove(new LambdaQueryWrapper<KnowledgeDocTagRelDO>().eq(KnowledgeDocTagRelDO::getDocId, docId));
    }
}
