package com.github.paicoding.forum.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import com.github.paicoding.forum.api.model.enums.resume.ResumeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author YiHui
 * @date 2024/8/7
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("resume")
public class ResumeDO extends BaseDO {
    /**
     * 用户
     */
    private Long userId;
    /**
     * 用户上传简历对应的描述信息
     */
    private String mark;
    /**
     * 简历附件
     */
    private String resumeUrl;

    /**
     * 简历文件名
     */
    private String resumeName;

    /**
     * 接收回信的邮箱地址
     */
    private String replayEmail;

    /**
     * 回复
     */
    private String replay;
    /**
     * 回复的附件
     */
    private String replayUrl;
    /**
     * 状态
     *
     * @see ResumeTypeEnum#getType()
     */
    private Integer type;
    /**
     * 0 有效 1 已删除
     */
    private Integer deleted;
}
