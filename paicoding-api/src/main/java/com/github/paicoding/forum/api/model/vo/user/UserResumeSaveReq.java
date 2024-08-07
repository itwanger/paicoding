package com.github.paicoding.forum.api.model.vo.user;

import lombok.Data;

/**
 * 用户上传简历的传参
 *
 * @author YiHui
 * @date 2024/8/7
 */
@Data
public class UserResumeSaveReq {
    /**
     * 简历id，存在时表示更新；不存在时，表示插入
     */
    private Long resumeId;

    /**
     * 执行用户的id
     */
    private Long userId;
    /**
     * 备注信息
     */
    private String mark;
    /**
     * 简历名
     */
    private String resumeName;
    /**
     * 简历附件
     */
    private String resumeUrl;
    /**
     * 接收回复的邮件地址
     */
    private String replayEmail;
}
