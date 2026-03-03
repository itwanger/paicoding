package com.github.paicoding.forum.service.knowledge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeDocReq;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeReviewTaskDTO;
import com.github.paicoding.forum.service.knowledge.constants.KnowledgeConst;
import com.github.paicoding.forum.service.knowledge.repository.dao.KnowledgeChangeTaskDao;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeChangeTaskDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeReviewService {

    private final KnowledgeChangeTaskDao changeTaskDao;
    private final KnowledgeDocService knowledgeDocService;
    private final ObjectMapper objectMapper;

    public Long createTask(String taskType,
                           Long targetDocId,
                           String payloadJson,
                           String llmPrompt,
                           String llmAnswer,
                           String toolTraceJson,
                           Long proposerUserId) {
        KnowledgeChangeTaskDO task = new KnowledgeChangeTaskDO();
        task.setTaskType(taskType);
        task.setTargetDocId(targetDocId);
        task.setPayloadJson(payloadJson);
        task.setLlmPrompt(llmPrompt);
        task.setLlmAnswer(llmAnswer);
        task.setToolTraceJson(toolTraceJson);
        task.setProposerUserId(proposerUserId);
        task.setStatus(KnowledgeConst.REVIEW_PENDING);
        changeTaskDao.save(task);
        return task.getId();
    }

    public PageVo<KnowledgeReviewTaskDTO> queryPage(String status, Long pageNum, Long pageSize) {
        long current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long size = pageSize == null || pageSize < 1 ? 20 : pageSize;
        long offset = (current - 1) * size;

        List<KnowledgeChangeTaskDO> list = changeTaskDao.listByStatus(status, offset, size);
        long total = changeTaskDao.countByStatus(status);

        List<KnowledgeReviewTaskDTO> result = list.stream().map(task -> {
            KnowledgeReviewTaskDTO dto = new KnowledgeReviewTaskDTO();
            BeanUtils.copyProperties(task, dto);
            dto.setTaskId(task.getId());
            return dto;
        }).collect(Collectors.toList());
        return PageVo.build(result, size, current, total);
    }

    public boolean approveTask(Long taskId, Long reviewerUserId, String comment) {
        KnowledgeChangeTaskDO task = changeTaskDao.getById(taskId);
        if (task == null || !KnowledgeConst.REVIEW_PENDING.equals(task.getStatus())) {
            return false;
        }

        try {
            KnowledgeDocReq payload = objectMapper.readValue(task.getPayloadJson(), KnowledgeDocReq.class);
            Long targetId = payload.getDocId();
            if (KnowledgeConst.TASK_CREATE.equals(task.getTaskType())) {
                targetId = knowledgeDocService.saveDoc(payload, reviewerUserId);
                task.setTargetDocId(targetId);
            } else if (KnowledgeConst.TASK_UPDATE.equals(task.getTaskType())) {
                knowledgeDocService.saveDoc(payload, reviewerUserId);
            }
        } catch (Exception e) {
            log.error("Approve knowledge task failed, taskId={}", taskId, e);
            return false;
        }

        task.setStatus(KnowledgeConst.REVIEW_APPROVED);
        task.setReviewerUserId(reviewerUserId);
        task.setReviewComment(comment);
        changeTaskDao.updateById(task);
        return true;
    }

    public boolean rejectTask(Long taskId, Long reviewerUserId, String comment) {
        KnowledgeChangeTaskDO task = changeTaskDao.getById(taskId);
        if (task == null || !KnowledgeConst.REVIEW_PENDING.equals(task.getStatus())) {
            return false;
        }
        task.setStatus(KnowledgeConst.REVIEW_REJECTED);
        task.setReviewerUserId(reviewerUserId);
        task.setReviewComment(comment);
        changeTaskDao.updateById(task);
        return true;
    }

    public List<KnowledgeChangeTaskDO> queryPendingByDocId(Long docId) {
        if (docId == null) {
            return Collections.emptyList();
        }
        return changeTaskDao.lambdaQuery()
                .eq(KnowledgeChangeTaskDO::getTargetDocId, docId)
                .eq(KnowledgeChangeTaskDO::getStatus, KnowledgeConst.REVIEW_PENDING)
                .list();
    }
}
