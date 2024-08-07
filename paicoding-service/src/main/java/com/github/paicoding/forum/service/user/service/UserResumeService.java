package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.vo.user.UserResumeReplayReq;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReq;
import com.github.paicoding.forum.api.model.vo.user.UserResumeSaveReq;
import com.github.paicoding.forum.api.model.vo.user.dto.UserResumeDTO;

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
     * 管理员回复简历
     *
     * @param req
     * @return
     */
    UserResumeDTO replayResume(UserResumeReplayReq req);


    /**
     * 简历列表查询
     *
     * @return
     */
    List<UserResumeDTO> listResume(UserResumeReq req);
}
