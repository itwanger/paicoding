package com.github.paicoding.forum.service.user.converter;

import com.beust.jcommander.internal.Lists;
import com.github.paicoding.forum.api.model.vo.user.dto.UserResumeDTO;
import com.github.paicoding.forum.service.user.repository.entity.ResumeDO;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2024/8/7
 */
public class UserResumeConverter {

    public static UserResumeDTO toResume(ResumeDO resume) {
        UserResumeDTO dto = new UserResumeDTO();
        dto.setResumeId(resume.getId());
        dto.setResumeUrl(resume.getResumeUrl());
        dto.setMark(resume.getMark());
        dto.setResumeName(resume.getResumeName());
        dto.setReplayEmail(resume.getReplayEmail());
        dto.setReplay(resume.getReplay());
        dto.setReplayUrl(resume.getReplayUrl());
        dto.setType(resume.getType());
        dto.setCreateTime(resume.getCreateTime().getTime());
        dto.setUpdateTime(resume.getUpdateTime().getTime());
        return dto;
    }

    public static List<UserResumeDTO> batchToResume(List<ResumeDO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(UserResumeConverter::toResume).collect(Collectors.toList());
    }
}
