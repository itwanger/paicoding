package com.github.paicoding.forum.service.knowledge.service;

import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeTagDTO;
import com.github.paicoding.forum.service.knowledge.repository.dao.KnowledgeTagDao;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeTagDO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeTagService {

    private final KnowledgeTagDao tagDao;

    public List<KnowledgeTagDTO> listAllOnlineTags() {
        return tagDao.listAllOnline().stream().map(this::convert).collect(Collectors.toList());
    }

    public Map<Long, KnowledgeTagDTO> mapByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return tagDao.listByIds(ids).stream()
                .map(this::convert)
                .collect(Collectors.toMap(KnowledgeTagDTO::getTagId, v -> v));
    }

    public Set<Long> ensureTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<KnowledgeTagDO> tags = tagDao.listByIds(tagIds);
        return tags.stream().map(KnowledgeTagDO::getId).collect(Collectors.toSet());
    }

    private KnowledgeTagDTO convert(KnowledgeTagDO tag) {
        KnowledgeTagDTO dto = new KnowledgeTagDTO();
        BeanUtils.copyProperties(tag, dto);
        dto.setTagId(tag.getId());
        return dto;
    }
}
