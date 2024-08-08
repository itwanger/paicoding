package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.vo.user.UserResumeReplayReq;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReq;
import com.github.paicoding.forum.api.model.vo.user.UserResumeSaveReq;
import com.github.paicoding.forum.api.model.vo.user.dto.ResumeDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserResumeInfoDTO;

import java.util.List;

/**
 * 用户的简历
 *
 * @author YiHui
 * @date 2024/8/7
 */
public interface UserResumeService {

    /**
     * 用户更新/上传简历
     *
     * @param req
     * @return
     */
    Boolean saveResume(UserResumeSaveReq req);

    /**
     * 简历下载，讲状态更新为处理中
     *
     * @param resumeId
     * @return
     */
    Boolean downloadResume(Long resumeId);

    /**
     * 删除简历
     * @param resumeId
     * @return
     */
    Boolean deleteResume(Long resumeId);


    /**
     * 管理员回复简历
     *
     * @param req
     * @return
     */
    Boolean replayResume(UserResumeReplayReq req);


    /**
     * 查询用户的最近上传的简历
     *
     * @return
     */
    ResumeDTO getLatestResume(Long userId);

    /**
     * 查询简历列表
     *
     * @param req
     * @return
     */
    List<UserResumeInfoDTO> listResumes(UserResumeReq req);

    long count(UserResumeReq req);
}
