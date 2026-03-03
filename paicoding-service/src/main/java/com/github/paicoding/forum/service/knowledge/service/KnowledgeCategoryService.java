package com.github.paicoding.forum.service.knowledge.service;

import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeCategoryReq;
import com.github.paicoding.forum.api.model.vo.knowledge.SearchKnowledgeCategoryReq;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeCategoryDTO;
import com.github.paicoding.forum.service.knowledge.repository.dao.KnowledgeCategoryDao;
import com.github.paicoding.forum.service.knowledge.repository.dao.KnowledgeDocDao;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeCategoryDO;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeDocDO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeCategoryService {

    private final KnowledgeCategoryDao categoryDao;
    private final KnowledgeDocDao docDao;

    public List<KnowledgeCategoryDTO> queryTreeForGuest() {
        List<KnowledgeCategoryDO> categories = categoryDao.listByStatus(1);
        List<KnowledgeDocDO> docs = docDao.lambdaQuery()
                .eq(KnowledgeDocDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(KnowledgeDocDO::getStatus, 1)
                .list();
        Map<Long, Long> categoryDocCount = docs.stream()
                .collect(Collectors.groupingBy(KnowledgeDocDO::getCategoryId, Collectors.counting()));

        Map<Long, KnowledgeCategoryDTO> categoryMap = new HashMap<>();
        List<KnowledgeCategoryDTO> level1 = new ArrayList<>();
        for (KnowledgeCategoryDO category : categories) {
            KnowledgeCategoryDTO dto = convert(category);
            dto.setDocCount(categoryDocCount.getOrDefault(category.getId(), 0L));
            categoryMap.put(category.getId(), dto);
        }

        for (KnowledgeCategoryDO category : categories) {
            KnowledgeCategoryDTO dto = categoryMap.get(category.getId());
            if (Objects.equals(category.getLevel(), 1)) {
                level1.add(dto);
            } else if (category.getParentId() != null && categoryMap.containsKey(category.getParentId())) {
                KnowledgeCategoryDTO parent = categoryMap.get(category.getParentId());
                parent.getChildren().add(dto);
                parent.setDocCount((parent.getDocCount() == null ? 0L : parent.getDocCount()) + (dto.getDocCount() == null ? 0L : dto.getDocCount()));
            }
        }
        return level1;
    }

    public PageVo<KnowledgeCategoryDTO> queryAdminPage(SearchKnowledgeCategoryReq req) {
        long pageNum = req.getPageNumber() == null || req.getPageNumber() < 1 ? 1 : req.getPageNumber();
        long pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 20 : req.getPageSize();
        long offset = (pageNum - 1) * pageSize;

        List<KnowledgeCategoryDO> list = categoryDao.lambdaQuery()
                .eq(KnowledgeCategoryDO::getDeleted, 0)
                .eq(req.getLevel() != null, KnowledgeCategoryDO::getLevel, req.getLevel())
                .eq(req.getParentId() != null, KnowledgeCategoryDO::getParentId, req.getParentId())
                .like(req.getCategoryName() != null && !req.getCategoryName().isEmpty(), KnowledgeCategoryDO::getCategoryName, req.getCategoryName())
                .orderByAsc(KnowledgeCategoryDO::getLevel)
                .orderByDesc(KnowledgeCategoryDO::getRank)
                .last("limit " + offset + "," + pageSize)
                .list();

        long total = categoryDao.lambdaQuery()
                .eq(KnowledgeCategoryDO::getDeleted, 0)
                .eq(req.getLevel() != null, KnowledgeCategoryDO::getLevel, req.getLevel())
                .eq(req.getParentId() != null, KnowledgeCategoryDO::getParentId, req.getParentId())
                .like(req.getCategoryName() != null && !req.getCategoryName().isEmpty(), KnowledgeCategoryDO::getCategoryName, req.getCategoryName())
                .count();

        List<KnowledgeCategoryDTO> result = list.stream().map(this::convert).collect(Collectors.toList());
        return PageVo.build(result, pageSize, pageNum, total);
    }

    public void saveCategory(KnowledgeCategoryReq req) {
        KnowledgeCategoryDO category;
        if (req.getCategoryId() != null && req.getCategoryId() > 0) {
            category = categoryDao.getById(req.getCategoryId());
            if (category == null) {
                return;
            }
        } else {
            category = new KnowledgeCategoryDO();
            category.setDeleted(0);
            category.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        }
        category.setParentId(req.getParentId());
        category.setLevel(req.getLevel());
        category.setCategoryName(req.getCategoryName());
        category.setSlug(req.getSlug());
        category.setRank(req.getRank() == null ? 0 : req.getRank());
        if (req.getStatus() != null) {
            category.setStatus(req.getStatus());
        }
        categoryDao.saveOrUpdate(category);
    }

    public void deleteCategory(Long categoryId) {
        categoryDao.lambdaUpdate().eq(KnowledgeCategoryDO::getId, categoryId).set(KnowledgeCategoryDO::getDeleted, 1).update();
    }

    public void operateCategory(Long categoryId, Integer status) {
        categoryDao.lambdaUpdate().eq(KnowledgeCategoryDO::getId, categoryId).set(KnowledgeCategoryDO::getStatus, status).update();
    }

    private KnowledgeCategoryDTO convert(KnowledgeCategoryDO category) {
        KnowledgeCategoryDTO dto = new KnowledgeCategoryDTO();
        BeanUtils.copyProperties(category, dto);
        dto.setCategoryId(category.getId());
        return dto;
    }
}
