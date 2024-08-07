package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

/**
 * 用户上传简历的传参
 *
 * @author YiHui
 * @date 2024/8/7
 */
@Data
public class UserResumeReplayReq {
    /**
     * 简历id
     */
    private Long resumeId;
    /**
     * 回复信息
     */
    private String replay;
    /**
     * 简历附件
     */
    private String replayUrl;
}
