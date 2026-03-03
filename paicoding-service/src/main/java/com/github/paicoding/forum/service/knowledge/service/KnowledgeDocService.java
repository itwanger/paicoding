package com.github.paicoding.forum.service.knowledge.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeDocReq;
import com.github.paicoding.forum.api.model.vo.knowledge.SearchKnowledgeDocReq;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeDocDTO;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeTagDTO;
import com.github.paicoding.forum.service.knowledge.repository.dao.KnowledgeCategoryDao;
import com.github.paicoding.forum.service.knowledge.repository.dao.KnowledgeDocDao;
import com.github.paicoding.forum.service.knowledge.repository.dao.KnowledgeDocTagRelDao;
import com.github.paicoding.forum.service.knowledge.repository.dao.KnowledgeTagDao;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeCategoryDO;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeDocDO;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeDocTagRelDO;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeTagDO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeDocService {

    private final KnowledgeDocDao docDao;
    private final KnowledgeDocTagRelDao docTagRelDao;
    private final KnowledgeTagDao tagDao;
    private final KnowledgeCategoryDao categoryDao;

    public PageVo<KnowledgeDocDTO> queryPublishedDocs(Long categoryId, Long tagId, String keyword, Long pageNum, Long pageSize) {
        long current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10 : pageSize;

        List<KnowledgeDocDO> docs = docDao.queryPublishedDocs(categoryId, keyword, current, size);
        if (tagId != null) {
            Set<Long> docIds = docTagRelDao.listByTagId(tagId).stream().map(KnowledgeDocTagRelDO::getDocId).collect(Collectors.toSet());
            docs = docs.stream().filter(doc -> docIds.contains(doc.getId())).collect(Collectors.toList());
        }
        Long total = docDao.countPublishedDocs(categoryId, keyword);

        List<KnowledgeDocDTO> result = attachCategoryAndTags(docs, false);
        return PageVo.build(result, size, current, total == null ? 0 : total);
    }

    public KnowledgeDocDTO queryPublishedDocDetail(Long docId) {
        KnowledgeDocDO doc = docDao.lambdaQuery()
                .eq(KnowledgeDocDO::getId, docId)
                .eq(KnowledgeDocDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(KnowledgeDocDO::getStatus, 1)
                .one();
        if (doc == null) {
            return null;
        }
        return attachCategoryAndTags(Collections.singletonList(doc), true).stream().findFirst().orElse(null);
    }

    public PageVo<KnowledgeDocDTO> queryAdminDocs(SearchKnowledgeDocReq req) {
        long current = req.getPageNumber() == null || req.getPageNumber() < 1 ? 1 : req.getPageNumber();
        long size = req.getPageSize() == null || req.getPageSize() < 1 ? 20 : req.getPageSize();
        long offset = (current - 1) * size;

        List<KnowledgeDocDO> docs = docDao.lambdaQuery()
                .eq(KnowledgeDocDO::getDeleted, 0)
                .eq(req.getCategoryId() != null, KnowledgeDocDO::getCategoryId, req.getCategoryId())
                .eq(req.getStatus() != null, KnowledgeDocDO::getStatus, req.getStatus())
                .like(StringUtils.isNotBlank(req.getKeyword()), KnowledgeDocDO::getTitle, req.getKeyword())
                .orderByDesc(KnowledgeDocDO::getUpdateTime)
                .last("limit " + offset + "," + size)
                .list();

        if (req.getTagId() != null) {
            Set<Long> docIds = docTagRelDao.listByTagId(req.getTagId()).stream().map(KnowledgeDocTagRelDO::getDocId).collect(Collectors.toSet());
            docs = docs.stream().filter(doc -> docIds.contains(doc.getId())).collect(Collectors.toList());
        }

        long total = docDao.lambdaQuery()
                .eq(KnowledgeDocDO::getDeleted, 0)
                .eq(req.getCategoryId() != null, KnowledgeDocDO::getCategoryId, req.getCategoryId())
                .eq(req.getStatus() != null, KnowledgeDocDO::getStatus, req.getStatus())
                .like(StringUtils.isNotBlank(req.getKeyword()), KnowledgeDocDO::getTitle, req.getKeyword())
                .count();

        List<KnowledgeDocDTO> list = attachCategoryAndTags(docs, false);
        return PageVo.build(list, size, current, total);
    }

    public KnowledgeDocDTO queryAdminDocDetail(Long docId) {
        KnowledgeDocDO doc = docDao.lambdaQuery()
                .eq(KnowledgeDocDO::getId, docId)
                .eq(KnowledgeDocDO::getDeleted, 0)
                .one();
        if (doc == null) {
            return null;
        }
        return attachCategoryAndTags(Collections.singletonList(doc), true).stream().findFirst().orElse(null);
    }

    public Long saveDoc(KnowledgeDocReq req, Long userId) {
        KnowledgeDocDO doc;
        boolean isNew = req.getDocId() == null || req.getDocId() <= 0;
        if (isNew) {
            doc = new KnowledgeDocDO();
            doc.setDeleted(0);
            doc.setCreateUserId(userId);
        } else {
            doc = docDao.getById(req.getDocId());
            if (doc == null) {
                return null;
            }
        }
        doc.setCategoryId(req.getCategoryId());
        doc.setTitle(req.getTitle());
        doc.setDescription(req.getDescription());
        doc.setContentMd(req.getContentMd());
        doc.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        doc.setUpdateUserId(userId);
        if (doc.getStatus() != null && doc.getStatus() == 1) {
            doc.setPublishTime(new Date());
        }
        docDao.saveOrUpdate(doc);

        if (req.getTagIds() != null) {
            docTagRelDao.removeByDocId(doc.getId());
            List<KnowledgeDocTagRelDO> rels = new ArrayList<>();
            for (Long tagId : req.getTagIds()) {
                KnowledgeDocTagRelDO rel = new KnowledgeDocTagRelDO();
                rel.setDocId(doc.getId());
                rel.setTagId(tagId);
                rels.add(rel);
            }
            if (!rels.isEmpty()) {
                docTagRelDao.saveBatch(rels);
            }
        }
        return doc.getId();
    }

    public void deleteDoc(Long docId) {
        docDao.lambdaUpdate().eq(KnowledgeDocDO::getId, docId).set(KnowledgeDocDO::getDeleted, 1).update();
        docTagRelDao.removeByDocId(docId);
    }

    public void operateDoc(Long docId, Integer status) {
        docDao.lambdaUpdate().eq(KnowledgeDocDO::getId, docId).set(KnowledgeDocDO::getStatus, status).update();
    }

    public List<KnowledgeDocDTO> queryDocByIds(List<Long> docIds, boolean withContent) {
        List<KnowledgeDocDO> docs = docDao.listByIds(docIds);
        return attachCategoryAndTags(docs, withContent);
    }

    private List<KnowledgeDocDTO> attachCategoryAndTags(List<KnowledgeDocDO> docs, boolean withContent) {
        if (docs == null || docs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> categoryIds = docs.stream().map(KnowledgeDocDO::getCategoryId).distinct().collect(Collectors.toList());
        List<KnowledgeCategoryDO> categories = categoryDao.listByIds(categoryIds);
        Map<Long, String> categoryNameMap = categories.stream().collect(Collectors.toMap(KnowledgeCategoryDO::getId, KnowledgeCategoryDO::getCategoryName));

        List<Long> docIds = docs.stream().map(KnowledgeDocDO::getId).collect(Collectors.toList());
        List<KnowledgeDocTagRelDO> rels = docTagRelDao.lambdaQuery().in(KnowledgeDocTagRelDO::getDocId, docIds).list();
        Map<Long, List<Long>> docTagMap = rels.stream().collect(Collectors.groupingBy(KnowledgeDocTagRelDO::getDocId,
                Collectors.mapping(KnowledgeDocTagRelDO::getTagId, Collectors.toList())));

        List<Long> tagIds = rels.stream().map(KnowledgeDocTagRelDO::getTagId).distinct().collect(Collectors.toList());
        Map<Long, KnowledgeTagDO> tagMap = tagDao.listByIds(tagIds).stream().collect(Collectors.toMap(KnowledgeTagDO::getId, t -> t));

        List<KnowledgeDocDTO> result = new ArrayList<>();
        for (KnowledgeDocDO doc : docs) {
            KnowledgeDocDTO dto = new KnowledgeDocDTO();
            BeanUtils.copyProperties(doc, dto);
            dto.setDocId(doc.getId());
            dto.setCategoryName(categoryNameMap.get(doc.getCategoryId()));
            if (!withContent) {
                dto.setContentMd(null);
            }

            List<Long> boundTagIds = docTagMap.getOrDefault(doc.getId(), Collections.emptyList());
            List<KnowledgeTagDTO> tags = boundTagIds.stream().map(tagId -> {
                KnowledgeTagDO tag = tagMap.get(tagId);
                if (tag == null) {
                    return null;
                }
                KnowledgeTagDTO tagDTO = new KnowledgeTagDTO();
                tagDTO.setTagId(tag.getId());
                tagDTO.setTagName(tag.getTagName());
                tagDTO.setStatus(tag.getStatus());
                return tagDTO;
            }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
            dto.setTags(tags);
            result.add(dto);
        }

        Map<Long, Integer> order = new LinkedHashMap<>();
        for (int i = 0; i < docs.size(); i++) {
            order.put(docs.get(i).getId(), i);
        }
        result.sort((a, b) -> Integer.compare(order.getOrDefault(a.getDocId(), 0), order.getOrDefault(b.getDocId(), 0)));
        return result;
    }
}
